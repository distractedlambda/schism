const executor = @import("runtime/executor.zig");

pub const Config = @import("runtime/config.zig").Config;

comptime {
    _ = @import("runtime/vectors.zig");
}

pub fn yield() void {
    suspend {
        var continuation = executor.Continuation.init(@frame());
        executor.submit(&continuation);
    }
}
