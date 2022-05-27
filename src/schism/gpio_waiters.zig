const ContinuationQueue = @import("executor.zig").ContinuationQueue;
const CoreLocal = @import("multicore.zig").CoreLocal;

pub const Waiters = [30]CoreLocal(ContinuationQueue);

pub var yield_until_low: Waiters = initial_waiters;

pub var yield_until_high: Waiters = initial_waiters;

const initial_waiters = [1]CoreLocal(ContinuationQueue){CoreLocal(ContinuationQueue).init(.{})} ** 30;
