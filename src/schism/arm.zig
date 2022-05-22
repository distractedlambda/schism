pub inline fn dataMemoryBarrier() void {
    asm volatile ("dmb" ::: "memory");
}

pub inline fn dataSynchronizationBarrier() void {
    asm volatile ("dsb" ::: "memory");
}

pub inline fn instructionSynchronizationBarrier() void {
    asm volatile ("isb");
}

pub inline fn disableInterrupts() void {
    asm volatile ("cpsid i" ::: "memory");
}

pub inline fn enableInterrupts() void {
    asm volatile ("cpsie i" ::: "memory");
}

pub inline fn waitForEvent() void {
    asm volatile ("wfe" ::: "memory");
}

pub inline fn waitForInterrupt() void {
    asm volatile ("wfi" ::: "memory");
}

pub inline fn nop() void {
    asm volatile ("nop");
}
