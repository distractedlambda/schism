pub fn dataMemoryBarrier() void {
    asm volatile ("dmb" ::: "memory");
}

pub fn dataSynchronizationBarrier() void {
    asm volatile ("dsb" ::: "memory");
}

pub fn instructionSynchronizationBarrier() void {
    asm volatile ("isb");
}

pub fn disableInterrupts() void {
    asm volatile ("cpsid i");
}

pub fn enableInterrupts() void {
    asm volatile ("cpsie i");
}

pub fn waitForEvent() void {
    asm volatile ("wfe" ::: "memory");
}

pub fn waitForInterrupt() void {
    asm volatile ("wfi" ::: "memory");
}
