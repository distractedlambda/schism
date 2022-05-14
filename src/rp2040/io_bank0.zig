const std = @import("std");

const PeripheralRegisterArray = @import("peripheral_register_array.zig").PeripheralRegisterArray;
const PeripheralRegisterMatrix = @import("peripheral_register_matrix.zig").PeripheralRegisterMatrix;

const base_address = 0x40014000;

pub const Override = enum(u2) {
    None,
    Invert,
    DriveLow,
    DriveHigh,
};

pub const Funcsel = enum(u5) {
    Spi = 1,
    Uart = 2,
    I2c = 3,
    Pwm = 4,
    Sio = 5,
    Pio0 = 6,
    Pio1 = 7,
    Clock = 8,
    Usb = 9,
    Null = 31,
    _,
};

pub const InterruptKind = enum(u2) {
    LevelLow,
    LevelHigh,
    EdgeLow,
    EdgeHigh,
};

pub fn interruptBitIndex(gpio: u5, kind: InterruptKind) u32 {
    std.debug.assert(gpio <= 30);
    return @as(u32, gpio) * 4 + @enumToInt(kind);
}

pub const gpio_status = struct {
    pub usingnamespace PeripheralRegisterArray(30, base_address, 0x8);

    // pub const irqtoproc = RegisterField(bool, 26);
    // pub const irqfrompad = RegisterField(bool, 24);
    // pub const intoperi = RegisterField(bool, 19);
    // pub const infrompad = RegisterField(bool, 17);
    // pub const oetopad = RegisterField(bool, 13);
    // pub const oefromperi = RegisterField(bool, 12);
    // pub const outtopad = RegisterField(bool, 9);
    // pub const outfromperi = RegisterField(bool, 8);
};

pub const gpio_ctrl = PeripheralRegisterArray(30, base_address + 0x004, 0x8, .{
    .{ .name = "irqover", .type = Override, .lsb = 28, .default = .None },
    .{ .name = "inover", .type = Override, .lsb = 16, .default = .None },
    .{ .name = "oeover", .type = Override, .lsb = 12, .default = .None },
    .{ .name = "outover", .type = Override, .lsb = 8, .default = .None },
    .{ .name = "funcsel", .type = Funcsel, .lsb = 0, .default = .Null },
});

pub const intr = PeripheralRegisterArray(4, base_address + 0x0f0, 0x4, u32);

pub const proc_inte = PeripheralRegisterMatrix(2, 4, base_address + 0x100, 0x30, 0x4, u32);
pub const proc_intf = PeripheralRegisterMatrix(2, 4, base_address + 0x110, 0x30, 0x4, u32);
pub const proc_ints = PeripheralRegisterMatrix(2, 4, base_address + 0x120, 0x30, 0x4, u32);

pub const dormant_wake_inte = PeripheralRegisterArray(4, base_address + 0x160, 0x4, u32);
pub const dormant_wake_intf = PeripheralRegisterArray(4, base_address + 0x170, 0x4, u32);
pub const dormant_wake_ints = PeripheralRegisterArray(4, base_address + 0x180, 0x4, u32);
