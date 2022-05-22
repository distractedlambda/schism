const bits = @import("../bits.zig");

const Register = @import("register.zig").Register;

pub fn PeripheralRegister(comptime address: usize, comptime spec: bits.BitStructSpec) type {
    return struct {
        pub usingnamespace Register(address, spec);

        pub inline fn toggleRaw(mask: u32) void {
            @intToPtr(*volatile u32, address + 0x1000).* = mask;
        }

        pub inline fn setRaw(mask: u32) void {
            @intToPtr(*volatile u32, address + 0x2000).* = mask;
        }

        pub inline fn clearRaw(mask: u32) void {
            @intToPtr(*volatile u32, address + 0x3000).* = mask;
        }

        pub inline fn toggle(mask: @This().Bits.FlagMask) void {
            toggleRaw(@This().Bits.packFlagMask(mask));
        }

        pub inline fn set(mask: @This().Bits.FlagMask) void {
            setRaw(@This().Bits.packFlagMask(mask));
        }

        pub inline fn clear(mask: @This().Bits.FlagMask) void {
            clearRaw(@This().Bits.packFlagMask(mask));
        }
    };
}
