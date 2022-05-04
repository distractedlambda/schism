const std = @import("std");

const arm = @import("../arm.zig");
const config = @import("config.zig");
const core_local = @import("core_local.zig");
const executor = @import("executor.zig");
const hardware_spinlock = @import("hardware_spinlock.zig");
const rp2040 = @import("../rp2040.zig");

const Continuation = executor.Continuation;
const ContinuationQueue = executor.ContinuationQueue;
const CoreLocal = core_local.CoreLocal;

fn checkGpioInBounds(comptime gpio: u5) void {
    if (gpio >= 30) {
        @compileError(std.fmt.comptimePrint("{} is not a valid GPIO pin number", .{gpio}));
    }
}

fn checkGpioAssignedToSio(comptime gpio: u5) void {
    checkGpioInBounds(gpio);
    if (config.gpio[gpio].function != .Sio) {
        @compileError(std.fmt.comptimePrint("GPIO {} is not assigned to SIO", .{gpio}));
    }
}

pub fn enableOutput(comptime gpio: u5) void {
    checkGpioAssignedToSio(gpio);
    rp2040.sio.gpio_oe_set.write(@as(u32, 1) << gpio);
}

pub fn disableOutput(comptime gpio: u5) void {
    checkGpioAssignedToSio(gpio);
    rp2040.sio.gpio_oe_clr.write(@as(u32, 1) << gpio);
}

pub fn set(comptime gpio: u5) void {
    checkGpioAssignedToSio(gpio);
    rp2040.sio.gpio_out_set.write(@as(u32, 1) << gpio);
}

pub fn clear(comptime gpio: u5) void {
    checkGpioAssignedToSio(gpio);
    rp2040.sio.gpio_out_clr.write(@as(u32, 1) << gpio);
}

pub fn read(comptime gpio: u5) u1 {
    checkGpioAssignedToSio(gpio);
    return @truncate(u1, rp2040.sio.gpio_in.read() >> gpio);
}

fn yieldUntilLowWaiters(comptime gpio: u5) *ContinuationQueue {
    _ = gpio;
    return struct {
        var waiters = CoreLocal(ContinuationQueue).init(.{});
    }.waiters.ptr();
}

fn yieldUntilHighWaiters(comptime gpio: u5) *ContinuationQueue {
    _ = gpio;
    return struct {
        var waiters = CoreLocal(ContinuationQueue).init(.{});
    }.waiters.ptr();
}

pub fn yieldUntilLow(comptime gpio: u5) void {
    checkGpioAssignedToSio(gpio);

    if (!config.gpio[gpio].function.Sio.allow_yield_until_low) {
        @compileError(std.fmt.comptimePrint("yieldUntilLow is not enabled for GPIO {}", .{gpio}));
    }

    suspend {
        arm.disableInterrupts();
        defer arm.enableInterrupts();

        var continuation = Continuation.init(@frame());
        yieldUntilLowWaiters(gpio).pushBack(&continuation);

        rp2040.io_bank0.proc_inte.set(core_local.currentCore(), gpio / 8, @as(u32, 1) << (gpio % 8 * 4));
    }
}

pub fn yieldUntilHigh(comptime gpio: u5) void {
    checkGpioAssignedToSio(gpio);

    if (!config.gpio[gpio].function.Sio.allow_yield_until_high) {
        @compileError(std.fmt.comptimePrint("yieldUntilHigh is not enabled for GPIO {}", .{gpio}));
    }

    suspend {
        arm.disableInterrupts();
        defer arm.enableInterrupts();

        var continuation = Continuation.init(@frame());
        yieldUntilHighWaiters(gpio).pushBack(&continuation);

        rp2040.io_bank0.proc_inte.set(core_local.currentCore(), gpio / 8, @as(u32, 1) << (gpio % 8 * 4 + 1));
    }
}

pub fn processInterrupt() void {
    // FIXME: don't load all status registers unless we need all of them
    var interrupt_status: [rp2040.io_bank0.intr.len]u32 = undefined;

    for (interrupt_status) |*status, i| {
        status.* = rp2040.io_bank0.proc_ints.read(core_local.currentCore(), @intCast(u2, i));
        rp2040.io_bank0.proc_inte.clear(core_local.currentCore(), @intCast(u2, i), status.*);
    }

    inline for (config.gpio) |gpio_config, gpio| {
        if (gpio_config.function != .Sio) {
            continue;
        }

        if (gpio_config.function.Sio.allow_yield_until_low) {
            if (@truncate(u1, interrupt_status[gpio / 8] >> (gpio % 8 * 4)) != 0) {
                executor.submitAll(yieldUntilLowWaiters(gpio));
            }
        }

        if (gpio_config.function.Sio.allow_yield_until_high) {
            if (@truncate(u1, interrupt_status[gpio / 8] >> (gpio % 8 * 4 + 1)) != 0) {
                executor.submitAll(yieldUntilHighWaiters(gpio));
            }
        }
    }
}
