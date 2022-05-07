const PeripheralRegister = @import("peripheral_register.zig").PeripheralRegister;

const base_address = 0x40060000;

pub const ctrl = struct {
    pub usingnamespace PeripheralRegister(base_address + 0x00);

    pub const Enable = enum(u12) {
        Disable = 0xd1e,
        Enable = 0xfab,
    };

    pub const FreqRange = enum(u12) {
        Low = 0xfa4,
        Medium = 0xfa5,
        High = 0xfa7,
        TooHigh = 0xfa6,
    };

    //pub const enable = RegisterField(Enable, 12);
    //pub const freq_range = RegisterField(FreqRange, 0);
};

pub const DriveStrength = enum(u3) {
    Default = 0b000,
    Double = 0b001,
    Triple = 0b011,
    Quadruple = 0b111,
};

pub const freqa = struct {
    pub usingnamespace PeripheralRegister(base_address + 0x04);

    pub const passwd_value = 0x9696;

    // pub const passwd = RegisterField(u16, 16);
    // pub const ds3 = RegisterField(DriveStrength, 12);
    // pub const ds2 = RegisterField(DriveStrength, 8);
    // pub const ds1 = RegisterField(DriveStrength, 4);
    // pub const ds0 = RegisterField(DriveStrength, 0);
};

pub const freqb = struct {
    pub usingnamespace PeripheralRegister(base_address + 0x08);

    pub const passwd_value = 0x9696;

    // pub const passwd = RegisterField(u16, 16);
    // pub const ds7 = RegisterField(DriveStrength, 12);
    // pub const ds6 = RegisterField(DriveStrength, 8);
    // pub const ds5 = RegisterField(DriveStrength, 4);
    // pub const ds4 = RegisterField(DriveStrength, 0);
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

    // pub const passwd = RegisterField(u8, 4);
    // pub const enable = RegisterField(bool, 3);
    // pub const flip = RegisterField(bool, 2);
    // pub const shift = RegisterField(u2, 0);
};

pub const status = struct {
    pub usingnamespace PeripheralRegister(base_address + 0x18);

    // pub const stable = RegisterField(bool, 31);
    // pub const badwrite = RegisterField(bool, 24);
    // pub const div_running = RegisterField(bool, 16);
    // pub const enabled = RegisterField(bool, 12);
};

pub const randombit = struct {
    pub usingnamespace PeripheralRegister(base_address + 0x1c);

    // pub const value = RegisterField(u1, 0);
};

pub const count = struct {
    pub usingnamespace PeripheralRegister(base_address + 0x20);

    // pub const value = RegisterField(u8, 0);
};
