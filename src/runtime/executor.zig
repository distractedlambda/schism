const std = @import("std");

const arm = @import("../arm.zig");
const config = @import("config.zig");
const hardware_spinlock = @import("hardware_spinlock.zig");

pub const Continuation = struct {
    frame: anyframe,
    next: *Continuation = undefined,
    prior: *Continuation = undefined,

    pub fn init(frame: anyframe) @This() {
        return .{ .frame = frame };
    }
};

pub const ContinuationQueue = struct {
    head: ?*Continuation = null,

    pub fn isEmpty(self: @This()) bool {
        return self.head == null;
    }

    pub fn pushBack(self: *@This(), continuation: *Continuation) void {
        if (self.head) |head| {
            continuation.next = head;
            continuation.prior = head.prior;
            head.prior.next = continuation;
            head.prior = continuation;
        } else {
            continuation.next = continuation;
            continuation.prior = continuation;
            self.head = continuation;
        }
    }

    // FIXME: scribble over next and prior pointers after pop?
    pub fn popFront(self: *@This()) ?*Continuation {
        if (self.head) |head| {
            if (self.head.next == self.head) {
                defer self.head = null;
                return self.head;
            } else {
                defer self.head = self.head.next;
                self.head.next.prior = self.head.prior;
                return self.head;
            }
        }
    }
};

var ready_continuations = ContinuationQueue.init();

pub fn submit(continuation: *Continuation) void {
    arm.disableInterrupts();
    defer arm.enableInterrupts();

    hardware_spinlock.lock(config.executor_spinlock);
    defer hardware_spinlock.unlock(config.executor_spinlock);

    ready_continuations.pushBack(continuation);
}

pub fn submitAll(queue: *ContinuationQueue) void {
    const queue_head = queue.head orelse return;
    queue.head = null;

    arm.disableInterrupts();
    defer arm.enableInterrupts();

    hardware_spinlock.lock(config.executor_spinlock);
    defer hardware_spinlock.unlock(config.executor_spinlock);

    if (ready_continuations.head) |ready_continuations_head| {
        const queue_tail = queue_head.prior;
        const ready_continuations_tail = ready_continuations_head.prior;

        ready_continuations_tail.next = queue_head;
        queue_head.prior = ready_continuations_tail;

        queue_tail.next = ready_continuations_head;
        ready_continuations_head.prior = queue_tail;
    } else {
        ready_continuations.head = queue_head;
    }
}

pub fn run() noreturn {
    while (true) {
        const continuation = while (true) {
            if (blk: {
                arm.disableInterrupts();
                defer arm.enableInterrupts();

                hardware_spinlock.lock(config.executor_spinlock);
                defer hardware_spinlock.unlock(config.executor_spinlock);

                break :blk ready_continuations.removePrioritized();
            }) |cont| {
                break cont;
            }

            arm.waitForEvent();
        };

        resume continuation.frame;
    }
}
