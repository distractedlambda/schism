const std = @import("std");

const arm = @import("../arm.zig");
const config = @import("config.zig");

const HardwareSpinLock = @import("HardwareSpinLock.zig");

pub const Continuation = struct {
    frame: anyframe,
    next: *Continuation = undefined,
    prior: *Continuation = undefined,
};

pub const ContinuationQueue = struct {
    head: ?*Continuation,

    pub fn init() @This() {
        return .{ .head = null };
    }

    pub fn isEmpty(self: @This()) bool {
        return self.head == null;
    }

    pub fn pushBack(self: *@This(), continuation: *Continuation) void {
        if (self.head) |head| {
            continuation.next = self.head;
            continuation.prior = self.head.prior;
            self.head.prior = continuation;
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

const ready_continuations_lock = HardwareSpinLock.init(0);
var ready_continuations = ContinuationQueue.init();

pub fn submit(continuation: *Continuation) void {
    arm.disableInterrupts();
    defer arm.enableInterrupts();

    ready_continuations_lock.lock();
    defer ready_continuations_lock.unlock();

    ready_continuations.add(continuation);
}

pub fn run() noreturn {
    while (true) {
        const continuation = while (true) {
            if (blk: {
                arm.disableInterrupts();
                defer arm.enableInterrupts();

                ready_continuations_lock.lock();
                defer ready_continuations_lock.unlock();

                break :blk ready_continuations.removePrioritized();
            }) |cont| {
                break cont;
            }

            arm.waitForEvent();
        };

        resume continuation.frame;
    }
}
