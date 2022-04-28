const std = @import("std");

const config = @import("config.zig");

const Continuation = @import("Continuation.zig");
const ContinuationQueue = @import("ContinuationQueue.zig");
const PrioritizedContinuation = @import("PrioritizedContinuation.zig");

const PopulationMask = std.StaticBitSet(config.num_priority_levels);

queues: [config.num_priority_levels]ContinuationQueue,
population_mask: PopulationMask,

pub fn init() @This() {
    return .{
        .queues = [_]ContinuationQueue{ContinuationQueue.init()} ** config.num_priority_levels,
        .population_mask = PopulationMask.initEmpty(),
    };
}

pub fn add(self: *@This(), continuation: *Continuation, priority_level: config.PriorityLevel) void {
    self.queues[priority_level].pushBack(continuation);
    self.population_mask.set(priority_level);
}

pub fn removePrioritized(self: *@This()) ?PrioritizedContinuation {
    const priority_level = @intCast(config.PriorityLevel, self.population_mask.findFirstSet() orelse return null);

    defer {
        if (self.queues[priority_level].isEmpty()) {
            self.population_mask.unset(priority_level);
        }
    }

    return PrioritizedContinuation.init(self.queues[priority_level].popFront().*, priority_level);
}
