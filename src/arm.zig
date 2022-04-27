pub fn dmb() void {
    asm volatile ("dmb" ::: "memory");
}

pub fn dsb() void {
    asm volatile ("dsb" ::: "memory");
}

pub fn isb() void {
    asm volatile ("isb");
}
