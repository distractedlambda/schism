const std = @import("std");

const arm = @import("../arm.zig");
const bits = @import("../bits.zig");
const config = @import("config.zig");
const core_local = @import("core_local.zig");
const executor = @import("executor.zig");
const gpio = @import("gpio.zig");
const usb = @import("usb.zig");
const rp2040 = @import("../rp2040/rp2040.zig");
const schism = @import("schism.zig");

comptime {
    @export(vector_table, .{ .name = "__vectors", .section = ".vectors" });
    @export(vector_table, .{ .name = "__VECTOR_TABLE", .section = ".vectors" });
}

pub var vector_table = VectorTable{
    .stack_top = @ptrToInt(config.core0_stack.ptr + config.core0_stack.len),
    .reset = handleReset,
    .nmi = handleNmi,
    .hardfault = handleHardfault,
    .svcall = handleSvcall,
    .pendsv = handlePendsv,
    .systick = handleSystick,
    .irq0 = handleTimerIrq0,
    .irq1 = handleTimerIrq1,
    .irq2 = handleTimerIrq2,
    .irq3 = handleTimerIrq3,
    .irq4 = handlePwmIrqWrap,
    .irq5 = usb.handleIrq,
    .irq6 = handleXipIrq,
    .irq7 = handlePio0Irq0,
    .irq8 = handlePio0Irq1,
    .irq9 = handlePio1Irq0,
    .irq10 = handlePio1Irq1,
    .irq11 = handleDmaIrq0,
    .irq12 = handleDmaIrq1,
    .irq13 = gpio.handleIrq,
    .irq14 = handleIoIrqQspi,
    .irq15 = handleSioIrqProc0,
    .irq16 = handleSioIrqProc1,
    .irq17 = handleClocksIrq,
    .irq18 = handleSpi0Irq,
    .irq19 = handleSpi1Irq,
    .irq20 = handleUart0Irq,
    .irq21 = handleUart1Irq,
    .irq22 = handleAdcIrqFifo,
    .irq23 = handleI2c0Irq,
    .irq24 = handleI2c1Irq,
    .irq25 = handleRtcIrq,
    .irq26 = handleUnconnectedIrq,
    .irq27 = handleUnconnectedIrq,
    .irq28 = handleUnconnectedIrq,
    .irq29 = handleUnconnectedIrq,
    .irq30 = handleUnconnectedIrq,
    .irq31 = handleUnconnectedIrq,
};

var core0_frame: @Frame(@import("root").main) = undefined;

fn handleReset() callconv(.C) noreturn {
    arm.disableInterrupts();

    // Zero out .bss
    const bss_start = @extern([*]volatile u32, .{ .name = "__bss_start__" });
    const bss_end = @extern([*]volatile u32, .{ .name = "__bss_end__" });
    for (bss_start[0 .. (@ptrToInt(bss_end) - @ptrToInt(bss_start)) / @sizeOf(u32)]) |*word| word.* = 0;

    // Initialize .data
    const data_source = @extern([*]const u32, .{ .name = "__data_source__" });
    const data_start = @extern([*]volatile u32, .{ .name = "__data_start__" });
    const data_end = @extern([*]volatile u32, .{ .name = "__data_end__" });
    for (data_start[0 .. (@ptrToInt(data_end) - @ptrToInt(data_start)) / @sizeOf(u32)]) |*word, i| word.* = data_source[i];

    // Bring basic peripherals out of reset
    const reset_mask = rp2040.resets.Bits.mask(.{ .pads_bank0, .io_bank0 });
    rp2040.resets.reset.clearRaw(reset_mask);
    while (rp2040.resets.reset_done.readRaw() & reset_mask != reset_mask) {}

    // Initialize GPIOs
    inline for (config.gpio) |gpio_config, gpio_num| {
        const pads_gpio = rp2040.pads_bank0.gpio.Bits.Fields{
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

        const gpio_ctrl = rp2040.io_bank0.gpio_ctrl.Bits.Fields{
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

    // Start running user code
    arm.enableInterrupts();
    core0_frame = async @import("root").main();
    executor.run();
}

fn handleNmi() callconv(.C) void {
    return;
}

fn handleHardfault() callconv(.C) void {
    asm volatile (
        \\ 1: bkpt 0x0000
        \\    b.n 1b
    );

    unreachable;
}

fn handleSvcall() callconv(.C) void {
    return;
}

fn handlePendsv() callconv(.C) void {
    return;
}

fn handleSystick() callconv(.C) void {
    return;
}

fn handleTimerIrq0() callconv(.C) void {
    return;
}

fn handleTimerIrq1() callconv(.C) void {
    return;
}

fn handleTimerIrq2() callconv(.C) void {
    return;
}

fn handleTimerIrq3() callconv(.C) void {
    return;
}

fn handlePwmIrqWrap() callconv(.C) void {
    return;
}

fn handleXipIrq() callconv(.C) void {
    return;
}

fn handlePio0Irq0() callconv(.C) void {
    return;
}

fn handlePio0Irq1() callconv(.C) void {
    return;
}

fn handlePio1Irq0() callconv(.C) void {
    return;
}

fn handlePio1Irq1() callconv(.C) void {
    return;
}

fn handleDmaIrq0() callconv(.C) void {
    return;
}

fn handleDmaIrq1() callconv(.C) void {
    return;
}

fn handleIoIrqQspi() callconv(.C) void {
    return;
}

fn handleSioIrqProc0() callconv(.C) void {
    return;
}

fn handleSioIrqProc1() callconv(.C) void {
    return;
}

fn handleClocksIrq() callconv(.C) void {
    return;
}

fn handleSpi0Irq() callconv(.C) void {
    return;
}

fn handleSpi1Irq() callconv(.C) void {
    return;
}

fn handleUart0Irq() callconv(.C) void {
    return;
}

fn handleUart1Irq() callconv(.C) void {
    return;
}

fn handleAdcIrqFifo() callconv(.C) void {
    return;
}

fn handleI2c0Irq() callconv(.C) void {
    return;
}

fn handleI2c1Irq() callconv(.C) void {
    return;
}

fn handleRtcIrq() callconv(.C) void {
    return;
}

fn handleUnconnectedIrq() callconv(.C) void {
    return;
}

const VectorTable = extern struct {
    stack_top: usize,
    reset: fn () callconv(.C) void,
    nmi: fn () callconv(.C) void,
    hardfault: fn () callconv(.C) void,
    reserved_3: fn () callconv(.C) void = reservedVector,
    reserved_4: fn () callconv(.C) void = reservedVector,
    reserved_5: fn () callconv(.C) void = reservedVector,
    reserved_6: fn () callconv(.C) void = reservedVector,
    reserved_7: fn () callconv(.C) void = reservedVector,
    reserved_8: fn () callconv(.C) void = reservedVector,
    reserved_9: fn () callconv(.C) void = reservedVector,
    svcall: fn () callconv(.C) void,
    reserved_11: fn () callconv(.C) void = reservedVector,
    reserved_12: fn () callconv(.C) void = reservedVector,
    pendsv: fn () callconv(.C) void,
    systick: fn () callconv(.C) void,
    irq0: fn () callconv(.C) void,
    irq1: fn () callconv(.C) void,
    irq2: fn () callconv(.C) void,
    irq3: fn () callconv(.C) void,
    irq4: fn () callconv(.C) void,
    irq5: fn () callconv(.C) void,
    irq6: fn () callconv(.C) void,
    irq7: fn () callconv(.C) void,
    irq8: fn () callconv(.C) void,
    irq9: fn () callconv(.C) void,
    irq10: fn () callconv(.C) void,
    irq11: fn () callconv(.C) void,
    irq12: fn () callconv(.C) void,
    irq13: fn () callconv(.C) void,
    irq14: fn () callconv(.C) void,
    irq15: fn () callconv(.C) void,
    irq16: fn () callconv(.C) void,
    irq17: fn () callconv(.C) void,
    irq18: fn () callconv(.C) void,
    irq19: fn () callconv(.C) void,
    irq20: fn () callconv(.C) void,
    irq21: fn () callconv(.C) void,
    irq22: fn () callconv(.C) void,
    irq23: fn () callconv(.C) void,
    irq24: fn () callconv(.C) void,
    irq25: fn () callconv(.C) void,
    irq26: fn () callconv(.C) void,
    irq27: fn () callconv(.C) void,
    irq28: fn () callconv(.C) void,
    irq29: fn () callconv(.C) void,
    irq30: fn () callconv(.C) void,
    irq31: fn () callconv(.C) void,
};

fn reservedVector() callconv(.C) void {
    @panic("called from reserved entry in vector table");
}
