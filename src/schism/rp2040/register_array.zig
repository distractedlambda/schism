const std = @import("std");

const bits = @import("../bits.zig");

pub fn RegisterArray(
    comptime len_: usize,
    comptime base_address: usize,
    comptime stride: usize,
    comptime spec: bits.BitStructSpec,
) type {
    return struct {
        pub const Bits = bits.BitStruct(u32, spec);

        pub const len = len_;

        inline fn address(index: usize) usize {
            std.debug.assert(index < len);
            return base_address + index * stride;
        }

        pub inline fn readRaw(index: usize) u32 {
            return @intToPtr(*volatile u32, address(index)).*;
        }

        pub inline fn writeRaw(index: usize, value: u32) void {
            @intToPtr(*volatile u32, address(index)).* = value;
        }

        pub inline fn read(index: usize) Bits.Unpacked {
            return Bits.unpack(readRaw(index));
        }

        pub inline fn write(index: usize, unpacked: Bits.Unpacked) void {
            writeRaw(index, Bits.pack(unpacked));
        }
    };
}
