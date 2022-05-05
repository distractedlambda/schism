const std = @import("std");

const llvmintrin = @import("llvmintrin.zig");
const runtime = @import("runtime.zig");
const picosystem = @import("picosystem.zig");

comptime {
    _ = runtime;
    _ = picosystem;
}

const led_pin = picosystem.pins.user_led.blue;
const button_pin = picosystem.pins.buttons.a;

pub const runtime_config = blk: {
    var config = runtime.Config{};
    config.gpio[led_pin].function = .{ .Sio = .{} };
    config.gpio[button_pin].function = .{ .Sio = .{ .allow_yield_until_low = true, .allow_yield_until_high = true } };
    break :blk config;
};

pub fn main() void {
    runtime.gpio.enableOutput(led_pin);

    while (true) {
        runtime.gpio.clear(led_pin);
        runtime.gpio.yieldUntilHigh(button_pin);
        runtime.gpio.set(led_pin);
        runtime.gpio.yieldUntilLow(button_pin);
    }
}

pub fn panic(message: []const u8, error_return_trace: ?*std.builtin.StackTrace) noreturn {
    _ = message;
    _ = error_return_trace;
    llvmintrin.trap();
}
