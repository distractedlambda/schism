const RegisterMatrix = @import("register_matrix.zig").RegisterMatrix;

pub fn PeripheralRegisterMatrix(
    comptime rows: comptime_int,
    comptime cols: comptime_int,
    comptime base_address: u32,
    comptime row_stride: u32,
    comptime col_stride: u32,
) type {
    return struct {
        pub usingnamespace RegisterMatrix(rows, cols, base_address, row_stride, col_stride);

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
