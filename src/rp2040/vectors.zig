const std = @import("std");

const bootrom = @import("bootrom.zig");

comptime {
    asm (@embedFile("vectors.S"));
}

extern fn main() void;

export fn handleReset() noreturn {
    @setRuntimeSafety(false);

    // Zero out .bss
    const bss_start = @ptrCast([*]volatile u32, @extern(u32, .{ .name = "__bss_start__" }));
    const bss_end = @ptrCast([*]volatile u32, @extern(u32, .{ .name = "__bss_end__" }));
    for (bss_start[0 .. bss_end - bss_start]) |*word| word.* = 0;

    // Initialize .data
    const data_source = @ptrCast([*]const u32, @extern(u32, .{ .name = "__etext" }));
    const data_start = @ptrCast([*]volatile u32, @extern(u32, .{ .name = "__data_start__" }));
    const data_end = @ptrCast([*]volatile u32, @extern(u32, .{ .name = "__data_end__" }));
    for (data_start[0 .. data_end - data_start]) |*word, i| word.* = data_source[i];

    // Link library routines from bootrom
    bootrom.link();

    main();

    @panic("return from main()");
}

export fn handleNmi() void {
    return;
}

export fn handleHardfault() void {
    return;
}

export fn handleSvcall() void {
    return;
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

export fn handleAdcI2c0Irq() void {
    return;
}

export fn handleAdcI2c1Irq() void {
    return;
}

export fn handleRtcIrq() void {
    return;
}

export fn handleUnconnectedIrq() void {
    return;
}
