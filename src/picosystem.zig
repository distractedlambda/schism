comptime {
    _ = @import("picosystem/second_stage_bootloader.zig");
}

pub const pins = @import("picosystem/pins.zig");
