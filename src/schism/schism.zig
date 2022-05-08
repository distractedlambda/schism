const config = @import("config.zig");
const executor = @import("executor.zig");
const gpio = @import("gpio.zig");

comptime {
    _ = @import("vectors.zig");
}

pub const Config = config.Config;

pub const yield = executor.yield;

pub const enableGpioOutput = gpio.enableOutput;
pub const disableGpioOutput = gpio.disableOutput;
pub const setGpio = gpio.set;
pub const clearGpio = gpio.clear;
pub const readGpio = gpio.read;
pub const yieldUntilGpioLow = gpio.yieldUntilLow;
pub const yieldUntilGpioHigh = gpio.yieldUntilHigh;
