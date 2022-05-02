const PeripheralRegister = @import("peripheral_register.zig").PeripheralRegister;
const RegisterField = @import("register_field.zig").RegisterField;

const base_address = 0x40024000;

pub const ctrl = struct {
    pub usingnamespace PeripheralRegister(base_address + 0x00);

    pub const Enable = enum(u12) {
        Disable = 0xd1e,
        Enable = 0xfab,
    };

    pub const FreqRange = enum(u12) {
        @"1_15mhz" = 0xaa0,
    };

    pub const enable = RegisterField(Enable, 12);
    pub const freq_range = RegisterField(FreqRange, 0);
};

pub const status = struct {
    pub usingnamespace PeripheralRegister(base_address + 0x04);

    pub const stable = RegisterField(bool, 31);
    pub const badwrite = RegisterField(bool, 24);
    pub const enabled = RegisterField(bool, 12);
};

pub const dormant = struct {
    pub usingnamespace PeripheralRegister(base_address + 0x08);

    pub const dormant = 0x636f6d61;
    pub const wake = 0x77616b65;
};

pub const startup = struct {
    pub usingnamespace PeripheralRegister(base_address + 0x0c);

    pub const x4 = RegisterField(bool, 20);
    pub const delay = RegisterField(u14, 0);
};

pub const count = PeripheralRegister(base_address + 0x1c);
