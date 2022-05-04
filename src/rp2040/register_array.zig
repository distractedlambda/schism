const std = @import("std");

const bits = @import("../bits.zig");

pub fn RegisterArray(comptime len: comptime_int, comptime base_address: u32, comptime stride: u32) type {
    return struct {
        pub const Index = std.math.IntFittingRange(0, len - 1);

        fn address(index: Index) u32 {
            std.debug.assert(index >= 0 and index < len);
            return base_address + index * stride;
        }

        pub fn read(index: Index) u32 {
            return @intToPtr(*volatile u32, address(index)).*;
        }

        pub fn write(index: Index, value: u32) void {
            @intToPtr(*volatile u32, address(index)).* = value;
        }

        pub fn writeFields(index: Index, fields: anytype) void {
            write(index, bits.make(fields));
        }
    };
}
