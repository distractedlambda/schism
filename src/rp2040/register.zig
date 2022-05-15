const bits = @import("../bits.zig");

pub fn Register(comptime address: usize, comptime spec: bits.BitStructSpec) type {
    return struct {
        pub const Bits = bits.BitStruct(u32, spec);

        pub const address = address;

        pub inline fn readNonVolatileRaw() u32 {
            return @intToPtr(*const u32, address).*;
        }

        pub inline fn readRaw() u32 {
            return @intToPtr(*volatile u32, address).*;
        }

        pub inline fn writeRaw(value: u32) void {
            @intToPtr(*volatile u32, address).* = value;
        }

        pub inline fn readNonVolatile() Bits.Unpacked {
            return Bits.unpack(readNonVolatileRaw());
        }

        pub inline fn read() Bits.Unpacked {
            return Bits.unpack(readRaw());
        }

        pub inline fn write(unpacked: Bits.Unpacked) void {
            writeRaw(Bits.pack(unpacked));
        }
    };
}
