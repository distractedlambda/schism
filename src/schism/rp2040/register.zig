const bits = @import("../bits.zig");

pub fn Register(comptime address_: usize, comptime spec: bits.BitStructSpec) type {
    return struct {
        pub const Fields = bits.BitStruct(32, spec);

        pub const address = address_;

        pub inline fn readNonVolatileRaw() u32 {
            return @intToPtr(*const u32, address).*;
        }

        pub inline fn readRaw() u32 {
            return @intToPtr(*volatile u32, address).*;
        }

        pub inline fn writeRaw(value: u32) void {
            @intToPtr(*volatile u32, address).* = value;
        }

        pub inline fn readNonVolatile() Fields.Unpacked {
            return Fields.unpack(readNonVolatileRaw());
        }

        pub inline fn read() Fields.Unpacked {
            return Fields.unpack(readRaw());
        }

        pub inline fn write(value: Fields.Unpacked) void {
            writeRaw(Fields.pack(value));
        }
    };
}
