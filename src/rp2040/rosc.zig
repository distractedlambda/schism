const bits = @import("../bits.zig");

const PeripheralRegister = @import("peripheral_register.zig").PeripheralRegister;

const base_address = 0x40060000;

pub const Enable = enum(u12) {
    Disable = 0xd1e,
    Enable = 0xfab,
    _,
};

pub const FreqRange = enum(u12) {
    Low = 0xfa4,
    Medium = 0xfa5,
    High = 0xfa7,
    TooHigh = 0xfa6,
    _,
};

pub const ctrl = PeripheralRegister(base_address + 0x00, .{
    .Record = &[_]bits.BitStructField{
        .{
            .name = "enable",
            .type = Enable,
            .lsb = 12,
            .default = &Enable.Enable,
        },
        .{
            .name = "freq_range",
            .type = FreqRange,
            .lsb = 0,
            .default = &FreqRange.Low,
        },
    },
});
