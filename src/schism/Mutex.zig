const executor = @import("executor.zig");

const Continuation = executor.Continuation;
const ContinuationQueue = executor.ContinuationQueue;

state: union(enum) {
    unlocked: void,
    locked: ContinuationQueue,
},

pub fn init() @This() {
    return .{ .state = .unlocked };
}

pub fn lock(self: *@This()) void {
    switch (self.state) {
        .unlocked => {
            self.state = .{ .locked = .{} };
        },

        .locked => |*queue| {
            var continuation = Continuation.init(@frame());
            suspend queue.pushBack(&continuation);
        },
    }
}

pub fn unlock(self: *@This()) void {
    if (self.state.locked.popFront()) |continuation| {
        executor.submit(continuation);
    } else {
        self.state = .unlocked;
    }
}
