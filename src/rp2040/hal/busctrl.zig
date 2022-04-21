const mmio = @import("mmio.zig");

const base_address = 0x40030000;
const bus_priority_address = base_address + 0x00;
const performance_counters_base_address = base_address + 0x08;

pub const PerformanceEvent = enum(u5) {
    apb_contested,
    apb,
    fastperi_contested,
    fastperi,
    sram5_contested,
    sram5,
    sram4_contested,
    sram4,
    sram3_contested,
    sram3,
    sram2_contested,
    sram2,
    sram1_contested,
    sram1,
    sram0_contested,
    sram0,
    xip_main_contested,
    xip_main,
    rom_contested,
    rom,
};

pub const Priority = enum(u1) {
    low,
    high,

    fn toInt(self: @This()) u1 {
        return @enumToInt(self);
    }
};

pub const Priorities = struct {
    proc0: Priority,
    proc1: Priority,
    dma_r: Priority,
    dma_w: Priority,

    fn toInt(self: @This()) u32 {
        return self.proc0.toInt() | (self.proc1.toInt() << 4) | (self.dma_r.toInt() << 8) | (self.dma_w.toInt() << 12);
    }
};

pub fn setPriorities(priorities: Priorities) void {
    mmio.write(bus_priority_address, priorities.toInt());
}

pub fn readPerformanceCounter(index: u2) u24 {
    return @truncate(u24, mmio.read(performance_counters_base_address + index * 8));
}

pub fn clearPerformanceCounter(index: u2) void {
    mmio.write(performance_counters_base_address + index * 8, 0);
}

pub fn selectPerformanceEventForCounter(index: u2, event: PerformanceEvent) void {
    mmio.write(performance_counters_base_address + index * 8 + 4, @enumToInt(event));
}
