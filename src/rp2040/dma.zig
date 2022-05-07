const PeripheralRegister = @import("peripheral_register.zig").PeripheralRegister;
const PeripheralRegisterArray = @import("peripheral_register_array.zig").PeripheralRegisterArray;

const base_address = 0x50000000;

pub const ctrl = struct {
    pub const TreqSel = enum(u6) {
        DReqPio0Tx0,
        DReqPio0Tx1,
        DReqPio0Tx2,
        DReqPio0Tx3,

        DReqPio0Rx0,
        DReqPio0Rx1,
        DReqPio0Rx2,
        DReqPio0Rx3,

        DReqPio1Tx0,
        DReqPio1Tx1,
        DReqPio1Tx2,
        DReqPio1Tx3,

        DReqPio1Rx0,
        DReqPio1Rx1,
        DReqPio1Rx2,
        DReqPio1Rx3,

        DReqSpi0Tx,
        DReqSpi0Rx,

        DReqSpi1Tx,
        DReqSpi1Rx,

        DReqUart0Tx,
        DReqUart0Rx,

        DReqUart1Tx,
        DReqUart1Rx,

        DReqPwmWrap0,
        DReqPwmWrap1,
        DReqPwmWrap2,
        DReqPwmWrap3,
        DReqPwmWrap4,
        DReqPwmWrap5,
        DReqPwmWrap6,
        DReqPwmWrap7,

        DReqI2c0Tx,
        DReqI2c0Rx,

        DReqI2c1Tx,
        DReqI2c1Rx,

        DReqAdc,

        DReqXipStream,
        DReqXipSsitx,
        DReqXipSsirx,

        Timer0,
        Timer1,
        Timer2,
        Timer3,

        Permanent,
    };

    pub const DataSize = enum(u2) {
        Byte,
        Halfword,
        Word,
    };

    // pub const ahb_error = RegisterField(bool, 31);
    // pub const read_error = RegisterField(bool, 30);
    // pub const write_error = RegisterField(bool, 29);
    // pub const busy = RegisterField(bool, 24);
    // pub const sniff_en = RegisterField(bool, 23);
    // pub const bswap = RegisterField(bool, 22);
    // pub const irq_quiet = RegisterField(bool, 21);
    // pub const treq_sel = RegisterField(TreqSel, 15);
    // pub const chain_to = RegisterField(u4, 11);
    // pub const ring_sel = RegisterField(bool, 10);
    // pub const ring_size = RegisterField(u4, 6);
    // pub const incr_write = RegisterField(bool, 5);
    // pub const incr_read = RegisterField(bool, 4);
    // pub const data_size = RegisterField(DataSize, 2);
    // pub const high_priority = RegisterField(bool, 1);
    // pub const en = RegisterField(bool, 0);
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

    // pub const x = RegisterField(u16, 16);
    // pub const y = RegisterField(u16, 0);
};

pub const multi_chan_trigger = PeripheralRegister(base_address + 0x430);

pub const sniff_ctrl = struct {
    pub usingnamespace PeripheralRegister(base_address + 0x434);

    pub const Calc = enum(u4) {
        Crc32 = 0x0,
        Crc32Rev = 0x1,
        Crc16Ccitt = 0x2,
        Crc16CcittRev = 0x3,
        Xor = 0xe,
        Sum = 0xf,
    };

    // pub const out_inv = RegisterField(bool, 11);
    // pub const out_rev = RegisterField(bool, 10);
    // pub const bswap = RegisterField(bool, 9);
    // pub const calc = RegisterField(Calc, 5);
    // pub const dmach = RegisterField(u4, 1);
    // pub const en = RegisterField(bool, 0);
};

pub const sniff_data = PeripheralRegister(base_address + 0x438);

pub const fifo_levels = struct {
    pub usingnamespace PeripheralRegister(base_address + 0x440);

    // pub const raf_lvl = RegisterField(u8, 16);
    // pub const waf_lvl = RegisterField(u8, 8);
    // pub const tdf_lvl = RegisterField(u8, 0);
};

pub const chan_abort = PeripheralRegister(base_address + 0x444);

pub const dbg_ctdreq = PeripheralRegisterArray(12, base_address + 0x800, 0x40);

pub const dbg_tcr = PeripheralRegisterArray(12, base_address + 0x804, 0x40);
