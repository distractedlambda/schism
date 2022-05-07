const RegisterMatrix = @import("register_matrix.zig").RegisterMatrix;

pub fn PeripheralRegisterMatrix(
    comptime rows: usize,
    comptime cols: usize,
    comptime base_address: u32,
    comptime row_stride: u32,
    comptime col_stride: u32,
    comptime spec: anytype,
) type {
    return struct {
        pub usingnamespace RegisterMatrix(rows, cols, base_address, row_stride, col_stride, spec);

        pub fn toggleRaw(row: usize, col: usize, mask: u32) void {
            @intToPtr(*volatile u32, @This().address(row, col) + 0x1000).* = mask;
        }

        pub fn setRaw(row: usize, col: usize, mask: u32) void {
            @intToPtr(*volatile u32, @This().address(row, col) + 0x2000).* = mask;
        }

        pub fn clearRaw(row: usize, col: usize, mask: u32) void {
            @intToPtr(*volatile u32, @This().address(row, col) + 0x3000).* = mask;
        }

        pub fn toggle(row: usize, col: usize, mask: anytype) void {
            toggleRaw(row, col, @This().Bits.mask(mask));
        }

        pub fn set(row: usize, col: usize, mask: anytype) void {
            setRaw(row, col, @This().Bits.mask(mask));
        }

        pub fn clear(row: usize, col: usize, mask: anytype) void {
            clearRaw(row, col, @This().Bits.mask(mask));
        }
    };
}
