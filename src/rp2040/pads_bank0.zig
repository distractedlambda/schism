const PeripheralRegister = @import("peripheral_register.zig").PeripheralRegister;
const PeripheralRegisterArray = @import("peripheral_register_array.zig").PeripheralRegisterArray;
const RegisterField = @import("register_field.zig").RegisterField;

const base_address = 0x4001c000;

pub const Voltage = enum(u1) {
    @"3.3V",
    @"1.8V",
};

pub const DriveStrength = enum(u2) {
    @"2mA",
    @"4mA",
    @"8mA",
    @"12mA"
};

pub const od = RegisterField(bool, 7);
pub const ie = RegisterField(bool, 6);
pub const drive = RegisterField(DriveStrength, 4);
pub const pue = RegisterField(bool, 3);
pub const pde = RegisterField(bool, 2);
pub const schmitt = RegisterField(bool, 1);
pub const slewfast = RegisterField(bool, 0);

pub const voltage_select = PeripheralRegister(base_address + 0x00);

pub const gpio = PeripheralRegisterArray(30, base_address + 0x04, 0x04);
pub const swclk = PeripheralRegister(base_address + 0x7c);
pub const swd = PeripheralRegister(base_address + 0x80);
