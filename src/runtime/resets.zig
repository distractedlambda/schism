const runtime = @import("../runtime.zig");
const rp2040 = @import("../rp2040.zig");

pub const SyncOrAsync = enum {
    Sync,
    Async,
};

pub inline fn reset(comptime blocks: anytype) void {
    rp2040.resets.reset.set(blocks);
}

pub inline fn unreset(comptime sync_or_async: SyncOrAsync, comptime blocks: anytype) void {
    const mask = rp2040.resets.Bits.mask(blocks);
    while (rp2040.resets.reset_done.readRaw() & mask != mask) {
        if (sync_or_async == .Async) {
            runtime.yield();
        }
    }
}

pub inline fn cycleThroughReset(comptime sync_or_async: SyncOrAsync, comptime blocks: anytype) void {
    reset(blocks);
    unreset(sync_or_async, blocks);
}
