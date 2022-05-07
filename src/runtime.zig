const executor = @import("runtime/executor.zig");

pub const gpio = @import("runtime/gpio.zig");

pub const Config = @import("runtime/config.zig").Config;

comptime {
    _ = @import("runtime/vectors.zig");
}

pub fn yield() void {
    var continuation = executor.Continuation.init(@frame());
    suspend {
        executor.submit(&continuation);
    }
}
