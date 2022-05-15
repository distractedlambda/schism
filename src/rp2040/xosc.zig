const bits = @import("../bits.zig");

const PeripheralRegister = @import("peripheral_register.zig").PeripheralRegister;

const base_address = 0x40024000;

pub const CtrlEnable = enum(u12) {
    Disable = 0xd1e,
    Enable = 0xfab,
    _,
};

pub const CtrlFreqRange = enum(u12) {
    @"1_15MHz" = 0xaa0,
    _,
};

pub const ctrl = PeripheralRegister(base_address + 0x00, .{
    .Record = &[_]bits.BitStructField{
        .{
            .name = "enable",
            .type = CtrlEnable,
            .lsb = 12,
            .default = &CtrlEnable.Enable,
        },
        .{
            .name = "freq_range",
            .type = CtrlFreqRange,
            .lsb = 0,
            .default = &CtrlFreqRange.@"1_15MHz",
        },
    },
});

// FIXME: docs say this always reads as 0, but it doesn't seem to
pub const StatusFreqRange = enum(u2) {
    @"1_15MHz",
    _,
};

pub const status = PeripheralRegister(base_address + 0x04, .{
    .Record = &[_]bits.BitStructField{
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
            .type = StatusFreqRange,
            .lsb = 0,
        },
    },
});

pub const dormant = PeripheralRegister(base_address + 0x08, .{
    .Scalar = enum(u32) {
        Dormant = 0x636f6d61,
        Wake = 0x77616b65,
    },
});

pub const startup = PeripheralRegister(base_address + 0x0c, .{
    .Record = &[_]bits.BitStructField{
        .{
            .name = "x4",
            .type = bool,
            .lsb = 20,
            .default = &false,
        },
        .{
            .name = "delay",
            .type = u14,
            .lsb = 0,
            .default = &@as(u14, 0x00c4),
        },
    },
});

pub const count = PeripheralRegister(base_address + 0x1c, .{ .Scalar = u8 });
