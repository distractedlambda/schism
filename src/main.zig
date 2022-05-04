const std = @import("std");

const llvmintrin = @import("llvmintrin.zig");
const runtime = @import("runtime.zig");
const picosystem = @import("picosystem.zig");

comptime {
    _ = runtime;
    _ = picosystem;
}

pub const runtime_config = blk: {
    var config = runtime.Config{};
    break :blk config;
};

pub fn panic(message: []const u8, error_return_trace: ?*std.builtin.StackTrace) noreturn {
    _ = message;
    _ = error_return_trace;
    llvmintrin.trap();
}
