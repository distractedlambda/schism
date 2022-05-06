const std = @import("std");

const arm = @import("../arm.zig");
const config = @import("config.zig");
const core_local = @import("core_local.zig");
const executor = @import("executor.zig");
const rp2040 = @import("../rp2040.zig");
const waiters = @import("gpio/waiters.zig");

const Continuation = executor.Continuation;
const ContinuationQueue = executor.ContinuationQueue;
const CoreLocal = core_local.CoreLocal;

fn checkGpioInBounds(comptime gpio: u5) void {
    if (gpio >= 30) {
        @compileError(std.fmt.comptimePrint("{} is not a valid GPIO pin number", .{gpio}));
    }
}

fn checkGpioAssignedToSio(comptime gpio: u5) void {
    comptime checkGpioInBounds(gpio);
    if (config.gpio[gpio].function != .Sio) {
        @compileError(std.fmt.comptimePrint("GPIO {} is not assigned to SIO", .{gpio}));
    }
}

pub fn enableOutput(comptime gpio: u5) void {
    comptime checkGpioAssignedToSio(gpio);
    rp2040.sio.gpio_oe_set.write(@as(u32, 1) << gpio);
}

pub fn disableOutput(comptime gpio: u5) void {
    comptime checkGpioAssignedToSio(gpio);
    rp2040.sio.gpio_oe_clr.write(@as(u32, 1) << gpio);
}

pub fn set(comptime gpio: u5) void {
    comptime checkGpioAssignedToSio(gpio);
    rp2040.sio.gpio_out_set.write(@as(u32, 1) << gpio);
}

pub fn clear(comptime gpio: u5) void {
    comptime checkGpioAssignedToSio(gpio);
    rp2040.sio.gpio_out_clr.write(@as(u32, 1) << gpio);
}

pub fn read(comptime gpio: u5) u1 {
    comptime checkGpioAssignedToSio(gpio);
    return @truncate(u1, rp2040.sio.gpio_in.read() >> gpio);
}

pub fn yieldUntilLow(comptime gpio: u5) void {
    comptime checkGpioAssignedToSio(gpio);

    if (!config.gpio[gpio].function.Sio.allow_yield_until_low) {
        comptime @compileError(std.fmt.comptimePrint("yieldUntilLow is not enabled for GPIO {}", .{gpio}));
    }

    var continuation = Continuation.init(@frame());

    suspend {
        arm.disableInterrupts();
        defer arm.enableInterrupts();
        waiters.yieldUntilLow(gpio).pushBack(&continuation);
        rp2040.io_bank0.proc_inte.set(core_local.currentCore(), gpio / 8, @as(u32, 1) << (gpio % 8 * 4));
    }
}

pub fn yieldUntilHigh(comptime gpio: u5) void {
    comptime checkGpioAssignedToSio(gpio);

    if (!config.gpio[gpio].function.Sio.allow_yield_until_high) {
        comptime @compileError(std.fmt.comptimePrint("yieldUntilHigh is not enabled for GPIO {}", .{gpio}));
    }

    var continuation = Continuation.init(@frame());

    suspend {
        arm.disableInterrupts();
        defer arm.enableInterrupts();
        waiters.yieldUntilHigh(gpio).pushBack(&continuation);
        rp2040.io_bank0.proc_inte.set(core_local.currentCore(), gpio / 8, @as(u32, 1) << (gpio % 8 * 4 + 1));
    }
}
