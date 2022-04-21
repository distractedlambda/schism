const mmio = @import("mmio.zig");

const base_address = 0xd0000000;

pub fn cpuid() u1 {
    return @truncate(u1, mmio.read(base_address + 0x000));
}

pub const gpio = struct {
    pub fn read() u30 {
        return @truncate(u30, mmio.read(base_address + 0x004));
    }

    pub fn readHigh() u6 {
        return @truncate(u6, mmio.read(base_address + 0x008));
    }

    pub fn write(bits: u30) void {
        mmio.write(base_address + 0x010, bits);
    }

    pub fn set(mask: u30) void {
        mmio.write(base_address + 0x014, mask);
    }

    pub fn clear(mask: u30) void {
        mmio.write(base_address + 0x018, mask);
    }

    pub fn toggle(mask: u30) void {
        mmio.write(base_address + 0x01c, mask);
    }

    pub fn setWriteEnable(bits: u30) void {
        mmio.write(base_address + 0x020, bits);
    }

    pub fn writeEnable(mask: u30) void {
        mmio.write(base_address + 0x024, mask);
    }

    pub fn writeDisable(mask: u30) void {
        mmio.write(base_address + 0x028, mask);
    }

    pub fn toggleWriteEnable(mask: u30) void {
        mmio.write(base_address + 0x02c, mask);
    }
};

pub const fifo = struct {
    pub const Status = struct {
        valid: bool,
        ready: bool,
        write_overflow: bool,
        read_on_empty: bool,
    };

    pub fn status() Status {
        const bits = mmio.read(base_address + 0x050);
        return .{
            .valid = (bits & 0x0) != 0,
            .ready = (bits & 0x1) != 0,
            .write_overflow = (bits & 0x2) != 0,
            .read_on_empty = (bits & 0x4) != 0,
        };
    }

    pub fn write(value: u32) void {
        mmio.write(base_address + 0x054, value);
    }

    pub fn read() u32 {
        return mmio.read(base_address + 0x058);
    }
};

pub const spinlock = struct {
    pub fn state() u32 {
        return mmio.read(base_address + 0x05c);
    }

    pub fn tryLock(lock: u5) bool {
        return mmio.read(base_address + 0x100 + lock * 0x4) != 0;
    }

    pub fn unlock(lock: u5) void {
        mmio.write(base_address + 0x100 + lock * 0x4, 0);
    }
};

pub const divider = struct {
    pub const Status = struct {
        ready: bool,
        dirty: bool,
    };

    pub fn status() Status {
        const bits = mmio.read(base_address + 0x078);
        return .{
            .ready = (bits & 0x1) != 0,
            .dirty = (bits & 0x2) != 0,
        };
    }

    pub fn setUnsignedDividend(value: u32) void {
        mmio.write(base_address + 0x060, value);
    }

    pub fn setUnsignedDivisor(value: u32) void {
        mmio.write(base_address + 0x064, value);
    }

    pub fn setSignedDividend(value: i32) void {
        mmio.write(base_address + 0x068, @bitCast(u32, value));
    }

    pub fn setSignedDivisor(value: i32) void {
        mmio.write(base_address + 0x06c, @bitCast(u32, value));
    }

    pub fn readUnsignedQuotient() u32 {
        return mmio.read(base_address + 0x070);
    }

    pub fn readSignedQuotient() i32 {
        return @bitCast(i32, readUnsignedQuotient());
    }

    pub fn readUnsignedRemainder() u32 {
        return mmio.read(base_address + 0x074);
    }

    pub fn readSignedRemainder() i32 {
        return @bitCast(i32, readUnsignedRemainder());
    }
};
