const arm = @import("../arm.zig");
const bits = @import("../bits.zig");

pub fn Register(comptime address: u32) type {
    return struct {
        pub const address = address;

        pub fn readNonVolatile() u32 {
            return @intToPtr(*const u32, address).*;
        }

        pub fn read() u32 {
            return @intToPtr(*volatile u32, address).*;
        }

        pub fn write(value: u32) void {
            @intToPtr(*volatile u32, address).* = value;
        }

        pub fn writeFields(fields: anytype) void {
            write(bits.make(fields));
        }
    };
}
