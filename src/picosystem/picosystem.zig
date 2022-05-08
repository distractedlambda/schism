comptime {
    _ = @import("second_stage_bootloader.zig");
}

pub const pins = @import("pins.zig");
