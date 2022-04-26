const std = @import("std");

const bits = @import("../bits.zig");

pub fn RegisterMatrix(
    comptime rows: comptime_int,
    comptime cols: comptime_int,
    comptime base_address: u32,
    comptime row_stride: u32,
    comptime col_stride: u32,
) type {
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

        pub fn writeFields(row: Row, col: Col, fields: anytype) void {
            write(row, col, bits.make(fields));
        }
    };
}
