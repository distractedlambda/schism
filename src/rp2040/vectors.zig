const std = @import("std");

const bootrom = @import("bootrom.zig");

comptime {
    asm (@embedFile("vectors.S"));
}

export fn handleReset() noreturn {
    @setRuntimeSafety(false);

    // Zero out .bss
    const bss_start = @extern([*]volatile u32, .{ .name = "__bss_start__" });
    const bss_end = @extern([*]volatile u32, .{ .name = "__bss_end__" });
    for (bss_start[0 .. (@ptrToInt(bss_end) - @ptrToInt(bss_start)) / @sizeOf(u32)]) |*word| word.* = 0;

    // Initialize .data
    const data_source = @extern([*]const u32, .{ .name = "__data_source__" });
    const data_start = @extern([*]volatile u32, .{ .name = "__data_start__" });
    const data_end = @extern([*]volatile u32, .{ .name = "__data_end__" });
    for (data_start[0 .. (@ptrToInt(data_end) - @ptrToInt(data_start)) / @sizeOf(u32)]) |*word, i| word.* = data_source[i];

    // Link library routines from bootrom
    bootrom.link();

    // Call main()
    @import("root").main();
    @panic("return from main()");
}

export fn handleNmi() void {
    return;
}

export fn handleHardfault() void {
    @setRuntimeSafety(false);

    asm volatile (
        \\ 1: bkpt 0x0000
        \\    b.n 1b
    );

    unreachable;
}

export fn handleSvcall() void {
    return;
}

export fn handlePendsv() void {
    return;
}

export fn handleSystick() void {
    return;
}

export fn handleTimerIrq0() void {
    return;
}

export fn handleTimerIrq1() void {
    return;
}

export fn handleTimerIrq2() void {
    return;
}

export fn handleTimerIrq3() void {
    return;
}

export fn handlePwmIrqWrap() void {
    return;
}

export fn handleUsbctrlIrq() void {
    return;
}

export fn handleXipIrq() void {
    return;
}

export fn handlePio0Irq0() void {
    return;
}

export fn handlePio0Irq1() void {
    return;
}

export fn handlePio1Irq0() void {
    return;
}

export fn handlePio1Irq1() void {
    return;
}

export fn handleDmaIrq0() void {
    return;
}

export fn handleDmaIrq1() void {
    return;
}

export fn handleIoIrqBank0() void {
    return;
}

export fn handleIoIrqQspi() void {
    return;
}

export fn handleSioIrqProc0() void {
    return;
}

export fn handleSioIrqProc1() void {
    return;
}

export fn handleClocksIrq() void {
    return;
}

export fn handleSpi0Irq() void {
    return;
}

export fn handleSpi1Irq() void {
    return;
}

export fn handleUart0Irq() void {
    return;
}

export fn handleUart1Irq() void {
    return;
}

export fn handleAdcIrqFifo() void {
    return;
}

export fn handleI2c0Irq() void {
    return;
}

export fn handleI2c1Irq() void {
    return;
}

export fn handleRtcIrq() void {
    return;
}

export fn handleUnconnectedIrq() void {
    return;
}
