const std = @import("std");

const arm = @import("../arm.zig");
const config = @import("config.zig");
const core_local = @import("core_local.zig");

const CoreLocal = core_local.CoreLocal;

pub const Continuation = struct {
    frame: anyframe,
    next: ?*Continuation = null,
    prior: ?*Continuation = null,

    pub inline fn init(frame: anyframe) @This() {
        return .{ .frame = frame };
    }
};

pub const ContinuationQueue = struct {
    head: ?*Continuation = null,

    pub inline fn isEmpty(self: @This()) bool {
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

    pub fn peekFront(self: *@This()) ?*Continuation {
        return self.head;
    }
};

var ready_continuations = CoreLocal(ContinuationQueue).init(.{});

pub fn submit(continuation: *Continuation) void {
    ready_continuations.ptr().pushBack(continuation);
}

pub fn submitAll(queue: *ContinuationQueue) void {
    ready_continuations.ptr().spliceBack(queue);
}

pub fn run() noreturn {
    while (true) {
        if (ready_continuations.ptr().popFront()) |continuation| {
            const frame = continuation.frame;
            resume frame;
        } else {
            arm.waitForInterrupt();
        }

        arm.enableInterrupts();
        arm.disableInterrupts();
    }
}

pub fn yield() void {
    var continuation = Continuation.init(@frame());
    suspend {
        submit(&continuation);
    }
}
