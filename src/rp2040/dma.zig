const PeripheralRegister = @import("peripheral_register.zig").PeripheralRegister;
const PeripheralRegisterArray = @import("peripheral_register_array.zig").PeripheralRegisterArray;
const RegisterField = @import("register_field.zig").RegisterField;

const base_address = 0x50000000;

pub const ctrl = struct {
    pub const TreqSel = enum(u6) {
        dreq_pio0_tx0,
        dreq_pio0_tx1,
        dreq_pio0_tx2,
        dreq_pio0_tx3,

        dreq_pio0_rx0,
        dreq_pio0_rx1,
        dreq_pio0_rx2,
        dreq_pio0_rx3,

        dreq_pio1_tx0,
        dreq_pio1_tx1,
        dreq_pio1_tx2,
        dreq_pio1_tx3,

        dreq_pio1_rx0,
        dreq_pio1_rx1,
        dreq_pio1_rx2,
        dreq_pio1_rx3,

        dreg_spi0_tx,
        dreq_spi0_rx,

        dreg_spi1_tx,
        dreq_spi1_rx,

        dreg_uart0_tx,
        dreq_uart0_rx,

        dreg_uart1_tx,
        dreq_uart1_rx,

        dreq_pwm_wrap0,
        dreq_pwm_wrap1,
        dreq_pwm_wrap2,
        dreq_pwm_wrap3,
        dreq_pwm_wrap4,
        dreq_pwm_wrap5,
        dreq_pwm_wrap6,
        dreq_pwm_wrap7,

        dreq_i2c0_tx,
        dreq_i2c0_rx,

        dreq_i2c1_tx,
        dreq_i2c1_rx,

        dreq_adc,

        dreq_xip_stream,
        dreq_xip_ssitx,
        dreq_xip_ssirx,

        timer_0,
        timer_1,
        timer_2,
        timer_3,

        permanent,
    };

    pub const DataSize = enum(u2) {
        byte,
        halfword,
        word,
    };

    pub const ahb_error = RegisterField(bool, 31);
    pub const read_error = RegisterField(bool, 30);
    pub const write_error = RegisterField(bool, 29);
    pub const busy = RegisterField(bool, 24);
    pub const sniff_en = RegisterField(bool, 23);
    pub const bswap = RegisterField(bool, 22);
    pub const irq_quiet = RegisterField(bool, 21);
    pub const treq_sel = RegisterField(TreqSel, 15);
    pub const chain_to = RegisterField(u4, 11);
    pub const ring_sel = RegisterField(bool, 10);
    pub const ring_size = RegisterField(u4, 6);
    pub const incr_write = RegisterField(bool, 5);
    pub const incr_read = RegisterField(bool, 4);
    pub const data_size = RegisterField(DataSize, 2);
    pub const high_priority = RegisterField(bool, 1);
    pub const en = RegisterField(bool, 0);
};

pub const read_addr = PeripheralRegisterArray(12, base_address + 0x000, 0x40);
pub const write_addr = PeripheralRegisterArray(12, base_address + 0x004, 0x40);
pub const trans_count = PeripheralRegisterArray(12, base_address + 0x008, 0x40);
pub const ctrl_trig = PeripheralRegisterArray(12, base_address + 0x00c, 0x40);

pub const al1_ctrl = PeripheralRegisterArray(12, base_address + 0x010, 0x40);
pub const al1_read_addr = PeripheralRegisterArray(12, base_address + 0x014, 0x40);
pub const al1_write_addr = PeripheralRegisterArray(12, base_address + 0x018, 0x40);
pub const al1_trans_count_trig = PeripheralRegisterArray(12, base_address + 0x01c, 0x40);

pub const al2_ctrl = PeripheralRegisterArray(12, base_address + 0x020, 0x40);
pub const al2_trans_count = PeripheralRegisterArray(12, base_address + 0x024, 0x40);
pub const al2_read_addr = PeripheralRegisterArray(12, base_address + 0x024, 0x40);
pub const al2_write_addr_trig = PeripheralRegisterArray(12, base_address + 0x024, 0x40);

pub const al3_ctrl = PeripheralRegisterArray(12, base_address + 0x030, 0x40);
pub const al3_write_addr = PeripheralRegisterArray(12, base_address + 0x034, 0x40);
pub const al3_trans_count = PeripheralRegisterArray(12, base_address + 0x038, 0x40);
pub const al3_read_addr_trig = PeripheralRegisterArray(12, base_address + 0x03c, 0x40);

pub const intr = PeripheralRegister(base_address + 0x400);

pub const inte = PeripheralRegisterArray(2, base_address + 0x404, 0x10);
pub const intf = PeripheralRegisterArray(2, base_address + 0x408, 0x10);
pub const ints = PeripheralRegisterArray(2, base_address + 0x40c, 0x10);

pub const timer = struct {
    pub usingnamespace PeripheralRegisterArray(4, base_address + 0x420, 0x04);

    pub const x = RegisterField(u16, 16);
    pub const y = RegisterField(u16, 0);
};

pub const multi_chan_trigger = PeripheralRegister(base_address + 0x430);

pub const sniff_ctrl = struct {
    pub usingnamespace PeripheralRegister(base_address + 0x434);

    pub const Calc = enum(u4) {
        crc32 = 0x0,
        crc32_rev = 0x1,
        crc16_ccitt = 0x2,
        crc16_ccitt_rev = 0x3,
        xor = 0xe,
        sum = 0xf,
    };

    pub const out_inv = RegisterField(bool, 11);
    pub const out_rev = RegisterField(bool, 10);
    pub const bswap = RegisterField(bool, 9);
    pub const calc = RegisterField(Calc, 5);
    pub const dmach = RegisterField(u4, 1);
    pub const en = RegisterField(bool, 0);
};

pub const sniff_data = PeripheralRegister(base_address + 0x438);

pub const fifo_levels = struct {
    pub usingnamespace PeripheralRegister(base_address + 0x440);

    pub const raf_lvl = RegisterField(u8, 16);
    pub const waf_lvl = RegisterField(u8, 8);
    pub const tdf_lvl = RegisterField(u8, 0);
};

pub const chan_abort = PeripheralRegister(base_address + 0x444);

pub const dbg_ctdreq = PeripheralRegisterArray(12, base_address + 0x800, 0x40);

pub const dbg_tcr = PeripheralRegisterArray(12, base_address + 0x804, 0x40);
