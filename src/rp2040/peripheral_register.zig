const Register = @import("register.zig").Register;

pub fn PeripheralRegister(comptime address: u32) type {
    return struct {
        pub usingnamespace Register(address);

        pub fn toggle(mask: u32) void {
            @intToPtr(*volatile u32, address + 0x1000).* = mask;
        }

        pub fn set(mask: u32) void {
            @intToPtr(*volatile u32, address + 0x2000).* = mask;
        }

        pub fn clear(mask: u32) void {
            @intToPtr(*volatile u32, address + 0x3000).* = mask;
        }
    };
}
