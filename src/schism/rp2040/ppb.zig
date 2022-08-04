const bits = @import("../bits.zig");

const Register = @import("register.zig").Register;
const RegisterArray = @import("register_array.zig").RegisterArray;

const base_address = 0xe0000000;

pub const syst_csr = struct {
    pub usingnamespace Register(base_address + 0xe010);

    pub const Clksource = enum(u1) {
        ExternalReference,
        Processor,
    };

    // pub const countflag = RegisterField(bool, 16);
    // pub const clksource = RegisterField(Clksource, 2);
    // pub const tickint = RegisterField(bool, 1);
    // pub const enable = RegisterField(bool, 0);
};

pub const syst_rvr = struct {
    pub usingnamespace Register(base_address + 0xe014);

    // pub const reload = RegisterField(u24, 0);
};

pub const syst_cvr = struct {
    pub usingnamespace Register(base_address + 0xe018);

    // pub const current = RegisterField(u24, 0);
};

pub const syst_calib = struct {
    pub usingnamespace Register(base_address + 0xe01c);

    // pub const noref = RegisterField(bool, 31);
    // pub const skew = RegisterField(bool, 30);
    // pub const tenms = RegisterField(u24, 0);
};

const irq_spec = bits.BitStructSpec{
    .Record = &.{
        .{
            .name = "timer_irq0",
            .type = bool,
            .lsb = 0,
            .default = &false,
        },
        .{
            .name = "timer_irq1",
            .type = bool,
            .lsb = 1,
            .default = &false,
        },
        .{
            .name = "timer_irq2",
            .type = bool,
            .lsb = 2,
            .default = &false,
        },
        .{
            .name = "timer_irq3",
            .type = bool,
            .lsb = 3,
            .default = &false,
        },
        .{
            .name = "pwm_wrap",
            .type = bool,
            .lsb = 4,
            .default = &false,
        },
        .{
            .name = "usbctrl",
            .type = bool,
            .lsb = 5,
            .default = &false,
        },
        .{
            .name = "xip",
            .type = bool,
            .lsb = 6,
            .default = &false,
        },
        .{
            .name = "pio0_irq0",
            .type = bool,
            .lsb = 7,
            .default = &false,
        },
        .{
            .name = "pio0_irq1",
            .type = bool,
            .lsb = 8,
            .default = &false,
        },
        .{
            .name = "pio1_irq0",
            .type = bool,
            .lsb = 9,
            .default = &false,
        },
        .{
            .name = "pio1_irq1",
            .type = bool,
            .lsb = 10,
            .default = &false,
        },
        .{
            .name = "dma_irq0",
            .type = bool,
            .lsb = 11,
            .default = &false,
        },
        .{
            .name = "dma_irq1",
            .type = bool,
            .lsb = 12,
            .default = &false,
        },
        .{
            .name = "io_bank0",
            .type = bool,
            .lsb = 13,
            .default = &false,
        },
        .{
            .name = "io_qspi",
            .type = bool,
            .lsb = 14,
            .default = &false,
        },
        .{
            .name = "sio_proc0",
            .type = bool,
            .lsb = 15,
            .default = &false,
        },
        .{
            .name = "sio_proc1",
            .type = bool,
            .lsb = 16,
            .default = &false,
        },
        .{
            .name = "clocks",
            .type = bool,
            .lsb = 17,
            .default = &false,
        },
        .{
            .name = "spi0",
            .type = bool,
            .lsb = 18,
            .default = &false,
        },
        .{
            .name = "spi1",
            .type = bool,
            .lsb = 19,
            .default = &false,
        },
        .{
            .name = "uart0",
            .type = bool,
            .lsb = 20,
            .default = &false,
        },
        .{
            .name = "uart1",
            .type = bool,
            .lsb = 21,
            .default = &false,
        },
        .{
            .name = "adc_fifo",
            .type = bool,
            .lsb = 22,
            .default = &false,
        },
        .{
            .name = "i2c0",
            .type = bool,
            .lsb = 23,
            .default = &false,
        },
        .{
            .name = "i2c1",
            .type = bool,
            .lsb = 24,
            .default = &false,
        },
        .{
            .name = "rtc",
            .type = bool,
            .lsb = 25,
            .default = &false,
        },
    },
};

pub const nvic_iser = Register(base_address + 0xe100, irq_spec);
pub const nvic_icer = Register(base_address + 0xe180, irq_spec);
pub const nvic_ispr = Register(base_address + 0xe200, irq_spec);
pub const nvic_icpr = Register(base_address + 0xe280, irq_spec);

pub const nvic_ipr = RegisterArray(8, base_address + 0xe400, 0x4);

pub const icsr = struct {
    pub usingnamespace Register(base_address + 0xed04);

    // pub const nmipendset = RegisterField(bool, 31);
    // pub const pendsvset = RegisterField(bool, 28);
    // pub const pendsvclr = RegisterField(bool, 27);
    // pub const pendstset = RegisterField(bool, 26);
    // pub const pendstclr = RegisterField(bool, 25);
    // pub const isrpreempt = RegisterField(bool, 23);
    // pub const isrpending = RegisterField(bool, 22);
    // pub const vectpending = RegisterField(u9, 12);
    // pub const vectactive = RegisterField(u9, 0);
};

pub const vtor = Register(base_address + 0xed08);

pub const aircr = struct {
    pub usingnamespace Register(base_address + 0xed0c);

    pub const vectkey = struct {
        // pub usingnamespace RegisterField(u16, 16);

        pub const key_value = 0x05FA;
    };

    // pub const endianness = RegisterField(bool, 15);
    // pub const sysresetreq = RegisterField(bool, 2);
    // pub const vectclractive = RegisterField(bool, 1);
};

pub const scr = struct {
    pub usingnamespace Register(base_address + 0xed10);

    // pub const sevonpend = RegisterField(bool, 4);
    // pub const sleepdeep = RegisterField(bool, 2);
    // pub const sleeponexit = RegisterField(bool, 1);
};

pub const shpr2 = struct {
    pub usingnamespace Register(base_address + 0xed1c);

    // pub const pri_11 = RegisterField(u2, 30);
};

pub const shpr3 = struct {
    pub usingnamespace Register(base_address + 0xed20);

    // pub const pri_15 = RegisterField(u2, 30);
    // pub const pri_14 = RegisterField(u2, 22);
};

pub const shcsr = struct {
    pub usingnamespace Register(base_address + 0xed24);

    // pub const svcallpended = RegisterField(bool, 15);
};

pub const mpu_ctrl = struct {
    pub usingnamespace Register(base_address + 0xed94);

    // pub const privdefena = RegisterField(bool, 2);
    // pub const hfnmiena = RegisterField(bool, 1);
    // pub const enable = RegisterField(bool, 0);
};

pub const mpu_rnr = struct {
    pub usingnamespace Register(base_address + 0xed98);

    // pub const region = RegisterField(u4, 0);
};

pub const mpu_rbar = struct {
    pub usingnamespace Register(base_address + 0xed9c);

    // pub const addr = RegisterField(u24, 8);
    // pub const valid = RegisterField(bool, 4);
    // pub const region = RegisterField(u4, 0);
};

pub const mpu_rasr = struct {
    pub usingnamespace Register(base_address + 0xeda0);

    pub const attrs = struct {
        // pub const xn = RegisterField(bool, 28);
        // pub const ap = RegisterField(u3, 24);
        // pub const s = RegisterField(bool, 18);
        // pub const c = RegisterField(bool, 17);
        // pub const b = RegisterField(bool, 16);
    };

    // pub const srd = RegisterField(u8, 8);
    // pub const size = RegisterField(u5, 1);
    // pub const enable = RegisterField(bool, 0);
};
