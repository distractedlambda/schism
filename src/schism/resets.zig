const rp2040 = @import("../rp2040/rp2040.zig");

pub inline fn reset(comptime blocks: anytype) void {
    rp2040.resets.reset.set(blocks);
}

pub inline fn unreset(comptime blocks: anytype) void {
    const mask = rp2040.resets.Bits.mask(blocks);
    rp2040.resets.reset.clear(blocks);
    while (rp2040.resets.reset_done.readRaw() & mask != mask) {}
}

pub inline fn cycleThroughReset(comptime blocks: anytype) void {
    reset(blocks);
    unreset(blocks);
}
