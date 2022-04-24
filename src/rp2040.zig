pub const bootrom = @import("rp2040/bootrom.zig");
pub const registers = @import("rp2040/registers.zig");

comptime {
    _ = @import("rp2040/vectors.zig");
}
