const ContinuationQueue = @import("../executor.zig").ContinuationQueue;
const CoreLocal = @import("../core_local.zig").CoreLocal;

pub fn yieldUntilLow(comptime gpio: u5) *ContinuationQueue {
    _ = gpio;
    return struct {
        var waiters = CoreLocal(ContinuationQueue).init(.{});
    }.waiters.ptr();
}

pub fn yieldUntilHigh(comptime gpio: u5) *ContinuationQueue {
    _ = gpio;
    return struct {
        var waiters = CoreLocal(ContinuationQueue).init(.{});
    }.waiters.ptr();
}
