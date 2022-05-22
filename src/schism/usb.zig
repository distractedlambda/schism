const config = @import("config.zig").resolved;

pub const device = @import("usb/device.zig");
pub const protocol = @import("usb/protocol.zig");

pub fn init() void {
    switch (config.usb orelse return) {
        .Device => device.init(),
    }
}

pub fn handleIrq() callconv(.C) void {
    switch (config.usb orelse return) {
        .Device => device.handleIrq(),
    }
}
