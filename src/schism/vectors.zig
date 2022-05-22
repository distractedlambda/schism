const std = @import("std");

const arm = @import("arm.zig");
const bootrom = @import("bootrom.zig");
const config = @import("config.zig").resolved;
const executor = @import("executor.zig");
const gpio = @import("gpio.zig");
const resets = @import("resets.zig");
const rp2040 = @import("rp2040.zig");
const usb = @import("usb.zig");

comptime {
    @export(vector_table, .{ .name = "__vectors", .section = ".vectors" });
    @export(vector_table, .{ .name = "__VECTOR_TABLE", .section = ".vectors" });
}

pub var vector_table = VectorTable{
    .stack_top = config.core0_stack_top,
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

    // Start up external oscillator
    rp2040.xosc.startup.write(.{ .delay = 47 });
    rp2040.xosc.ctrl.write(.{ .enable = .Enable });
    while (!rp2040.xosc.status.read().stable) {}

    // Switch reference clock to external oscillator
    rp2040.clocks.clk_ref_ctrl.write(.{ .src = .XoscClksrc });
    while (!rp2040.clocks.clk_ref_selected.read().xosc_clksrc) {}

    // Start up system PLL, targeting 125 MHz
    resets.unreset(.{ .pll_sys = true });
    rp2040.pll_sys.fbdiv_int.write(125);
    rp2040.pll_sys.pwr.clear(.{ .pd = true, .vcopd = true });
    while (!rp2040.pll_sys.cs.read().lock) {}
    rp2040.pll_sys.prim.write(.{ .postdiv1 = 6, .postdiv2 = 2 });
    rp2040.pll_sys.pwr.clear(.{ .postdivpd = true });

    // Switch system clock to PLL
    rp2040.clocks.clk_sys_control.write(.{ .src = .ClksrcClkSysAux });
    while (!rp2040.clocks.clk_sys_selected.read().clksrc_clk_sys_aux) {}

    // Power down ring oscillator
    rp2040.rosc.ctrl.write(.{ .enable = .Disable });

    // Zero out .bss
    const bss_start = @extern([*]volatile u32, .{ .name = "__bss_start__" });
    const bss_end = @extern([*]volatile u32, .{ .name = "__bss_end__" });
    for (bss_start[0 .. (@ptrToInt(bss_end) - @ptrToInt(bss_start)) / @sizeOf(u32)]) |*word| word.* = 0;

    // Initialize .data
    const data_source = @extern([*]const u32, .{ .name = "__data_source__" });
    const data_start = @extern([*]volatile u32, .{ .name = "__data_start__" });
    const data_end = @extern([*]volatile u32, .{ .name = "__data_end__" });
    for (data_start[0 .. (@ptrToInt(data_end) - @ptrToInt(data_start)) / @sizeOf(u32)]) |*word, i| word.* = data_source[i];

    // Link bootrom routines
    // FIXME: do we need to do this even earlier, for safety? The danger is a
    // call to one of those late-linked functions being emitted for code that
    // runs above.
    bootrom.init();

    // Initialize peripherals
    gpio.init();
    usb.init();

    // Start running user code
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
