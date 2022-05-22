const bits = @import("../bits.zig");

const RegisterArray = @import("register_array.zig").RegisterArray;

pub fn PeripheralRegisterArray(
    comptime len: usize,
    comptime base_address: usize,
    comptime stride: usize,
    comptime spec: bits.BitStructSpec,
) type {
    return struct {
        pub usingnamespace RegisterArray(len, base_address, stride, spec);

        pub inline fn toggleRaw(index: usize, mask: u32) void {
            @intToPtr(*volatile u32, @This().address(index) + 0x1000).* = mask;
        }

        pub inline fn setRaw(index: usize, mask: u32) void {
            @intToPtr(*volatile u32, @This().address(index) + 0x2000).* = mask;
        }

        pub inline fn clearRaw(index: usize, mask: u32) void {
            @intToPtr(*volatile u32, @This().address(index) + 0x3000).* = mask;
        }

        pub inline fn toggle(index: usize, mask: @This().Bits.FlagMask) void {
            toggleRaw(index, @This().Bits.packFlagMask(mask));
        }

        pub inline fn set(index: usize, mask: @This().Bits.FlagMask) void {
            setRaw(index, @This().Bits.packFlagMask(mask));
        }

        pub inline fn clear(index: usize, mask: @This().Bits.FlagMask) void {
            clearRaw(index, @This().Bits.packFlagMask(mask));
        }
    };
}
