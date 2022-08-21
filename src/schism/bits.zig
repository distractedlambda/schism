const std = @import("std");

pub fn BitStruct(comptime Bits_: type, comptime spec: BitStructSpec) type {
    if (@typeInfo(Bits_) != .Int or @typeInfo(Bits_).Int.signedness != .unsigned) {
        @compileError("expected an unsigned integer type, got " ++ @typeName(Bits_));
    }

    switch (spec) {
        .Scalar => |value_type| {
            if (@bitSizeOf(value_type) > @bitSizeOf(Bits_)) {
                @compileError(@typeName(spec) ++ " does not fit in " ++ @typeName(Bits_));
            }

            return extern struct {
                pub const Bits = Bits_;

                pub const Unpacked = value_type;

                bits: Bits,

                pub inline fn init(value: Unpacked) @This() {
                    return .{ .bits = toBits(value) };
                }

                pub inline fn get(self: @This()) Unpacked {
                    return unpack(self.bits);
                }

                pub inline fn assign(self: *@This(), value: Unpacked) void {
                    self.bits = pack(value);
                }

                pub inline fn unpack(bits: Bits) Unpacked {
                    return fromBits(Unpacked, @truncate(std.meta.Int(.unsigned, @bitSizeOf(Unpacked)), bits));
                }

                pub inline fn pack(value: Unpacked) Bits {
                    return toBits(value);
                }
            };
        },

        .Record => |field_specs| {
            comptime var population: Bits_ = 0;
            comptime var unpacked_fields: [field_specs.len]std.builtin.Type.StructField = undefined;
            comptime var flags_fields: [field_specs.len]std.builtin.Type.StructField = undefined;
            comptime var next_flags_field = 0;

            inline for (field_specs) |field_spec, i| {
                if (field_spec.lsb + @bitSizeOf(field_spec.type) > @bitSizeOf(Bits_)) {
                    @compileError("field '" ++ field_spec.name ++ "' does not fit in " ++ @typeName(Bits_));
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
                    flags_fields[next_flags_field] = .{
                        .name = field_spec.name,
                        .field_type = bool,
                        .is_comptime = false,
                        .alignment = @alignOf(bool),
                        .default_value = &false,
                    };

                    next_flags_field += 1;
                }
            }

            return extern struct {
                pub const Bits = Bits_;

                pub const Unpacked = @Type(.{
                    .Struct = .{
                        .layout = .Auto,
                        .fields = &unpacked_fields,
                        .decls = &.{},
                        .is_tuple = false,
                    },
                });

                pub const Flags = @Type(.{
                    .Struct = .{
                        .layout = .Auto,
                        .fields = flags_fields[0..next_flags_field],
                        .decls = &.{},
                        .is_tuple = false,
                    },
                });

                bits: Bits,

                pub inline fn init(value: Unpacked) @This() {
                    return .{ .bits = pack(value) };
                }

                pub inline fn get(self: @This()) Unpacked {
                    return unpack(self.bits);
                }

                pub inline fn assign(self: *@This(), value: Unpacked) void {
                    self.bits = pack(value);
                }

                pub inline fn set(self: *@This(), flags: Flags) void {
                    self.bits |= packFlags(flags);
                }

                pub inline fn clear(self: *@This(), flags: Flags) void {
                    self.bits &= ~packFlags(flags);
                }

                pub inline fn toggle(self: *@This(), flags: Flags) void {
                    self.bits ^= packFlags(flags);
                }

                pub inline fn pack(unpacked: Unpacked) Bits {
                    var int: Bits = 0;

                    inline for (field_specs) |field_spec| {
                        int |= @as(Bits, toBits(@field(unpacked, field_spec.name))) << field_spec.lsb;
                    }

                    return int;
                }

                pub inline fn unpack(bits: Bits) Unpacked {
                    var unpacked: Unpacked = undefined;

                    inline for (field_specs) |field_spec| {
                        @field(unpacked, field_spec.name) = fromBits(
                            field_spec.type,
                            @truncate(std.meta.Int(.unsigned, @bitSizeOf(field_spec.type)), bits >> field_spec.lsb),
                        );
                    }

                    return unpacked;
                }

                pub inline fn packFlags(flags: Flags) Bits {
                    var int: Bits = 0;

                    inline for (field_specs) |field_spec| {
                        if (@hasField(Flags, field_spec.name)) {
                            int |= @as(Bits, toBits(@field(flags, field_spec.name))) << field_spec.lsb;
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

pub inline fn toBits(value: anytype) BitsOf(@TypeOf(value)) {
    const Bits = BitsOf(@TypeOf(value));
    return switch (@typeInfo(@TypeOf(value))) {
        .Bool => @boolToInt(value),
        .Float => @bitCast(Bits, value),
        .Enum => toBits(@enumToInt(value)),
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
