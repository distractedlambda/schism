const executor = @import("runtime/executor.zig");

comptime {
    _ = @import("runtime/vectors.zig");
}

pub fn yield() void {
    suspend {
        var continuation = executor.Continuation{ .frame = @frame() };
        executor.submit(&continuation);
    }
}
