const std = @import("std");

const root = @import("root");

const arm = @import("../arm.zig");
const config = @import("config.zig");

pub const Continuation = struct {
    frame: anyframe,
    next: ?*Continuation = null,
    prior: ?*Continuation = null,

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
            head.prior.?.next = continuation;
            head.prior = continuation;
        } else {
            continuation.next = continuation;
            continuation.prior = continuation;
            self.head = continuation;
        }
    }

    pub fn spliceBack(self: *@This(), other: *@This()) void {
        const other_head = other.head orelse return;
        other.head = null;

        if (self.head) |head| {
            const other_tail = other_head.prior.?;
            const tail = head.prior.?;

            tail.next = other_head;
            other_head.prior = tail;

            other_tail.next = head;
            head.prior = other_tail;
        } else {
            self.head = other_head;
        }
    }

    pub fn popFront(self: *@This()) ?*Continuation {
        const head = self.head orelse return null;

        if (head.next == head) {
            self.head = null;
        } else {
            self.head = head.next;
            head.next.?.prior = head.prior;
            head.prior.?.next = head.next;
        }

        head.next = null;
        head.prior = null;

        return head;
    }
};

var ready_continuations = ContinuationQueue{};

pub fn submit(continuation: *Continuation) void {
    arm.disableInterrupts();
    defer arm.enableInterrupts();
    ready_continuations.pushBack(continuation);
}

pub fn submitAll(queue: *ContinuationQueue) void {
    arm.disableInterrupts();
    defer arm.enableInterrupts();
    ready_continuations.spliceBack(queue);
}

var root_frame: @Frame(root.main) = undefined;

pub fn run() noreturn {
    root_frame = async root.main();

    while (true) {
        const continuation = get_continuation: {
            while (true) {
                if (blk: {
                    arm.disableInterrupts();
                    defer arm.enableInterrupts();
                    break :blk ready_continuations.popFront();
                }) |cont| {
                    break :get_continuation cont;
                }

                arm.waitForEvent();
            }
        };

        const frame = continuation.frame;
        resume frame;
    }
}
