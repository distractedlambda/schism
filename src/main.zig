const std = @import("std");

comptime {
    _ = @import("picosystem.zig");
    _ = @import("rp2040.zig");
}

extern fn @"llvm.trap"() noreturn;

export fn main() void {
    std.log.info("All your codebase are belong to us.", .{});
}

pub fn panic(message: []const u8, error_return_trace: ?*std.builtin.StackTrace) noreturn {
    _ = message;
    _ = error_return_trace;
    @"llvm.trap"();
}
