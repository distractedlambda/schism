const std = @import("std");

pub const Config = struct {
    core0_stack_top: usize = 0x20042000,
    core0_stack_size: usize = 0x1000,
    core1_stack_top: usize = 0x20041000,
    core1_stack_size: usize = 0x1000,
};

pub const core0_stack_top = resolved.core0_stack_top;
pub const core0_stack_size = resolved.core0_stack_size;
pub const core1_stack_top = resolved.core1_stack_top;
pub const core1_stack_size = resolved.core1_stack_size;

const resolved: Config = blk: {
    const root = @import("root");
    if (@hasDecl(root, "runtime_config")) {
        break :blk root.runtime_config;
    } else {
        break :blk .{};
    }
};
