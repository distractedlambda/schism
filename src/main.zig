const std = @import("std");

comptime {
    _ = @import("picosystem.zig");
    _ = @import("rp2040.zig");
}

pub fn main() anyerror!void {
    std.log.info("All your codebase are belong to us.", .{});
}
