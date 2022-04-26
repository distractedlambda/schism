const bits = @import("../bits.zig");

pub fn RegisterField(comptime T: type, comptime lsb: u16) type {
    return bits.BitField(T, u32, lsb);
}
