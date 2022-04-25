const registers = @import("../registers.zig");

const PeripheralRegister = registers.PeripheralRegister;
const PeripheralRegisterArray = registers.PeripheralRegisterArray;
const RegisterField = registers.RegisterField;

const base_address = 0x4001c000;

const voltage_select = struct {
    pub usingnamespace PeripheralRegister(base_address + 0x00);

    pub const Value = enum(u1) {
        @"3.3V",
        @"1.8V",
    };

    pub const value = RegisterField(value, 0);
};

const pad_common = struct {
    pub const Drive = enum(u2) {
        @"2mA",
        @"4mA",
        @"8mA",
        @"12mA"
    };

    pub const od = RegisterField(bool, 7);
    pub const ie = RegisterField(bool, 6);
    pub const drive = RegisterField(Drive, 4);
    pub const pue = RegisterField(bool, 3);
    pub const pde = RegisterField(bool, 2);
    pub const schmitt = RegisterField(bool, 1);
    pub const slewfast = RegisterField(bool, 0);
};

pub const gpio = struct {
    pub usingnamespace PeripheralRegisterArray(30, base_address + 0x04, 0x04);

    // FIXME switch over to usingnamespace once it no longer makes ZLS crash
    pub const Drive = pad_common.Drive;
    pub const od = pad_common.od;
    pub const ie = pad_common.ie;
    pub const drive = pad_common.drive;
    pub const pue = pad_common.pue;
    pub const pde = pad_common.pde;
    pub const schmitt = pad_common.schmitt;
    pub const slewfast = pad_common.slewfast;
};

pub const swclk = struct {
    pub usingnamespace PeripheralRegister(base_address + 0x7c);

    // FIXME switch over to usingnamespace once it no longer makes ZLS crash
    pub const Drive = pad_common.Drive;
    pub const od = pad_common.od;
    pub const ie = pad_common.ie;
    pub const drive = pad_common.drive;
    pub const pue = pad_common.pue;
    pub const pde = pad_common.pde;
    pub const schmitt = pad_common.schmitt;
    pub const slewfast = pad_common.slewfast;
};

pub const swd = struct {
    pub usingnamespace PeripheralRegister(base_address + 0x80);

    // FIXME switch over to usingnamespace once it no longer makes ZLS crash
    pub const Drive = pad_common.Drive;
    pub const od = pad_common.od;
    pub const ie = pad_common.ie;
    pub const drive = pad_common.drive;
    pub const pue = pad_common.pue;
    pub const pde = pad_common.pde;
    pub const schmitt = pad_common.schmitt;
    pub const slewfast = pad_common.slewfast;
};
