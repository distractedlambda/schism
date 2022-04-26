const PeripheralRegister = @import("peripheral_register.zig").PeripheralRegister;
const RegisterField = @import("register_field.zig").RegisterField;

const base_address = 0x40060000;

pub const ctrl = struct {
    pub usingnamespace PeripheralRegister(base_address + 0x00);

    pub const Enable = enum(u12) {
        disable = 0xd1e,
        enable = 0xfab,
    };

    pub const FreqRange = enum(u12) {
        low = 0xfa4,
        medium = 0xfa5,
        high = 0xfa7,
        toohigh = 0xfa6,
    };

    pub const enable = RegisterField(Enable, 12);
    pub const freq_range = RegisterField(FreqRange, 0);
};

pub const DriveStrength = enum(u3) {
    default = 0b000,
    double = 0b001,
    triple = 0b011,
    quadruple = 0b111,
};

pub const freqa = struct {
    pub usingnamespace PeripheralRegister(base_address + 0x04);

    pub const passwd_value = 0x9696;

    pub const passwd = RegisterField(Passwd, 16);
    pub const ds3 = RegisterField(DriveStrength, 12);
    pub const ds2 = RegisterField(DriveStrength, 8);
    pub const ds1 = RegisterField(DriveStrength, 4);
    pub const ds0 = RegisterField(DriveStrength, 0);
};

pub const freqb = struct {
    pub usingnamespace PeripheralRegister(base_address + 0x08);

    pub const passwd_value = 0x9696;

    pub const passwd = RegisterField(Passwd, 16);
    pub const ds7 = RegisterField(DriveStrength, 12);
    pub const ds6 = RegisterField(DriveStrength, 8);
    pub const ds5 = RegisterField(DriveStrength, 4);
    pub const ds4 = RegisterField(DriveStrength, 0);
};

pub const dormant = struct {
    pub usingnamespace PeripheralRegister(base_address + 0x0c);

    pub const dormant_value = 0x636f6d61;
};

pub const div = struct {
    pub usingnamespace PeripheralRegister(base_address + 0x10);

    pub fn divisorValue(divisor: u5) u32 {
        return @as(u32, 0xaa0) + divisor;
    }
};

pub const phase = struct {
    pub usingnamespace PeripheralRegister(base_address + 0x14);

    pub const passwd_value = 0xaa;

    pub const passwd = RegisterField(Passwd, 4);
    pub const enable = RegisterField(bool, 3);
    pub const flip = RegisterField(bool, 2);
    pub const shift = RegisterField(u2, 0);
};

pub const status = struct {
    pub usingnamespace PeripheralRegister(base_address + 0x18);

    pub const stable = RegisterField(bool, 31);
    pub const badwrite = RegisterField(bool, 24);
    pub const div_running = RegisterField(bool, 16);
    pub const enabled = RegisterField(bool, 12);
};

pub const randombit = struct {
    pub usingnamespace PeripheralRegister(base_address + 0x1c);

    pub const value = RegisterField(u1, 0);
};

pub const count = struct {
    pub usingnamespace PeripheralRegister(base_address + 0x20);

    pub const value = RegisterField(u8, 0);
};
