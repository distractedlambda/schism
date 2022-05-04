const std = @import("std");

const arm = @import("../arm.zig");
const config = @import("config.zig");
const core_local = @import("core_local.zig");
const executor = @import("executor.zig");
const hardware_spinlock = @import("hardware_spinlock.zig");
const rp2040 = @import("rp2040.zig");

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

pub fn yieldUntilLow(comptime gpio: u5) void {
    const statics = struct {
        var waiters = ContinuationQueue.init();
    };

    checkGpioAssignedToSio(gpio);

    comptime const spinlock = config.gpio[gpio].function.Sio.yield_until_low_spinlock.?;

    suspend {
        arm.disableInterrupts();
        defer arm.enableInterrupts();

        hardware_spinlock.lock(spinlock);
        defer hardware_spinlock.unlock(spinlock);

        var continuation = Continuation.init(@frame());
        statics.waiters.pushBack(&continuation);

        rp2040.io_bank0.proc_inte.set(0, gpio / 8, @as(u32, 1) << (gpio % 8 * 4));
    }
}

pub fn yieldUntilHigh(comptime gpio: u5) void {
    const statics = struct {
        var waiters = ContinuationQueue.init();
    };

    checkGpioAssignedToSio(gpio);

    comptime const spinlock = config.gpio[gpio].function.Sio.yield_until_high_spinlock.?;

    suspend {
        arm.disableInterrupts();
        defer arm.enableInterrupts();

        hardware_spinlock.lock(spinlock);
        defer hardware_spinlock.unlock(spinlock);

        var continuation = Continuation.init(@frame());
        statics.waiters.pushBack(&continuation);

        rp2040.io_bank0.proc_inte.set(0, gpio / 8, @as(u32, 1) << (gpio % 8 * 4 + 1));
    }
}

pub fn yieldUntilFallingEdge(comptime gpio: u5) void {
    const statics = struct {
        var waiters = ContinuationQueue.init();
    };

    checkGpioAssignedToSio(gpio);

    comptime const spinlock = config.gpio[gpio].function.Sio.yield_until_falling_edge_spinlock.?;

    suspend {
        arm.disableInterrupts();
        defer arm.enableInterrupts();

        hardware_spinlock.lock(spinlock);
        defer hardware_spinlock.unlock(spinlock);

        var continuation = Continuation.init(@frame());
        statics.waiters.pushBack(&continuation);

        rp2040.io_bank0.proc_inte.set(0, gpio / 8, @as(u32, 1) << (gpio % 8 * 4 + 2));
    }
}

pub fn yieldUntilRisingEdge(comptime gpio: u5) void {
    const statics = struct {
        var waiters = ContinuationQueue.init();
    };

    checkGpioAssignedToSio(gpio);

    comptime const spinlock = config.gpio[gpio].function.Sio.yield_until_rising_edge_spinlock.?;

    suspend {
        arm.disableInterrupts();
        defer arm.enableInterrupts();

        hardware_spinlock.lock(spinlock);
        defer hardware_spinlock.unlock(spinlock);

        var continuation = Continuation.init(@frame());
        statics.waiters.pushBack(&continuation);

        rp2040.io_bank0.proc_inte.set(0, gpio / 8, @as(u32, 1) << (gpio % 8 * 4 + 3));
    }
}

pub fn processInterrupt() void {
    // FIXME: don't load all status registers unless we need all of them
    var interrupt_status: [rp2040.io_bank0.intr.len]u32 = undefined;

    for (interrupt_status) |*status, i| {
        status.* = rp2040.io_bank0.intr.read(i);
    }

    inline for (config.gpio) |gpio_config, gpio| {
        if (gpio_config.function != .Sio) {
            continue;
        }
    }
}
