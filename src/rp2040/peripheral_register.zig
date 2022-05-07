const Register = @import("register.zig").Register;

pub fn PeripheralRegister(comptime address: usize, comptime spec: anytype) type {
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

        pub inline fn toggle(mask: anytype) void {
            toggleRaw(@This().Bits.mask(mask));
        }

        pub inline fn set(mask: anytype) void {
            setRaw(@This().Bits.mask(mask));
        }

        pub inline fn clear(mask: anytype) void {
            clearRaw(@This().Bits.mask(mask));
        }
    };
}
