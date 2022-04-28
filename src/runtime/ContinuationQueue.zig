const Continuation = @import("Continuation.zig");

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
