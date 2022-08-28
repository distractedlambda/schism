const std = @import("std");

const bits = @import("../bits.zig");

pub fn RegisterArray(
    comptime len_: usize,
    comptime base_address: usize,
    comptime stride: usize,
    comptime spec: bits.BitStructSpec,
) type {
    return struct {
        pub const Fields = bits.BitStruct(32, spec);

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

        pub inline fn read(index: usize) Fields.Unpacked {
            return Fields.unpack(readRaw(index));
        }

        pub inline fn write(index: usize, value: Fields.Unpacked) void {
            writeRaw(index, Fields.pack(value));
        }
    };
}
