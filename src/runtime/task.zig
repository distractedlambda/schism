const std = @import("std");

pub const Task = struct {
    frame: anyframe,
    next: *Task = undefined,
    prior: *Task = undefined,
};

pub const TaskQueue = struct {
    head: ?*Task,

    pub fn init() @This() {
        return .{ .head = null };
    }

    pub fn pushBack(self: *@This(), task: *Task) void {
        if (self.head) |head| {
            task.next = self.head;
            task.prior = self.head.prior;
            self.head.prior = task;
        } else {
            task.next = task;
            task.prior = task;
            self.head = task;
        }
    }

    pub fn popFront(self: *@This()) ?*Task {
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

pub fn TaskSet(comptime num_priority_levels: comptime_int) type {
    return struct {
        queues: [num_priority_levels]TaskQueue,

        pub const PriorityLevel = std.math.IntFittingRange(0, num_priority_levels - 1);

        pub fn init() @This() {
            return .{ .queues = [_]TaskQueue{TaskQueue.init()} ** num_priority_levels };
        }

        pub fn add(self: *@This(), task: *Task, priority: PriorityLevel) void {
            self.queues[priority].pushBack(task);
        }

        pub fn removePrioritized(self: *@This()) ?*Task {
            for (self.queues) |*queue| {
                if (queue.popFront()) |task| {
                    return task;
                }
            }

            return null;
        }
    };
}
