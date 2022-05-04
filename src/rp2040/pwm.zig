const PeripheralRegister = @import("PeripheralRegister.zig");
const PeripheralRegisterArray = @import("PeripheralRegisterArray.zig");
const RegisterField = @import("RegisterField.zig");

const base_address = 0x40050000;

pub const csr = struct {
    pub usingnamespace PeripheralRegisterArray(8, base_address + 0x00, 0x14);

    pub const Divmode = enum(u2) {
        FreeRunning,
        Level,
        RisingEdge,
        FallingEdge,
    };

    pub const ph_adv = RegisterField(bool, 7);
    pub const ph_ret = RegisterField(bool, 6);
    pub const divmode = RegisterField(Divmode, 4);
    pub const b_inv = RegisterField(bool, 3);
    pub const a_inv = RegisterField(bool, 2);
    pub const ph_correct = RegisterField(bool, 1);
    pub const en = RegisterField(bool, 0);
};

pub const div = struct {
    pub usingnamespace PeripheralRegisterArray(8, base_address + 0x04, 0x14);

    pub const int = RegisterField(u8, 4);
    pub const frac = RegisterField(u4, 0);
};

pub const ctr = struct {
    pub usingnamespace PeripheralRegisterArray(8, base_address + 0x08, 0x14);

    pub const value = RegisterField(u16, 0);
};

pub const cc = struct {
    pub usingnamespace PeripheralRegisterArray(8, base_address + 0x0c, 0x14);

    pub const b = RegisterField(u16, 16);
    pub const a = RegisterField(u16, 0);
};

pub const top = struct {
    pub usingnamespace PeripheralRegisterArray(8, base_address + 0x10, 0x14);

    pub const value = RegisterField(u16, 0);
};

pub const en = PeripheralRegister(base_address + 0xa0);

pub const intr = PeripheralRegister(base_address + 0xa4);

pub const inte = PeripheralRegister(base_address + 0xa8);

pub const intf = PeripheralRegister(base_address + 0xac);

pub const ints = PeripheralRegister(base_address + 0xb0);
