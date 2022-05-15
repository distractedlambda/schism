const bits = @import("../bits.zig");

const PeripheralRegister = @import("peripheral_register.zig").PeripheralRegister;
const PeripheralRegisterArray = @import("peripheral_register_array.zig").PeripheralRegisterArray;

const base_address = 0x4001c000;

pub const Voltage = enum(u1) {
    @"3.3V",
    @"1.8V",
};

pub const voltage_select = PeripheralRegister(base_address + 0x00, Voltage);

pub const DriveStrength = enum(u2) {
    @"2mA",
    @"4mA",
    @"8mA",
    @"12mA",
};

pub const SlewRate = enum(u1) {
    Slow,
    Fast,
};

const spec = bits.BitStructSpec{
    .Record = &[_]bits.BitStructField{
        .{
            .name = "od",
            .type = bool,
            .lsb = 7,
            .default = &false,
        },
        .{
            .name = "ie",
            .type = bool,
            .lsb = 6,
            .default = &true,
        },
        .{
            .name = "drive",
            .type = DriveStrength,
            .lsb = 4,
            .default = &DriveStrength.@"4mA",
        },
        .{
            .name = "pue",
            .type = bool,
            .lsb = 3,
            .default = &false,
        },
        .{
            .name = "pde",
            .type = bool,
            .lsb = 2,
            .default = &true,
        },
        .{
            .name = "schmitt",
            .type = bool,
            .lsb = 1,
            .default = &true,
        },
        .{
            .name = "slewfast",
            .type = SlewRate,
            .lsb = 0,
            .default = &SlewRate.Slow,
        },
    },
};

pub const gpio = PeripheralRegisterArray(30, base_address + 0x04, 0x04, spec);
pub const swclk = PeripheralRegister(base_address + 0x7c, spec);
pub const swd = PeripheralRegister(base_address + 0x80, spec);
