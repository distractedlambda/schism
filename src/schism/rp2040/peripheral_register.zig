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

        pub inline fn toggle(flags: @This().Fields.Flags) void {
            toggleRaw(@This().Fields.packFlags(flags));
        }

        pub inline fn set(flags: @This().Fields.Flags) void {
            setRaw(@This().Fields.packFlags(flags));
        }

        pub inline fn clear(flags: @This().Fields.Flags) void {
            clearRaw(@This().Fields.packFlags(flags));
        }
    };
}
