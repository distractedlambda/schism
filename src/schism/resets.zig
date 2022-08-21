const rp2040 = @import("rp2040.zig");

pub inline fn reset(blocks: rp2040.resets.reset.Bits.FlagMask) void {
    rp2040.resets.reset.set(blocks);
}

pub inline fn unreset(blocks: rp2040.resets.reset.Fields.Flags) void {
    const mask = rp2040.resets.reset.Fields.packFlags(blocks);
    rp2040.resets.reset.clearRaw(mask);
    while (rp2040.resets.reset_done.readRaw() & mask != mask) {}
}

pub inline fn cycleThroughReset(blocks: rp2040.resets.reset.Bits.FlagMask) void {
    reset(blocks);
    unreset(blocks);
}
