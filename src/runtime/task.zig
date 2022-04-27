const std = @import("std");

pub const Task = struct {
    frame: anyframe,
    next: *Task = undefined,
    prior: *Task = undefined,

    pub fn init(frame: anyframe) @This() {
        return .{ .frame = frame };
    }
};

pub const TaskQueue = struct {
    head: ?*Task,

    pub fn init() @This() {
        return .{ .head = null };
    }

    pub fn isEmpty(self: @This()) bool {
        return self.head == null;
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

pub fn TaskSet(comptime n_priorities: comptime_int) type {
    return struct {
        pub const Priority = std.math.IntFittingRange(0, n_priorities - 1);

        pub const PrioritizedTask = struct {
            task: *Task,
            priority: Priority,
        };

        const PopulationMask = std.StaticBitSet(n_priorities);

        queues: [n_priorities]TaskQueue,
        population_mask: PopulationMask,

        pub fn init() @This() {
            return .{
                .queues = [_]TaskQueue{TaskQueue.init()} ** n_priorities,
                .population_mask = PopulationMask.initEmpty(),
            };
        }

        pub fn add(self: *@This(), task: *Task, priority: Priority) void {
            self.queues[priority].pushBack(task);
            self.population_mask.set(priority);
        }

        pub fn removePrioritized(self: *@This()) ?PrioritizedTask {
            const priority = @intCast(Priority, self.population_mask.findFirstSet() orelse return null);

            defer {
                if (self.queues[priority].isEmpty()) {
                    self.population_mask.unset(priority);
                }
            }

            return .{
                .task = self.queues[priority].popFront().*,
                .priority = priority,
            };
        }
    };
}
