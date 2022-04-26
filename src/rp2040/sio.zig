const Register = @import("register.zig").Register;
const RegisterArray = @import("register_array.zig").RegisterArray;
const RegisterMatrix = @import("register_matrix.zig").RegisterMatrix;
const RegisterField = @import("register_field.zig").RegisterField;

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

pub const fifo_wr = Register(base_address + 0x054);
pub const fifo_rd = Register(base_address + 0x058);

pub const spinlock_st = Register(base_address + 0x05c);

pub const div_udividend = Register(base_address + 0x060);
pub const div_udivisor = Register(base_address + 0x064);
pub const div_sdividend = Register(base_address + 0x068);
pub const div_sdivisor = Register(base_address + 0x06c);
pub const div_quotient = Register(base_address + 0x070);
pub const div_remainder = Register(base_address + 0x074);

pub const div_csr = struct {
    pub usingnamespace Register(base_address + 0x078);

    pub const dirty = RegisterField(bool, 1);
    pub const ready = RegisterField(bool, 0);
};

pub const interp_accum = RegisterMatrix(2, 2, base_address + 0x080, 0x40, 0x4);
pub const interp_base = RegisterMatrix(2, 3, base_address + 0x088, 0x40, 0x4);
pub const interp_pop_lane = RegisterMatrix(2, 2, base_address + 0x094, 0x40, 0x4);
pub const interp_pop_full = RegisterArray(2, base_address + 0x09c, 0x40);
pub const interp_peek_lane = RegisterMatrix(2, 2, base_address + 0x0a0, 0x40, 0x4);
pub const interp_peek_full = RegisterArray(2, base_address + 0x0a8, 0x40);

pub const interp0_ctrl_lane0 = struct {
    pub usingnamespace Register(base_address + 0x0ac);

    pub const overf = RegisterField(bool, 25);
    pub const overf1 = RegisterField(bool, 24);
    pub const overf0 = RegisterField(bool, 23);
    pub const blend = RegisterField(bool, 21);
    pub const force_msb = RegisterField(u2, 19);
    pub const add_raw = RegisterField(bool, 18);
    pub const cross_result = RegisterField(bool, 17);
    pub const cross_input = RegisterField(bool, 16);
    pub const signed = RegisterField(bool, 15);
    pub const mask_msb = RegisterField(u5, 10);
    pub const mask_lsb = RegisterField(u5, 5);
    pub const shift = RegisterField(u5, 0);
};

pub const interp1_ctrl_lane0 = struct {
    pub usingnamespace Register(base_address + 0x0ec);

    pub const overf = RegisterField(bool, 25);
    pub const overf1 = RegisterField(bool, 24);
    pub const overf0 = RegisterField(bool, 23);
    pub const clamp = RegisterField(bool, 22);
    pub const force_msb = RegisterField(u2, 19);
    pub const add_raw = RegisterField(bool, 18);
    pub const cross_result = RegisterField(bool, 17);
    pub const cross_input = RegisterField(bool, 16);
    pub const signed = RegisterField(bool, 15);
    pub const mask_msb = RegisterField(u5, 10);
    pub const mask_lsb = RegisterField(u5, 5);
    pub const shift = RegisterField(u5, 0);
};

pub const interp_ctrl_lane1 = struct {
    pub usingnamespace RegisterArray(2, base_address + 0x0b0, 0x40);

    pub const force_msb = RegisterField(u2, 19);
    pub const add_raw = RegisterField(bool, 18);
    pub const cross_result = RegisterField(bool, 17);
    pub const cross_input = RegisterField(bool, 16);
    pub const signed = RegisterField(bool, 15);
    pub const mask_msb = RegisterField(u5, 10);
    pub const mask_lsb = RegisterField(u5, 5);
    pub const shift = RegisterField(u5, 0);
};

pub const interp_accum_add = RegisterMatrix(2, 2, base_address + 0x0b4, 0x40, 0x4);

pub const interp_base1and0 = RegisterArray(2, base_address + 0x0bc, 0x40);

pub const spinlock = RegisterArray(32, base_address + 0x100, 0x4);
