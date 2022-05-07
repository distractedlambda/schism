const std = @import("std");

const bits = @import("../bits.zig");

pub fn RegisterMatrix(
    comptime rows: comptime_int,
    comptime cols: comptime_int,
    comptime base_address: usize,
    comptime row_stride: usize,
    comptime col_stride: usize,
    comptime spec: anytype,
) type {
    return struct {
        pub const Bits = bits.BitStruct(u32, spec);

        pub fn address(row: usize, col: usize) usize {
            std.debug.assert(row >= 0 and row < rows);
            std.debug.assert(col >= 0 and col < cols);
            return base_address + row * row_stride + col * col_stride;
        }

        pub fn readRaw(row: usize, col: usize) u32 {
            return @intToPtr(*volatile u32, address(row, col)).*;
        }

        pub fn writeRaw(row: usize, col: usize, value: u32) void {
            @intToPtr(*volatile u32, address(row, col)).* = value;
        }

        pub fn read(row: usize, col: usize) Bits.Fields {
            return Bits.unpack(readRaw(row, col));
        }

        pub fn write(row: usize, col: usize, fields: Bits.Fields) void {
            writeRaw(row, col, Bits.pack(fields));
        }
    };
}
