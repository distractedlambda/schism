const registers = @import("../registers.zig");

const PeripheralRegister = registers.PeripheralRegister;
const PeripheralRegisterArray = registers.PeripheralRegisterArray;
const RegisterField = registers.RegisterField;

const base_address = 0x50000000;

pub const read_addr = PeripheralRegisterArray(12, base_address + 0x000, 0x40);

pub const write_addr = PeripheralRegisterArray(12, base_address + 0x004, 0x40);

pub const trans_count = PeripheralRegisterArray(12, base_address + 0x008, 0x40);

const ctrl_common = struct {
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

pub const ctrl_trig = struct {
    pub usingnamespace PeripheralRegisterArray(12, base_address + 0x00c, 0x40);
    pub usingnamespace ctrl_common;
};

pub const al1_ctrl = struct {
    pub usingnamespace PeripheralRegisterArray(12, base_address + 0x010, 0x40);
    pub usingnamespace ctrl_common;
};
