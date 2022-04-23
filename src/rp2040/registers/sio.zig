const registers = @import("../registers.zig");

const Register = registers.Register;
const RegisterArray = registers.RegisterArray;
const RegisterField = registers.RegisterField;

const base_address = 0xd0000000;

pub const cpuid = Register(base_address + 0x000);

pub const gpio_in = Register(base_address + 0x004);

pub const gpio_hi_in = Register(base_address + 0x008);

pub const gpio_out = Register(base_address + 0x010);

pub const gpio_out_set = Register(base_address + 0x014);

pub const gpio_out_clr = Register(base_address + 0x018);

pub const gpio_out_xor = Register(base_address + 0x01c);

pub const gpio_oe = Register(base_address + 0x020);

pub const gpio_oe_set = Register(base_address + 0x024);

pub const gpio_oe_clr = Register(base_address + 0x028);

pub const gpio_oe_xor = Register(base_address + 0x02c);

pub const gpio_hi_out = Register(base_address + 0x030);

pub const gpio_hi_out_set = Register(base_address + 0x034);

pub const gpio_hi_out_clr = Register(base_address + 0x038);

pub const gpio_hi_out_xor = Register(base_address + 0x03c);

pub const gpio_hi_oe = Register(base_address + 0x040);

pub const gpio_hi_oe_set = Register(base_address + 0x044);

pub const gpio_hi_oe_clr = Register(base_address + 0x048);

pub const gpio_hi_oe_xor = Register(base_address + 0x04c);

pub const fifo_st = struct {
    pub usingnamespace Register(base_address + 0x050);

    pub const roe = RegisterField(bool, 3);
    pub const wof = RegisterField(bool, 2);
    pub const rdy = RegisterField(bool, 1);
    pub const vld = RegisterField(bool, 0);
};
