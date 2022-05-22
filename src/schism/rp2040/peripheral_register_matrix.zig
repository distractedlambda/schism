const bits = @import("../bits.zig");

const RegisterMatrix = @import("register_matrix.zig").RegisterMatrix;

pub fn PeripheralRegisterMatrix(
    comptime rows: usize,
    comptime cols: usize,
    comptime base_address: u32,
    comptime row_stride: u32,
    comptime col_stride: u32,
    comptime spec: bits.BitStructSpec,
) type {
    return struct {
        pub usingnamespace RegisterMatrix(rows, cols, base_address, row_stride, col_stride, spec);

        pub inline fn toggleRaw(row: usize, col: usize, mask: u32) void {
            @intToPtr(*volatile u32, @This().address(row, col) + 0x1000).* = mask;
        }

        pub inline fn setRaw(row: usize, col: usize, mask: u32) void {
            @intToPtr(*volatile u32, @This().address(row, col) + 0x2000).* = mask;
        }

        pub inline fn clearRaw(row: usize, col: usize, mask: u32) void {
            @intToPtr(*volatile u32, @This().address(row, col) + 0x3000).* = mask;
        }

        pub inline fn toggle(row: usize, col: usize, mask: @This().Bits.FlagMask) void {
            toggleRaw(row, col, @This().Bits.packFlagMask(mask));
        }

        pub inline fn set(row: usize, col: usize, mask: @This().Bits.FlagMask) void {
            setRaw(row, col, @This().Bits.packFlagMask(mask));
        }

        pub inline fn clear(row: usize, col: usize, mask: @This().Bits.FlagMask) void {
            clearRaw(row, col, @This().Bits.packFlagMask(mask));
        }
    };
}
