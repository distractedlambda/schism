const PeripheralRegister = @import("peripheral_register.zig").PeripheralRegister;

const base_address = 0x40024000;

pub const ctrl = PeripheralRegister(base_address + 0x00, .{
    .{
        .name = "enable",
        .type = enum(u12) { Disable = 0xd1e, Enable = 0xfab, _ },
        .lsb = 12,
        .default = .Enable,
    },
    .{
        .name = "freq_range",
        .type = enum(u12) { @"1_15MHz" = 0xaa0, _ },
        .lsb = 0,
        .default = .@"1_15MHz",
    },
});

pub const status = PeripheralRegister(base_address + 0x04, .{
    .{
        .name = "stable",
        .type = bool,
        .lsb = 31,
    },
    .{
        .name = "badwrite",
        .type = bool,
        .lsb = 24,
    },
    .{
        .name = "enabled",
        .type = bool,
        .lsb = 12,
    },
    .{
        .name = "freq_range",
        .type = enum(u2) { @"1_15MHz", _ }, // FIXME: docs say this always reads as 0
        .lsb = 0,
    },
});

pub const dormant = PeripheralRegister(base_address + 0x08, enum(u32) {
    Dormant = 0x636f6d61,
    Wake = 0x77616b65,
});

pub const startup = PeripheralRegister(base_address + 0x0c, .{
    .{
        .name = "x4",
        .type = bool,
        .lsb = 20,
        .default = false,
    },
    .{
        .name = "delay",
        .type = u14,
        .lsb = 0,
        .default = 0x00c4,
    },
});

pub const count = PeripheralRegister(base_address + 0x1c, u8);
