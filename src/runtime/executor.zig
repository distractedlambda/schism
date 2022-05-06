const std = @import("std");

const root = @import("root");

const arm = @import("../arm.zig");
const config = @import("config.zig");

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

    pub fn popFront(self: *@This()) ?*Continuation {
        const head = self.head orelse return null;

        if (head.next == self.head) {
            self.head = null;
        } else {
            self.head = head.next;
            head.next.prior = head.prior;
            head.prior.next = head.next;
        }

        head.next = undefined;
        head.prior = undefined;

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
    const queue_head = queue.head orelse return;
    queue.head = null;

    arm.disableInterrupts();
    defer arm.enableInterrupts();

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

const RootFrame = @Frame(root.main);

var root_frame_buffer: [@sizeOf(RootFrame)]u8 align(@alignOf(RootFrame)) = undefined;

pub fn run() noreturn {
    const root_frame = @asyncCall(&root_frame_buffer, {}, root.main, .{});
    var root_continuation = Continuation.init(root_frame);
    submit(&root_continuation);

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

                // arm.waitForEvent();
            }
        };

        const frame = continuation.frame;
        resume frame;
    }
}
