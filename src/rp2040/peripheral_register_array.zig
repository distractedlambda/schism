const RegisterArray = @import("register_array.zig").RegisterArray;

pub fn PeripheralRegisterArray(
    comptime len: usize,
    comptime base_address: usize,
    comptime stride: usize,
    comptime spec: anytype,
) type {
    return struct {
        pub usingnamespace RegisterArray(len, base_address, stride, spec);

        pub const len = len;

        pub fn toggleRaw(index: usize, mask: u32) void {
            @intToPtr(*volatile u32, @This().address(index) + 0x1000).* = mask;
        }

        pub fn setRaw(index: usize, mask: u32) void {
            @intToPtr(*volatile u32, @This().address(index) + 0x2000).* = mask;
        }

        pub fn clearRaw(index: usize, mask: u32) void {
            @intToPtr(*volatile u32, @This().address(index) + 0x3000).* = mask;
        }

        pub fn toggle(index: usize, mask: anytype) void {
            toggleRaw(index, @This().Bits.mask(mask));
        }

        pub fn set(index: usize, mask: anytype) void {
            setRaw(index, @This().Bits.mask(mask));
        }

        pub fn clear(index: usize, mask: anytype) void {
            clearRaw(index, @This().Bits.mask(mask));
        }
    };
}
