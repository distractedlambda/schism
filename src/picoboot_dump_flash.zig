const std = @import("std");

const libusb = @import("host/libusb.zig");
const picoboot = @import("host/picoboot.zig");

var flash_read_buffer: [16 * 1024 * 1024]u8 = undefined;

var should_exit: bool = false;
var async_main_frame: @Frame(asyncMain) = undefined;

pub fn main() anyerror!void {
    const context = try libusb.Context.init();
    defer context.exit();

    async_main_frame = async asyncMain(context);

    while (!should_exit) {
        try context.handleEvents();
    }

    try nosuspend await async_main_frame;
}

fn asyncMain(context: *libusb.Context) anyerror!void {
    defer should_exit = true;

    const device = (try picoboot.Device.findAny(context)) orelse return error.NoPicobootDeviceFound;
    defer device.deinit();

    const connection = try device.open();
    defer connection.close();

    try connection.read(&flash_read_buffer, 0x10000000);

    const out_file = try std.fs.cwd().createFile("dump.bin", .{});
    defer out_file.close();

    try out_file.writeAll(&flash_read_buffer);
}
