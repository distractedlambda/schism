const std = @import("std");

const registers = @import("../registers.zig");

const PeripheralRegisterArray = registers.PeripheralRegisterArray;
const PeripheralRegisterMatrix = registers.PeripheralRegisterMatrix;
const RegisterField = registers.RegisterField;

const base_address = 0x40014000;

pub const InterruptKind = enum(u2) {
    level_low,
    level_high,
    edge_low,
    edge_high,
};

pub fn interruptBitIndex(gpio: u5, kind: InterruptKind) u32 {
    std.debug.assert(gpio <= 30);
    return @as(u32, gpio) * 4 + @enumToInt(kind);
}

pub const gpio_status = struct {
    pub usingnamespace PeripheralRegisterArray(30, base_address, 0x8);

    pub const irqtoproc = RegisterField(bool, 26);
    pub const irqfrompad = RegisterField(bool, 24);
    pub const intoperi = RegisterField(bool, 19);
    pub const infrompad = RegisterField(bool, 17);
    pub const oetopad = RegisterField(bool, 13);
    pub const oefromperi = RegisterField(bool, 12);
    pub const outtopad = RegisterField(bool, 9);
    pub const outfromperi = RegisterField(bool, 8);
};

pub const gpio_ctrl = struct {
    pub usingnamespace PeripheralRegisterArray(30, base_address + 0x004, 0x8);

    pub const Override = enum(u2) {
        none,
        invert,
        drive_low,
        drive_high,
    };

    pub const irqover = RegisterField(Override, 28);
    pub const inover = RegisterField(Override, 16);
    pub const oeover = RegisterField(Override, 12);
    pub const outover = RegisterField(Override, 8);
    pub const funcsel = RegisterField(u5, 0);
};

pub const intr = PeripheralRegisterArray(4, base_address + 0x0f0, 0x4);

pub const proc_inte = PeripheralRegisterMatrix(2, 30, base_address + 0x100, 0x30, 0x4);

pub const proc_intf = PeripheralRegisterMatrix(2, 30, base_address + 0x110, 0x30, 0x4);

pub const proc_ints = PeripheralRegisterMatrix(2, 30, base_address + 0x120, 0x30, 0x4);

pub const dormant_wake_inte = PeripheralRegisterArray(30, base_address + 0x160, 0x4);

pub const dormant_wake_intf = PeripheralRegisterArray(30, base_address + 0x170, 0x4);

pub const dormant_wake_ints = PeripheralRegisterArray(30, base_address + 0x180, 0x4);
