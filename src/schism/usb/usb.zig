const config = @import("../config.zig").resolved;

pub const device = @import("device.zig");
pub const protocol = @import("protocol.zig");

pub fn handleIrq() callconv(.C) void {
    switch (comptime config.usb orelse return) {
        .Device => device.handleIrq(),
        else => @panic("got USB IRQ, but USB is not configured"),
    }
}

pub fn init() void {
    switch (comptime config.usb orelse return) {
        .Device => device.init(),
    }
}
