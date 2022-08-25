const std = @import("std");

const bits = @import("bits.zig");

pub fn EndianValue(comptime T: type, comptime endianness: std.builtin.Endian) type {
    return enum(bits.BitsOf(T)) {
        _,

        inline fn toEndianBits(value: T) bits.BitsOf(T) {
            return std.mem.nativeTo(bits.BitsOf(T), bits.toBits(value), endianness);
        }

        inline fn fromEndianBits(endian_bits: bits.BitsOf(T)) T {
            return bits.fromBits(T, std.mem.toNative(bits.BitsOf(T), endian_bits, endianness));
        }

        pub inline fn init(value: T) @This() {
            return @intToEnum(@This(), toEndianBits(value));
        }

        pub inline fn get(self: @This()) T {
            return fromEndianBits(@enumToInt(self));
        }

        pub inline fn assign(self: *@This(), value: T) void {
            self.* = init(value);
        }
    };
}

pub fn LittleEndian(comptime T: type) type {
    return EndianValue(T, .Little);
}

pub fn BigEndian(comptime T: type) type {
    return EndianValue(T, .Big);
}
