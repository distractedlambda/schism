const std = @import("std");

const bits = @import("../bits.zig");

pub fn RegisterMatrix(
    comptime rows_: comptime_int,
    comptime cols_: comptime_int,
    comptime base_address: usize,
    comptime row_stride: usize,
    comptime col_stride: usize,
    comptime spec: bits.BitStructSpec,
) type {
    return struct {
        pub const Fields = bits.BitStruct(u32, spec);

        pub const rows = rows_;
        pub const cols = cols_;

        pub inline fn address(row: usize, col: usize) usize {
            std.debug.assert(row < rows);
            std.debug.assert(col < cols);
            return base_address + row * row_stride + col * col_stride;
        }

        pub inline fn readRaw(row: usize, col: usize) u32 {
            return @intToPtr(*volatile u32, address(row, col)).*;
        }

        pub inline fn writeRaw(row: usize, col: usize, value: u32) void {
            @intToPtr(*volatile u32, address(row, col)).* = value;
        }

        pub inline fn read(row: usize, col: usize) Fields.Unpacked {
            return Fields.unpack(readRaw(row, col));
        }

        pub inline fn write(row: usize, col: usize, value: Fields.Unpacked) void {
            writeRaw(row, col, Fields.pack(value));
        }
    };
}
