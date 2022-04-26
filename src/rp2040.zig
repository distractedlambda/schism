pub const bootrom = @import("rp2040/bootrom.zig");
pub const registers = @import("rp2040/registers.zig");

comptime {
    _ = @import("rp2040/vectors.zig");
}

pub fn placeInReset(peripherals: anytype) void {
    registers.resets.reset.set(registers.maskFromPositions(registers.resets.Target, peripherals));
}

pub fn takeFromReset(peripherals: anytype) void {
    const mask = registers.maskFromPositions(registers.resets.Target, peripherals);
    registers.resets.reset.clear(mask);
    while ((registers.resets.reset_done.read() & mask) != mask) {}
}
