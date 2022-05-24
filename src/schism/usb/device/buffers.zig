const std = @import("std");

const arm = @import("../../arm.zig");
const derived_config = @import("DerivedConfig.zig").resolved;
const rp2040 = @import("../../rp2040.zig");

const channel_buffers_base_address = rp2040.usb.dpram_base_address + 0x180;

pub const ep0 = @intToPtr(*[64]u8, rp2040.usb.dpram_base_address + 0x100);
pub const rx = @intToPtr(*const [derived_config.num_rx_channels][64]u8, channel_buffers_base_address);
pub const tx = @intToPtr(*[derived_config.num_tx_channels][64]u8, channel_buffers_base_address + @as(usize, derived_config.num_rx_channels) * 64);

pub fn submit(bufctrl_idx: u5, len: usize, pid: u1) void {
    std.debug.assert(len <= 64);

    rp2040.usb.device_ep_buf_ctrl.write(bufctrl_idx, .{
        .buf0_full = @truncate(u1, bufctrl_idx) == 0,
        .buf0_data_pid = pid,
        .buf0_len = @intCast(u10, len),
    });

    // FIXME: do we really need this dsb?
    arm.dataSynchronizationBarrier();

    // FIXME: choose the number of noops based on clock frequency bounds
    arm.nop();
    arm.nop();
    arm.nop();

    rp2040.usb.device_ep_buf_ctrl.write(bufctrl_idx, .{
        .buf0_full = @truncate(u1, bufctrl_idx) == 0,
        .buf0_data_pid = pid,
        .buf0_len = @intCast(u10, len),
        .buf0_available = true,
    });
}
