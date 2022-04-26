const RegisterArray = @import("register_array.zig").RegisterArray;

pub fn PeripheralRegisterArray(comptime len: comptime_int, comptime base_address: u32, comptime stride: u32) type {
    return struct {
        pub usingnamespace RegisterArray(len, base_address, stride);

        pub const len = len;

        pub fn toggle(index: @This().Index, mask: u32) void {
            @intToPtr(*volatile u32, @This().address(index) + 0x1000).* = mask;
        }

        pub fn set(index: @This().Index, mask: u32) void {
            @intToPtr(*volatile u32, @This().address(index) + 0x2000).* = mask;
        }

        pub fn clear(index: @This().Index, mask: u32) void {
            @intToPtr(*volatile u32, @This().address(index) + 0x3000).* = mask;
        }
    };
}
