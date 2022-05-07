const std = @import("std");

pub fn BitStruct(comptime Int: type, comptime spec: anytype) type {
    if (@typeInfo(Int) != .Int or @typeInfo(Int).Int.signedness != .unsigned) {
        @compileError("expected an unsigned integer type, got " ++ @typeName(Int));
    }

    if (@TypeOf(spec) == type) {
        if (@bitSizeOf(spec) > @bitSizeOf(Int)) {
            @compileError(@typeName(spec) ++ " does not fit in " ++ @typeName(Int));
        }
    } else {
        comptime var population: Int = 0;
        inline for (spec) |field_spec| {
            if (field_spec.lsb + @bitSizeOf(field_spec.type) > @bitSizeOf(Int)) {
                @compileError("field '" ++ field_spec.name ++ "' does not fit in " ++ @typeName(Int));
            }

            if (@truncate(std.meta.Int(.unsigned, @bitSizeOf(field_spec.type)), population >> field_spec.lsb) != 0) {
                @compileError("field '" ++ field_spec.name ++ "' overlaps another field");
            }

            population |= ((1 << @bitSizeOf(field_spec.type)) - 1) << field_spec.lsb;
        }
    }

    return struct {
        pub const Int: type = Int;

        pub const Fields: type = blk: {
            if (@TypeOf(spec) == type) {
                break :blk spec;
            } else {
                comptime var struct_fields: [spec.len]std.builtin.Type.StructField = undefined;

                inline for (spec) |field_spec, i| {
                    struct_fields[i] = .{
                        .name = field_spec.name,
                        .field_type = field_spec.type,
                        .is_comptime = false,
                        .alignment = @alignOf(field_spec.type),
                        .default_value = default_blk: {
                            if (@hasField(@TypeOf(field_spec), "default")) {
                                break :default_blk &@as(field_spec.type, field_spec.default);
                            } else {
                                break :default_blk null;
                            }
                        },
                    };
                }

                break :blk @Type(.{ .Struct = .{
                    .layout = .Auto,
                    .fields = &struct_fields,
                    .decls = &[0]std.builtin.Type.Declaration{},
                    .is_tuple = false,
                } });
            }
        };

        pub const MaskBit: type = blk: {
            comptime var enum_fields: [spec.len]std.builtin.Type.EnumField = undefined;
            comptime var num_enum_fields = 0;

            inline for (spec) |field_spec| {
                if (field_spec.type == bool) {
                    enum_fields[num_enum_fields] = .{
                        .name = field_spec.name,
                        .value = 1 << field_spec.lsb,
                    };
                    num_enum_fields += 1;
                }
            }

            break :blk @Type(.{ .Enum = .{
                .layout = .Auto,
                .tag_type = u32,
                .fields = enum_fields[0..num_enum_fields],
                .decls = &[0]std.builtin.Type.Declaration{},
                .is_exhaustive = true,
            } });
        };

        pub inline fn pack(fields: Fields) Int {
            if (@TypeOf(spec) == type) {
                return asBits(fields);
            } else {
                var int: Int = 0;
                inline for (spec) |field_spec| {
                    int |= @as(Int, asBits(@field(fields, field_spec.name))) << field_spec.lsb;
                }
                return int;
            }
        }

        pub inline fn unpack(int: Int) Fields {
            if (@TypeOf(spec) == type) {
                return @truncate(spec, int);
            } else {
                var fields: Fields = undefined;
                inline for (spec) |field_spec| {
                    @field(fields, field_spec.name) = fromBits(
                        field_spec.type,
                        @truncate(@bitSizeOf(field_spec.type), int >> field_spec.lsb),
                    );
                }
                return fields;
            }
        }

        pub inline fn mask(bits: anytype) Int {
            var int: Int = 0;
            inline for (bits) |bit| {
                int |= @enumToInt(@as(MaskBit, bit));
            }
            return int;
        }
    };
}

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
