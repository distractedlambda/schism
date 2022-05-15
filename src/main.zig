const std = @import("std");

const arm = @import("arm.zig");
const llvmintrin = @import("llvmintrin.zig");
const picosystem = @import("picosystem/picosystem.zig");
const schism = @import("schism/schism.zig");

comptime {
    _ = schism;
    _ = picosystem;
}

const led_pin = picosystem.pins.user_led.blue;
const button_pin = picosystem.pins.buttons.down;

pub const schism_config = blk: {
    var config = schism.Config{};

    config.gpio[picosystem.pins.user_led.red].function = .{ .Sio = .{} };
    config.gpio[picosystem.pins.user_led.green].function = .{ .Sio = .{} };
    config.gpio[picosystem.pins.user_led.blue].function = .{ .Sio = .{} };
    config.gpio[picosystem.pins.screen.backlight].function = .{ .Sio = .{} };

    config.gpio[picosystem.pins.buttons.a].function = .{ .Sio = .{ .allow_yield_until_low = true, .allow_yield_until_high = true } };
    config.gpio[picosystem.pins.buttons.b].function = .{ .Sio = .{ .allow_yield_until_low = true, .allow_yield_until_high = true } };
    config.gpio[picosystem.pins.buttons.x].function = .{ .Sio = .{ .allow_yield_until_low = true, .allow_yield_until_high = true } };
    config.gpio[picosystem.pins.buttons.y].function = .{ .Sio = .{ .allow_yield_until_low = true, .allow_yield_until_high = true } };

    config.usb = .{ .Device = .{
        .manufacturer = "lucascorp",
        .product = "echoer2000",
        .product_id = 0x4004,
        .vendor_id = 0xcafe,
        .serial_number = "123456",
        .interfaces = &[_]schism.Config.UsbConfig.Interface{
            .{
                .name = "marcopolo",
                .class = 0xFF,
                .subclass = 0,
                .protocol = 0,
                .endpoints = &[_]schism.Config.UsbConfig.Endpoint{
                    .{ .direction = .In },
                    .{ .direction = .Out },
                },
            },
        },
    } };

    break :blk config;
};

fn driveLed(comptime led_gpio: u5, comptime button_gpio: u5) void {
    schism.enableGpioOutput(led_gpio);
    while (true) {
        schism.clearGpio(led_gpio);
        schism.yieldUntilGpioLow(button_gpio);
        schism.setGpio(led_gpio);
        schism.yieldUntilGpioHigh(button_gpio);
    }
}

pub fn main() void {
    var drive_red = async driveLed(picosystem.pins.user_led.red, picosystem.pins.buttons.a);
    var drive_green = async driveLed(picosystem.pins.user_led.green, picosystem.pins.buttons.b);
    var drive_blue = async driveLed(picosystem.pins.user_led.blue, picosystem.pins.buttons.x);
    await drive_red;
    await drive_green;
    await drive_blue;
}

pub fn panic(message: []const u8, error_return_trace: ?*std.builtin.StackTrace) noreturn {
    _ = message;
    _ = error_return_trace;
    arm.disableInterrupts();
    schism.enableGpioOutput(picosystem.pins.screen.backlight);
    schism.setGpio(picosystem.pins.screen.backlight);
    llvmintrin.trap();
}
