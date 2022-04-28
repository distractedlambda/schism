const config = @import("config.zig");

const Continuation = @import("Continuation.zig");

continuation: *Continuation,
priority_level: config.PriorityLevel,

pub fn init(continuation: *Continuation, priority_level: config.PriorityLevel) @This() {
    return .{ .continuation = continuation, .priority_level = priority_level };
}
