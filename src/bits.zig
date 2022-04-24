const std = @import("std");

pub fn BitsOf(comptime T: type) type {
    return std.meta.Int(.unsigned, @bitSizeOf(T));
}

pub fn BitField(comptime T: type, comptime Word: type, comptime lsb: u16) type {
    comptime {
        std.debug.assert(@typeInfo(Word).Int.signedness == .unsigned);
        std.debug.assert(@typeInfo(Word).Int.bits >= lsb + @bitSizeOf(T));
    }

    return struct {
        pub const T = T;
        pub const Word = Word;
        pub const lsb = lsb;

        pub fn extract(word: Word) T {
            return fromBits(T, @truncate(BitsOf(T), word >> lsb));
        }

        pub fn insert(word: Word, value: T) Word {
            return (word & ~@as(Word, ((1 << @bitSizeOf(T)) - 1) << lsb)) | (@as(Word, asBits(value)) << lsb);
        }
    };
}

pub fn asBits(value: anytype) BitsOf(@TypeOf(value)) {
    const Bits = BitsOf(@TypeOf(value));
    return switch (@typeInfo(@TypeOf(value))) {
        .Bool => @boolToInt(value),
        .Float => @bitCast(Bits, value),
        .Enum => asBits(@enumToInt(value)),
        .Int => |int| if (int.signedness == .unsigned) value else @bitCast(Bits, value),
        else => @compileError("unsupported type '" ++ @typeName(@TypeOf(value)) ++ "' for asBits"),
    };
}

pub fn fromBits(comptime T: type, bits: BitsOf(T)) T {
    return switch (@typeInfo(T)) {
        .Bool => bits != 0,
        .Float => @bitCast(T, bits),
        .Enum => |enm| @intToEnum(T, fromBits(enm.tag_type, bits)),
        .Int => |int| if (int.signedness == .unsigned) bits else @bitCast(T, bits),
        else => @compileError("unsupported type '" ++ @typeName(T) ++ "' for fromBits"),
    };
}

pub fn insert(word: anytype, fields: anytype) fields[0][0].Word {
    var result: fields[0][0].Word = word;

    inline for (fields) |type_and_value| {
        comptime std.debug.assert(type_and_value.len == 2 and type_and_value[0].Word == fields[0][0].Word);
        result = type_and_value[0].insert(result, type_and_value[1]);
    }

    return result;
}

pub fn make(fields: anytype) fields[0][0].Word {
    return insert(@as(u0, 0), fields);
}
