const bits = @import("bits.zig");
const mmio = @import("mmio.zig");

const base_address = 0x40024000;

pub const Status = struct {
    stable: bool,
    badwrite: bool,
    enabled: bool,
};

pub fn enable() void {
    mmio.write(base_address, 0xfab << 12);
}

pub fn disable() void {
    mmio.write(base_address, 0xd1e << 12);
}

pub fn status() Status {
    const value = mmio.read(base_address + 0x04);
    return .{
        .stable = bits.get(value, 31),
        .badwrite = bits.get(value, 24),
        .enabled = bits.get(value, 12),
    };
}
