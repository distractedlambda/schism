const std = @import("std");

const arm = @import("arm.zig");
const config = @import("Config.zig").resolved;
const executor = @import("executor.zig");
const gpio_waiters = @import("gpio_waiters.zig");
const multicore = @import("multicore.zig");
const resets = @import("resets.zig");
const rp2040 = @import("rp2040.zig");

const Continuation = executor.Continuation;
const ContinuationQueue = executor.ContinuationQueue;
const CoreLocal = multicore.CoreLocal;

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
        gpio_waiters.yield_until_low[gpio].ptr().pushBack(&continuation);
        rp2040.io_bank0.proc_inte.setRaw(multicore.currentCore(), gpio / 8, @as(u32, 1) << (gpio % 8 * 4));
    }
}

pub fn yieldUntilHigh(comptime gpio: u5) void {
    comptime checkGpioAssignedToSio(gpio);

    if (!config.gpio[gpio].function.Sio.allow_yield_until_high) {
        comptime @compileError(std.fmt.comptimePrint("yieldUntilHigh is not enabled for GPIO {}", .{gpio}));
    }

    var continuation = Continuation.init(@frame());

    suspend {
        gpio_waiters.yield_until_high[gpio].ptr().pushBack(&continuation);
        rp2040.io_bank0.proc_inte.setRaw(multicore.currentCore(), gpio / 8, @as(u32, 1) << (gpio % 8 * 4 + 1));
    }
}

pub fn handleIrq() callconv(.C) void {
    arm.disableInterrupts();
    defer arm.enableInterrupts();

    // FIXME: don't load all status registers unless we need all of them
    var interrupt_status: [rp2040.io_bank0.intr.len]u32 = undefined;

    for (interrupt_status) |*status, i| {
        status.* = rp2040.io_bank0.proc_ints.read(multicore.currentCore(), i);
        rp2040.io_bank0.proc_inte.clearRaw(multicore.currentCore(), i, status.*);
    }

    inline for (config.gpio) |gpio_config, gpio_num| {
        if (gpio_config.function != .Sio) {
            continue;
        }

        if (gpio_config.function.Sio.allow_yield_until_low) {
            if (@truncate(u1, interrupt_status[gpio_num / 8] >> ((gpio_num % 8) * 4)) != 0) {
                executor.submitAll(gpio_waiters.yield_until_low[gpio_num].ptr());
            }
        }

        if (gpio_config.function.Sio.allow_yield_until_high) {
            if (@truncate(u1, interrupt_status[gpio_num / 8] >> ((gpio_num % 8) * 4 + 1)) != 0) {
                executor.submitAll(gpio_waiters.yield_until_high[gpio_num].ptr());
            }
        }
    }
}

pub fn init() void {
    resets.unreset(.{ .pads_bank0 = true, .io_bank0 = true });

    inline for (config.gpio) |gpio_config, gpio_num| {
        const pads_gpio = rp2040.pads_bank0.gpio.Bits.Unpacked{
            .od = !gpio_config.output_enabled,
            .ie = gpio_config.input_enabled,
            .drive = gpio_config.drive_strength,
            .pue = gpio_config.pull_up_enabled,
            .pde = gpio_config.pull_down_enabled,
            .schmitt = gpio_config.schmitt_trigger_enabled,
            .slewfast = gpio_config.slew_rate,
        };

        if (comptime !std.meta.eql(pads_gpio, .{})) {
            rp2040.pads_bank0.gpio.write(gpio_num, pads_gpio);
        }

        const gpio_ctrl = rp2040.io_bank0.gpio_ctrl.Bits.Unpacked{
            .irqover = .None,
            .inover = gpio_config.input_override,
            .oeover = gpio_config.output_enable_override,
            .outover = gpio_config.output_override,
            .funcsel = comptime gpio_config.function.funcsel(),
        };

        if (comptime !std.meta.eql(gpio_ctrl, .{})) {
            rp2040.io_bank0.gpio_ctrl.write(gpio_num, gpio_ctrl);
        }
    }

    rp2040.ppb.nvic_iser.write(.{ .io_bank0 = true });
}
