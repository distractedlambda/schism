const registers = @import("../registers.zig");

const PeripheralRegister = registers.PeripheralRegister;
const RegisterField = registers.RegisterField;

const base_address = 0x40010000;

const common = struct {
    pub const proc1 = RegisterField(bool, 16);
    pub const proc0 = RegisterField(bool, 15);
    pub const sio = RegisterField(bool, 14);
    pub const vreg_and_chip_reset = RegisterField(bool, 13);
    pub const xip = RegisterField(bool, 12);
    pub const sram5 = RegisterField(bool, 11);
    pub const sram4 = RegisterField(bool, 10);
    pub const sram3 = RegisterField(bool, 9);
    pub const sram2 = RegisterField(bool, 8);
    pub const sram1 = RegisterField(bool, 7);
    pub const sram0 = RegisterField(bool, 6);
    pub const rom = RegisterField(bool, 5);
    pub const busfabric = RegisterField(bool, 4);
    pub const resets = RegisterField(bool, 3);
    pub const clocks = RegisterField(bool, 2);
    pub const xosc = RegisterField(bool, 1);
    pub const rosc = RegisterField(bool, 0);
};

pub const frce_on = struct {
    pub usingnamespace PeripheralRegister(base_address + 0x0);
    pub usingnamespace common;
};

pub const frce_off = struct {
    pub usingnamespace PeripheralRegister(base_address + 0x4);
    pub usingnamespace common;
};

pub const wdsel = struct {
    pub usingnamespace PeripheralRegister(base_address + 0x8);
    pub usingnamespace common;
};

pub const done = struct {
    pub usingnamespace PeripheralRegister(base_address + 0xc);
    pub usingnamespace common;
};
