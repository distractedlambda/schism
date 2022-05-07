const std = @import("std");

const bits = @import("../bits.zig");

pub fn Register(comptime address: usize, comptime spec: anytype) type {
    return struct {
        pub const Bits = bits.BitStruct(u32, spec);

        pub const address = address;

        pub fn readNonVolatileRaw() u32 {
            return @intToPtr(*const u32, address).*;
        }

        pub fn readRaw() u32 {
            return @intToPtr(*volatile u32, address).*;
        }

        pub fn writeRaw(value: u32) void {
            @intToPtr(*volatile u32, address).* = value;
        }

        pub fn readNonVolatile() Bits.Fields {
            return Bits.unpack(readNonVolatileRaw());
        }

        pub fn read() Bits.Fields {
            return Bits.unpack(readRaw());
        }

        pub fn write(fields: Bits.Fields) void {
            writeRaw(Bits.pack(fields));
        }
    };
}
