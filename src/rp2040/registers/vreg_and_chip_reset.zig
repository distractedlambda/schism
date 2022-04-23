const registers = @import("../registers.zig");

const PeripheralRegister = registers.PeripheralRegister;
const RegisterField = registers.RegisterField;

const base_address = 0x40064000;

pub const vreg = struct {
    pub usingnamespace PeripheralRegister(base_address + 0x0);

    pub const rok = RegisterField(bool, 12);
    pub const vsel = RegisterField(u4, 4);
    pub const hiz = RegisterField(bool, 1);
    pub const en = RegisterField(bool, 0);
};

pub const bod = struct {
    pub usingnamespace PeripheralRegister(base_address + 0x4);

    pub const vsel = RegisterField(u4, 4);
    pub const en = RegisterField(bool, 0);
};

pub const chip_reset = struct {
    pub usingnamespace PeripheralRegister(base_address + 0x8);

    pub const psm_restart_flag = RegisterField(bool, 24);
    pub const had_psm_restart = RegisterField(bool, 20);
    pub const had_run = RegisterField(bool, 16);
    pub const had_por = RegisterField(bool, 8);
};
