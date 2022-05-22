const Register = @import("register.zig").Register;
const RegisterField = @import("register_field.zig").RegisterField;

const base_address = 0x40000000;

pub const chip_id = struct {
    pub usingnamespace Register(base_address + 0x00);

    pub const revision = RegisterField(u4, 28);
    pub const part = RegisterField(u16, 12);
    pub const manufacturer = RegisterField(u12, 0);
};

pub const platform = struct {
    pub usingnamespace Register(base_address + 0x04);

    pub const asic = RegisterField(bool, 1);
    pub const fpga = RegisterField(bool, 0);
};

pub const gitref_rp2040 = Register(base_address + 0x40);
