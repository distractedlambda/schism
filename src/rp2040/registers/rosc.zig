const std = @import("std");

const registers = @import("../registers.zig");

const PeripheralRegister = registers.PeripheralRegister;
const RegisterField = registers.RegisterField;

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

test {
    std.testing.refAllDecls(ctrl);
    std.testing.refAllDecls(ctrl.enable);
    std.testing.refAllDecls(ctrl.freq_range);
}

pub const freqa = struct {
    pub usingnamespace PeripheralRegister(base_address + 0x04);

    pub const Passwd = enum(u16) {
        pass = 0x9696,
    };

    pub const DriveStrength = enum(u3) {
        default = 0b000,
        double = 0b001,
        triple = 0b011,
        quadruple = 0b111,
    };

    pub const passwd = RegisterField(Passwd, 16);
    pub const ds3 = RegisterField(DriveStrength, 12);
    pub const ds2 = RegisterField(DriveStrength, 8);
    pub const ds1 = RegisterField(DriveStrength, 4);
    pub const ds0 = RegisterField(DriveStrength, 0);
};

test {
    std.testing.refAllDecls(freqa);
    std.testing.refAllDecls(freqa.passwd);
    std.testing.refAllDecls(freqa.ds3);
}

pub const freqb = struct {
    pub usingnamespace PeripheralRegister(base_address + 0x08);

    pub const Passwd = freqa.Passwd;

    pub const DriveStrength = freqa.DriveStrength;

    pub const passwd = RegisterField(Passwd, 16);
    pub const ds7 = RegisterField(DriveStrength, 12);
    pub const ds6 = RegisterField(DriveStrength, 8);
    pub const ds5 = RegisterField(DriveStrength, 4);
    pub const ds4 = RegisterField(DriveStrength, 0);
};

test {
    std.testing.refAllDecls(freqb);
}

pub const dormant = struct {
    pub usingnamespace PeripheralRegister(base_address + 0x0c);

    pub fn setDormant() void {
        dormant.write(0x636f6d61);
    }
};

test {
    std.testing.refAllDecls(dormant);
}

pub const div = struct {
    pub usingnamespace PeripheralRegister(base_address + 0x10);

    pub fn setDivisor(divisor: u5) void {
        div.write(@as(u32, 0xaa0) + divisor);
    }
};

test {
    std.testing.refAllDecls(div);
}

pub const phase = struct {
    pub usingnamespace PeripheralRegister(base_address + 0x14);

    pub const Passwd = enum(u8) {
        pass = 0xaa,
    };

    pub const passwd = RegisterField(Passwd, 4);
    pub const enable = RegisterField(bool, 3);
    pub const flip = RegisterField(bool, 2);
    pub const shift = RegisterField(u2, 0);
};

test {
    std.testing.refAllDecls(phase);
}

pub const status = struct {
    pub usingnamespace PeripheralRegister(base_address + 0x18);

    pub const stable = RegisterField(bool, 31);
    pub const badwrite = RegisterField(bool, 24);
    pub const div_running = RegisterField(bool, 16);
    pub const enabled = RegisterField(bool, 12);
};

test {
    std.testing.refAllDecls(status);
    std.testing.refAllDecls(status.badwrite);
}

pub const randombit = struct {
    pub usingnamespace PeripheralRegister(base_address + 0x1c);

    pub const value = RegisterField(u1, 0);
};

test {
    std.testing.refAllDecls(randombit);
}

pub const count = struct {
    pub usingnamespace PeripheralRegister(base_address + 0x20);

    pub const value = RegisterField(u8, 0);
};

test {
    std.testing.refAllDecls(count);
}
