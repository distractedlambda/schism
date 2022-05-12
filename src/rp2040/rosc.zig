const PeripheralRegister = @import("peripheral_register.zig").PeripheralRegister;

const base_address = 0x40060000;

pub const ctrl = PeripheralRegister(base_address + 0x00, .{
    .{
        .name = "enable",
        .type = enum(u12) { Disable = 0xd1e, Enable = 0xfab },
        .lsb = 12,
        .default = .Enable,
    },
    .{
        .name = "freq_range",
        .type = enum(u12) { Low = 0xfa4, Medium = 0xfa5, High = 0xfa7, TooHigh = 0xfa6 },
        .lsb = 0,
        .default = .Low,
    },
});
