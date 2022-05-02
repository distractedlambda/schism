const executor = @import("executor.zig");

const Spinlock = @import("Spinlock.zig");

spinlock: Spinlock,
acquired: bool,
waiters: executor.ContinuationQueue,

pub fn init() @This() {
    return .{
        .spinlock = Spinlock.init(),
        .acquired = false,
        .waiters = executor.ContinuationQueue.init(),
    };
}

pub fn lock(self: *@This()) void {
    self.spinlock.lock();
    defer self.spinlock.unlock();
    if (self.acquired) {
        suspend {
            var continuation = executor.Continuation.init(@frame());
            self.waiters.pushBack(&continuation);
        }
    } else {
        self.acquired = true;
    }
}

pub fn unlock(self: *@This()) void {
    self.spinlock.lock();
    defer self.spinlock.unlock();
    if (self.waiters.popFront()) |continuation| {
        executor.submit(continuation);
    } else {
        self.acquired = false;
    }
}
