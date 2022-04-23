const std = @import("std");

const BitField = @import("bits.zig").BitField;

pub const io_bank0 = @import("registers/io_bank0.zig");
pub const pll_sys = @import("registers/pll_sys.zig");
pub const pll_usb = @import("registers/pll_usb.zig");
pub const psm = @import("registers/psm.zig");
pub const resets = @import("registers/resets.zig");
pub const rosc = @import("registers/rosc.zig");
pub const sio = @import("registers/sio.zig");
pub const vreg_and_chip_reset = @import("registers/vreg_and_chip_reset.zig");

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

        pub fn write(index: Index, value: u32) void {
            @intToPtr(*volatile u32, address(index)).* = value;
        }
    };
}

pub fn RegisterMatrix(comptime rows: comptime_int, comptime cols: comptime_int, comptime base_address: u32, comptime row_stride: u32, comptime col_stride: u32) type {
    return struct {
        pub const Row = std.math.IntFittingRange(0, rows - 1);

        pub const Col = std.math.IntFittingRange(0, cols - 1);

        fn address(row: Row, col: Col) u32 {
            std.debug.assert(row >= 0 and row < rows);
            std.debug.assert(col >= 0 and col < cols);
            return base_address + row * row_stride + col * col_stride;
        }

        pub fn read(row: Row, col: Col) u32 {
            return @intToPtr(*volatile u32, address(row, col));
        }

        pub fn write(row: Row, col: Col, value: u32) void {
            @intToPtr(*volatile u32, address(row, col)).* = value;
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

pub fn PeripheralRegisterMatrix(comptime rows: comptime_int, comptime cols: comptime_int, comptime base_address: u32, comptime row_stride: u32, comptime col_stride: u32) type {
    return struct {
        pub usingnamespace PeripheralRegisterMatrix(rows, cols, base_address, row_stride, col_stride);

        pub fn toggle(row: @This().Row, col: @This().Col, mask: u32) void {
            @intToPtr(*volatile u32, @This().address(row, col) + 0x1000).* = mask;
        }

        pub fn set(row: @This().Row, col: @This().Col, mask: u32) void {
            @intToPtr(*volatile u32, @This().address(row, col) + 0x2000).* = mask;
        }

        pub fn clear(row: @This().Row, col: @This().Col, mask: u32) void {
            @intToPtr(*volatile u32, @This().address(row, col) + 0x3000).* = mask;
        }
    };
}

test {
    std.testing.refAllDecls(@This());
}
