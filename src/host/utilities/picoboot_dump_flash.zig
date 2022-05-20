const libusb = @import("../libusb.zig");
const picoboot = @import("../picoboot.zig");

var flash_read_buffer: [16 * 1024 * 1024]u8 = undefined;

pub fn main() anyerror!void {
    const context = try libusb.Context.init();
    defer context.exit();

    const device = picoboot.Device.findAny(context) orelse return error.NoPicobootDeviceFound;
    defer device.deinit();

    const connection = try device.open();
    defer connection.close();

    try connection.read(flash_read_buffer, 0x10000000);
}
