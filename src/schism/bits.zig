const std = @import("std");

pub fn BitStruct(comptime Int: type, comptime spec: BitStructSpec) type {
    if (@typeInfo(Int) != .Int or @typeInfo(Int).Int.signedness != .unsigned) {
        @compileError("expected an unsigned integer type, got " ++ @typeName(Int));
    }

    switch (spec) {
        .Scalar => |value_type| {
            if (@bitSizeOf(value_type) > @bitSizeOf(Int)) {
                @compileError(@typeName(spec) ++ " does not fit in " ++ @typeName(Int));
            }

            return struct {
                pub const Int = Int;

                pub const Unpacked = value_type;

                pub inline fn pack(value: Unpacked) Int {
                    return asBits(value);
                }

                pub inline fn unpack(int: Int) Unpacked {
                    return fromBits(Unpacked, @truncate(std.meta.Int(.unsigned, @bitSizeOf(Unpacked)), int));
                }
            };
        },

        .Record => |field_specs| {
            comptime var population: Int = 0;
            comptime var unpacked_fields: [field_specs.len]std.builtin.Type.StructField = undefined;
            comptime var flag_mask_fields: [field_specs.len]std.builtin.Type.StructField = undefined;
            comptime var next_flag_mask_field = 0;

            inline for (field_specs) |field_spec, i| {
                if (field_spec.lsb + @bitSizeOf(field_spec.type) > @bitSizeOf(Int)) {
                    @compileError("field '" ++ field_spec.name ++ "' does not fit in " ++ @typeName(Int));
                }

                if (@truncate(std.meta.Int(.unsigned, @bitSizeOf(field_spec.type)), population >> field_spec.lsb) != 0) {
                    @compileError("field '" ++ field_spec.name ++ "' overlaps another field");
                }

                population |= ((1 << @bitSizeOf(field_spec.type)) - 1) << field_spec.lsb;

                unpacked_fields[i] = .{
                    .name = field_spec.name,
                    .field_type = field_spec.type,
                    .is_comptime = false,
                    .alignment = @alignOf(field_spec.type),
                    .default_value = field_spec.default,
                };

                if (field_spec.type == bool) {
                    flag_mask_fields[next_flag_mask_field] = .{
                        .name = field_spec.name,
                        .field_type = bool,
                        .is_comptime = false,
                        .alignment = @alignOf(bool),
                        .default_value = &false,
                    };

                    next_flag_mask_field += 1;
                }
            }

            return struct {
                pub const Int = Int;

                pub const Unpacked = @Type(.{
                    .Struct = .{
                        .layout = .Auto,
                        .fields = &unpacked_fields,
                        .decls = &.{},
                        .is_tuple = false,
                    },
                });

                pub const FlagMask = @Type(.{
                    .Struct = .{
                        .layout = .Auto,
                        .fields = flag_mask_fields[0..next_flag_mask_field],
                        .decls = &.{},
                        .is_tuple = false,
                    },
                });

                pub inline fn pack(unpacked: Unpacked) Int {
                    var int: Int = 0;

                    inline for (field_specs) |field_spec| {
                        int |= @as(Int, asBits(@field(unpacked, field_spec.name))) << field_spec.lsb;
                    }

                    return int;
                }

                pub inline fn unpack(int: Int) Unpacked {
                    var unpacked: Unpacked = undefined;

                    inline for (field_specs) |field_spec| {
                        @field(unpacked, field_spec.name) = fromBits(
                            field_spec.type,
                            @truncate(std.meta.Int(.unsigned, @bitSizeOf(field_spec.type)), int >> field_spec.lsb),
                        );
                    }

                    return unpacked;
                }

                pub inline fn packFlagMask(mask: FlagMask) Int {
                    var int: Int = 0;

                    inline for (field_specs) |field_spec| {
                        if (@hasField(FlagMask, field_spec.name)) {
                            int |= @as(Int, asBits(@field(mask, field_spec.name))) << field_spec.lsb;
                        }
                    }

                    return int;
                }
            };
        },
    }
}

pub const BitStructSpec = union(enum) {
    Scalar: type,
    Record: []const BitStructField,
};

pub const BitStructField = struct {
    name: []const u8,
    type: type,
    lsb: u16,
    default: ?*const anyopaque = null,
};

pub fn BitsOf(comptime T: type) type {
    return std.meta.Int(.unsigned, @bitSizeOf(T));
}

pub inline fn asBits(value: anytype) BitsOf(@TypeOf(value)) {
    const Bits = BitsOf(@TypeOf(value));
    return switch (@typeInfo(@TypeOf(value))) {
        .Bool => @boolToInt(value),
        .Float => @bitCast(Bits, value),
        .Enum => asBits(@enumToInt(value)),
        .Int => |int| if (int.signedness == .unsigned) value else @bitCast(Bits, value),
        else => @compileError("unsupported type '" ++ @typeName(@TypeOf(value)) ++ "' for asBits"),
    };
}

pub inline fn fromBits(comptime T: type, bits: BitsOf(T)) T {
    return switch (@typeInfo(T)) {
        .Bool => bits != 0,
        .Float => @bitCast(T, bits),
        .Enum => |enm| @intToEnum(T, fromBits(enm.tag_type, bits)),
        .Int => |int| if (int.signedness == .unsigned) bits else @bitCast(T, bits),
        else => @compileError("unsupported type '" ++ @typeName(T) ++ "' for fromBits"),
    };
}
