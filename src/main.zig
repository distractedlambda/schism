const std = @import("std");

const llvmintrin = @import("llvmintrin.zig");
const runtime = @import("runtime.zig");
const picosystem = @import("picosystem.zig");

comptime {
    _ = runtime;
    _ = picosystem;
}

const led_pin = picosystem.pins.user_led.blue;
const button_pin = picosystem.pins.buttons.down;

pub const runtime_config = blk: {
    var config = runtime.Config{};

    config.gpio[picosystem.pins.user_led.red].function = .{ .Sio = .{} };
    config.gpio[picosystem.pins.user_led.green].function = .{ .Sio = .{} };
    config.gpio[picosystem.pins.user_led.blue].function = .{ .Sio = .{} };

    config.gpio[picosystem.pins.buttons.a].function = .{ .Sio = .{ .allow_yield_until_low = true, .allow_yield_until_high = true } };
    config.gpio[picosystem.pins.buttons.b].function = .{ .Sio = .{ .allow_yield_until_low = true, .allow_yield_until_high = true } };
    config.gpio[picosystem.pins.buttons.x].function = .{ .Sio = .{ .allow_yield_until_low = true, .allow_yield_until_high = true } };
    config.gpio[picosystem.pins.buttons.y].function = .{ .Sio = .{ .allow_yield_until_low = true, .allow_yield_until_high = true } };

    break :blk config;
};

fn driveLed(comptime led_gpio: u5, comptime button_gpio: u5) void {
    runtime.gpio.enableOutput(led_gpio);
    while (true) {
        runtime.gpio.clear(led_gpio);
        runtime.gpio.yieldUntilLow(button_gpio);
        runtime.gpio.set(led_gpio);
        runtime.gpio.yieldUntilHigh(button_gpio);
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
    llvmintrin.trap();
}
