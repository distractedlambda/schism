const std = @import("std");

const BitField = @import("bits.zig").BitField;

pub const rosc = @import("registers/rosc.zig");

pub fn RegisterField(comptime T: type, comptime lsb: u16) type {
    return BitField(T, u32, lsb);
}

pub fn Register(comptime address: u32) type {
    return struct {
        pub fn read() u32 {
            return @intToPtr(*volatile u32, address).*;
        }

        pub fn write(value: u32) void {
            @intToPtr(*volatile u32, address).* = value;
        }
    };
}

pub fn PeripheralRegister(comptime address: u32) type {
    return struct {
        pub usingnamespace Register(address);

        pub fn toggle(mask: u32) void {
            @intToPtr(*volatile u32, address + 0x1000).* = mask;
        }

        pub fn set(mask: u32) void {
            @intToPtr(*volatile u32, address + 0x2000).* = mask;
        }

        pub fn clear(mask: u32) void {
            @intToPtr(*volatile u32, address + 0x3000).* = mask;
        }
    };
}

pub fn RegisterArray(comptime len: comptime_int, comptime base_address: u32, comptime stride: u32) type {
    return struct {
        pub const Index = std.math.IntFittingRange(0, len - 1);

        fn address(index: Index) u32 {
            std.debug.assert(index >= 0 and index < len);
            return base_address + index * stride;
        }

        pub fn read(index: Index) u32 {
            return @intToPtr(*volatile u32, address(index));
        }

        pub fn write(index: Index, value: u32) u32 {
            @intToPtr(*volatile u32, address(index)).* = value;
        }
    };
}

pub fn PeripheralRegisterArray(comptime len: comptime_int, comptime base_address: u32, comptime stride: u32) type {
    return struct {
        pub usingnamespace RegisterArray(len, base_address, stride);

        pub const len = len;

        pub fn toggle(index: @This().Index, mask: u32) void {
            @intToPtr(*volatile u32, @This().address(index) + 0x1000).* = mask;
        }

        pub fn set(index: @This().Index, mask: u32) void {
            @intToPtr(*volatile u32, @This().address(index) + 0x2000).* = mask;
        }

        pub fn clear(index: @This().Index, mask: u32) void {
            @intToPtr(*volatile u32, @This().address(index) + 0x3000).* = mask;
        }
    };
}

test {
    std.testing.refAllDecls(@This());
}
