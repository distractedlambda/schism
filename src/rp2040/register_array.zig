const std = @import("std");

const bits = @import("../bits.zig");

pub fn RegisterArray(
    comptime len: usize,
    comptime base_address: usize,
    comptime stride: usize,
    comptime spec: anytype,
) type {
    return struct {
        pub const Bits = bits.BitStruct(u32, spec);

        fn address(index: usize) usize {
            std.debug.assert(index >= 0 and index < len);
            return base_address + index * stride;
        }

        pub fn readRaw(index: usize) u32 {
            return @intToPtr(*volatile u32, address(index)).*;
        }

        pub fn writeRaw(index: usize, value: u32) void {
            @intToPtr(*volatile u32, address(index)).* = value;
        }

        pub fn read(index: usize) Bits.Fields {
            return Bits.pack(readRaw(index));
        }

        pub fn write(index: usize, fields: Bits.Fields) void {
            writeRaw(index, Bits.pack(fields));
        }
    };
}
