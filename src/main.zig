const std = @import("std");

const schism = @import("schism.zig");

comptime {
    _ = schism;
    _ = schism.picosystem;
}

const led_pin = schism.picosystem.pins.user_led.blue;
const button_pin = schism.picosystem.pins.buttons.down;

pub const schism_config = blk: {
    var config = schism.Config{};

    config.gpio[schism.picosystem.pins.user_led.red].function = .{ .Sio = .{} };
    config.gpio[schism.picosystem.pins.user_led.green].function = .{ .Sio = .{} };
    config.gpio[schism.picosystem.pins.user_led.blue].function = .{ .Sio = .{} };
    config.gpio[schism.picosystem.pins.screen.backlight].function = .{ .Sio = .{} };

    config.gpio[schism.picosystem.pins.buttons.a].function = .{ .Sio = .{ .allow_yield_until_low = true, .allow_yield_until_high = true } };
    config.gpio[schism.picosystem.pins.buttons.b].function = .{ .Sio = .{ .allow_yield_until_low = true, .allow_yield_until_high = true } };
    config.gpio[schism.picosystem.pins.buttons.x].function = .{ .Sio = .{ .allow_yield_until_low = true, .allow_yield_until_high = true } };
    config.gpio[schism.picosystem.pins.buttons.y].function = .{ .Sio = .{ .allow_yield_until_low = true, .allow_yield_until_high = true } };

    config.usb = .{ .Device = .{
        .manufacturer = "lucascorp",
        .product = "echoer2000",
        .product_id = 0x4004,
        .vendor_id = 0xcafe,
        .serial_number = "123456",
        .interfaces = &[_]schism.Config.UsbInterface{
            .{
                .name = "Schism Logging",
                .class = 0xFF,
                .subclass = 0,
                .protocol = 0,
                .endpoints = &[_]schism.Config.UsbEndpoint{
                    .{ .direction = .In },
                },
            },
        },
    } };

    break :blk config;
};

fn driveLed(comptime led_gpio: u5, comptime button_gpio: u5) void {
    schism.enableGpioOutput(led_gpio);
    while (true) {
        std.log.info("Clearing gpio {}", .{led_gpio});
        schism.clearGpio(led_gpio);
        schism.yieldUntilGpioLow(button_gpio);
        std.log.info("Setting gpio {}", .{led_gpio});
        schism.setGpio(led_gpio);
        schism.yieldUntilGpioHigh(button_gpio);
    }
}

pub fn main() void {
    var drive_red = async driveLed(schism.picosystem.pins.user_led.red, schism.picosystem.pins.buttons.a);
    var drive_green = async driveLed(schism.picosystem.pins.user_led.green, schism.picosystem.pins.buttons.b);
    var drive_blue = async driveLed(schism.picosystem.pins.user_led.blue, schism.picosystem.pins.buttons.x);
    await drive_red;
    await drive_green;
    await drive_blue;
}

pub const log = schism.usbLog;

pub noinline fn panic(message: []const u8, error_return_trace: ?*std.builtin.StackTrace) noreturn {
    _ = message;
    _ = error_return_trace;
    schism.arm.disableInterrupts();
    schism.enableGpioOutput(schism.picosystem.pins.screen.backlight);
    schism.setGpio(schism.picosystem.pins.screen.backlight);
    schism.llvmintrin.trap();
}
