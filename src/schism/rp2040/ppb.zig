const bits = @import("../bits.zig");

const Register = @import("register.zig").Register;
const RegisterArray = @import("register_array.zig").RegisterArray;

const base_address = 0xe0000000;

pub const Clksource = enum(u1) {
    ExternalReference,
    Processor,
};

pub const syst_csr = Register(0xe010, .{
    .Record = &.{
        .{
            .name = "countflag",
            .type = bool,
            .lsb = 16,
            .default = &false,
        },
        .{
            .name = "clksource",
            .type = Clksource,
            .lsb = 2,
            .default = &@as(Clksource, .ExternalReference),
        },
        .{
            .name = "tickint",
            .type = bool,
            .lsb = 1,
            .default = &false,
        },
        .{
            .name = "enable",
            .type = bool,
            .lsb = 0,
            .default = &false,
        },
    },
});

pub const syst_rvr = Register(0xe014, .{
    .Scalar = u24,
});

pub const syst_cvr = Register(0xe018, .{
    .Scalar = u24,
});

pub const syst_calib = Register(0xe01c, .{
    .Record = &.{
        .{
            .name = "noref",
            .type = bool,
            .lsb = 31,
        },
        .{
            .name = "skew",
            .type = bool,
            .lsb = 30,
        },
        .{
            .name = "tenms",
            .type = u24,
            .lsb = 0,
        },
    },
});

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

pub const icsr = Register(base_address + 0xed04, .{
    .Record = &.{
        .{
            .name = "nmipendset",
            .type = bool,
            .lsb = 31,
            .default = &false,
        },
        .{
            .name = "pendsvset",
            .type = bool,
            .lsb = 28,
            .default = &false,
        },
        .{
            .name = "pendsvclr",
            .type = bool,
            .lsb = 27,
            .default = &false,
        },
        .{
            .name = "pendstset",
            .type = bool,
            .lsb = 26,
            .default = &false,
        },
        .{
            .name = "pendstclr",
            .type = bool,
            .lsb = 25,
            .default = &false,
        },
        .{
            .name = "isrpreempt",
            .type = bool,
            .lsb = 23,
            .default = &false,
        },
        .{
            .name = "isrpending",
            .type = bool,
            .lsb = 22,
            .default = &false,
        },
        .{
            .name = "vectpending",
            .type = u9,
            .lsb = 12,
            .default = &@as(u9, 0),
        },
        .{
            .name = "vectactive",
            .type = u8,
            .lsb = 0,
            .default = &@as(u9, 0),
        },
    },
});

pub const vtor = Register(base_address + 0xed08, .{
    .Scalar = u32,
});

pub const aircr = Register(base_address + 0xed0c, .{
    .Record = &.{
        .{
            .name = "vectkey",
            .type = u16,
            .lsb = 16,
            .default = &@as(u16, 0x05fa),
        },
        .{
            .name = "endianness",
            .type = bool,
            .lsb = 15,
            .default = &false,
        },
        .{
            .name = "sysresetreq",
            .type = bool,
            .lsb = 2,
            .default = &false,
        },
        .{
            .name = "vectclractive",
            .type = bool,
            .lsb = 1,
            .default = &false,
        },
    },
});

pub const scr = Register(base_address + 0xed10, .{
    .Record = &.{
        .{
            .name = "sevonpend",
            .type = bool,
            .lsb = 4,
            .default = &false,
        },
        .{
            .name = "sleepdeep",
            .type = bool,
            .lsb = 2,
            .default = &false,
        },
        .{
            .name = "sleeponexit",
            .type = bool,
            .lsb = 1,
            .default = &false,
        },
    },
});

pub const shpr2 = Register(base_address + 0xed1c, .{
    .Record = &.{
        .{
            .name = "pri_11",
            .type = u2,
            .lsb = 30,
        },
    },
});

pub const shpr3 = Register(base_address + 0xed20, .{
    .Record = &.{
        .{
            .name = "pri_15",
            .type = u2,
            .lsb = 30,
        },
        .{
            .name = "pri_14",
            .type = u2,
            .lsb = 22,
        },
    },
});

pub const shcsr = Register(base_address + 0xed24, .{
    .Record = &.{
        .{
            .name = "svcallpended",
            .type = bool,
            .lsb = 15,
        },
    },
});

pub const mpu_ctrl = Register(base_address + 0xed94, .{
    .Record = &.{
        .{
            .name = "privdefena",
            .type = bool,
            .lsb = 2,
            .default = &false,
        },
        .{
            .name = "hfnmiena",
            .type = bool,
            .lsb = 1,
            .default = &false,
        },
        .{
            .name = "enable",
            .type = bool,
            .lsb = 0,
            .default = &false,
        },
    },
});

pub const mpu_rnr = Register(base_address + 0xed98, .{
    .Scalar = u3,
});

pub const mpu_rbar = Register(base_address + 0xed9c, .{
    .Record = &.{
        .{
            .name = "addr",
            .type = u24,
            .lsb = 8,
        },

        .{
            .name = "valid",
            .type = bool,
            .lsb = 4,
        },
        .{
            .name = "region",
            .type = u3,
            .lsb = 0,
        },
    },
});

pub const mpu_rasr = Register(base_address + 0xeda0, .{
    .Record = &.{
        .{
            .name = "attrs_xn",
            .type = bool,
            .lsb = 28,
        },
        .{
            .name = "attrs_ap",
            .type = u3,
            .lsb = 24,
        },
        .{
            .name = "attrs_s",
            .type = bool,
            .lsb = 18,
        },
        .{
            .name = "attrs_c",
            .type = bool,
            .lsb = 17,
        },
        .{
            .name = "attrs_b",
            .type = bool,
            .lsb = 16,
        },
        .{
            .name = "srd",
            .type = u8,
            .lsb = 8,
            .default = &@as(u8, 0),
        },
        .{
            .name = "size",
            .type = u5,
            .lsb = 1,
        },
        .{
            .name = "enable",
            .type = bool,
            .lsb = 0,
        },
    },
});
