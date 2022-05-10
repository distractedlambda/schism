const config = @import("../config.zig");

pub const device = @import("device.zig");
pub const protocol = @import("protocol.zig");

pub fn handleIrq() void {
    switch (comptime config.usb) {
        .Device => device.handleIrq(),
        else => @panic("got USB IRQ, but USB is not configured"),
    }
}
