const PeripheralRegister = @import("peripheral_register.zig").PeripheralRegister;
const PeripheralRegisterArray = @import("peripheral_register_array.zig").PeripheralRegisterArray;

const base_address = 0x40008000;

const full_div_spec = .{
    .{
        .name = "int",
        .type = u24,
        .lsb = 8,
        .default = 1,
    },
    .{
        .name = "frac",
        .type = u8,
        .lsb = 0,
        .default = 0,
    },
};

const partial_div_spec = .{
    .{
        .name = "int",
        .type = u2,
        .lsb = 8,
        .default = 1,
    },
};

pub const clock_gpout_ctrl = PeripheralRegisterArray(4, base_address + 0x00, 0x0c, .{
    .{
        .name = "nudge",
        .type = bool,
        .lsb = 20,
        .default = false,
    },
    .{
        .name = "phase",
        .type = u2,
        .lsb = 16,
        .default = 0,
    },
    .{
        .name = "dc50",
        .type = bool,
        .lsb = 12,
        .default = false,
    },
    .{
        .name = "enable",
        .type = bool,
        .lsb = 11,
        .default = false,
    },
    .{
        .name = "kill",
        .type = bool,
        .lsb = 10,
        .default = false,
    },
    .{
        .name = "auxsrc",
        .type = enum(u4) {
            ClksrcPllSys,
            ClksrcGpin0,
            ClksrcGpin1,
            ClksrcPllUsb,
            RoscClksrc,
            XoscClksrc,
            ClkSys,
            ClkUsb,
            ClkAdc,
            ClkRtc,
            ClkRef,
        },
        .lsb = 5,
        .default = .ClksrcPllSys,
    },
});

pub const clk_gpout_div = PeripheralRegisterArray(4, base_address + 0x04, 0x0c, full_div_spec);

pub const clk_ref_ctrl = PeripheralRegister(base_address + 0x30, .{
    .{
        .name = "auxsrc",
        .type = enum(u2) {
            ClksrcPllUsb,
            ClksrcGpin0,
            ClksrcGpin1,
        },
        .lsb = 5,
        .default = .ClksrcPllUsb,
    },
    .{
        .name = "src",
        .type = enum(u2) {
            RoscClksrcPh,
            ClksrcClkRefAux,
            XoscClksrc,
        },
        .lsb = 0,
        .default = .RoscClksrcPh,
    },
});

pub const clk_ref_div = PeripheralRegister(base_address + 0x34, partial_div_spec);

pub const clk_ref_selected = PeripheralRegister(base_address + 0x38, .{
    .{
        .name = "rosc_clksrc_ph",
        .type = bool,
        .lsb = 0,
    },
    .{
        .name = "clksrc_clk_ref_aux",
        .type = bool,
        .lsb = 1,
    },
    .{
        .name = "xosc_clksrc",
        .type = bool,
        .lsb = 2,
    },
});

pub const clk_sys_control = PeripheralRegister(base_address + 0x3c, .{
    .{
        .name = "auxsrc",
        .type = enum(u3) {
            ClksrcPllSys,
            ClksrcPllUsb,
            RoscClksrc,
            XoscClksrc,
            ClksrcGpin0,
            ClksrcGpin1,
        },
        .lsb = 5,
        .default = .ClksrcPllSys,
    },
    .{
        .name = "src",
        .type = enum(u1) {
            ClkRef,
            ClksrcClkSysAux,
        },
        .lsb = 0,
        .default = .ClkRef,
    },
});

pub const clk_sys_div = PeripheralRegister(base_address + 0x40, full_div_spec);

pub const clk_sys_selected = PeripheralRegister(base_address + 0x44, .{
    .{
        .name = "clk_ref",
        .type = bool,
        .lsb = 0,
    },
    .{
        .name = "clksrc_clk_sys_aux",
        .type = bool,
        .lsb = 1,
    },
});

pub const clk_peri_ctrl = PeripheralRegister(base_addresss + 0x48, .{
    .{
        .name = "enable",
        .type = bool,
        .lsb = 11,
        .default = false,
    },
    .{
        .name = "kill",
        .type = bool,
        .lsb = 10,
        .default = false,
    },
    .{
        .name = "auxsrc",
        .type = enum(u3) {
            ClkSys,
            ClksrcPllSys,
            ClksrcPllUsb,
            RoscClksrcPh,
            XoscClksrc,
            ClksrcGpin0,
            ClksrcGpin1,
        },
        .lsb = 5,
        .default = .ClkSys,
    },
});

const usb_adc_rtc_ctrl_spec = .{
    .{
        .name = "nudge",
        .type = bool,
        .lsb = 20,
        .default = false,
    },
    .{
        .name = "phase",
        .type = u2,
        .lsb = 16,
        .default = 0,
    },
    .{
        .name = "enable",
        .type = bool,
        .lsb = 11,
        .default = false,
    },
    .{
        .name = "kill",
        .type = bool,
        .lsb = 10,
        .default = false,
    },
    .{
        .name = "auxsrc",
        .type = enum(u3) {
            ClksrcPllUsb,
            ClksrcPllSys,
            RoscClksrcPh,
            XoscClksrc,
            ClksrcGpin0,
            ClksrcGpin1,
        },
        .lsb = 5,
        .default = .ClksrcPllUsb,
    },
};

pub const clk_usb_ctrl = PeripheralRegister(base_address + 0x54, usb_adc_rtc_ctrl_spec);

pub const clk_usb_div = PeripheralRegister(base_address + 0x58, partial_div_spec);

pub const clk_adc_ctrl = PeripheralRegister(base_address + 0x60, usb_adc_rtc_ctrl_spec);

pub const clk_adc_div = PeripheralRegister(base_address + 0x64, partial_div_spec);

pub const clk_rtc_ctrl = PeripheralRegister(base_address + 0x6c, usb_adc_rtc_ctrl_spec);

pub const clk_rtc_div = PeripheralRegister(base_address + 0x70, full_div_spec);

pub const clk_sys_resus_ctrl = PeripheralRegister(base_address + 0x78, .{
    .{
        .name = "clear",
        .type = bool,
        .lsb = 16,
        .default = false,
    },
    .{
        .name = "frce",
        .type = bool,
        .lsb = 12,
        .default = false,
    },
    .{
        .name = "enable",
        .type = bool,
        .lsb = 8,
        .default = false,
    },
    .{
        .name = "timeout",
        .type = u8,
        .lsb = 0,
        .default = 0xff,
    },
});

pub const clk_sys_resus_status = PeripheralRegister(base_address + 0x7c, bool);

pub const fc0_ref_khz = PeripheralRegister(base_address + 0x80, u20);

pub const fc0_min_khz = PeripheralRegister(base_address + 0x84, u25);

pub const fc0_max_khz = PeripheralRegister(base_address + 0x88, u25);

pub const fc0_delay = PeripheralRegister(base_address + 0x8c, u3);

pub const fc0_interval = PeripheralRegister(base_address + 0x90, u4);

pub const fc0_src = PeripheralRegister(base_address + 0x94, enum(u8) {
    Null,
    PllSysClksrcPrimary,
    PllUsbClksrcPrimary,
    RoscClksrc,
    RoscClksrcPh,
    XoscClksrc,
    ClksrcGpin0,
    ClksrcGpin1,
    ClkRef,
    ClkSys,
    ClkPeri,
    ClkUsb,
    ClkAdc,
    ClkRtc,
});

pub const fc0_status = PeripheralRegister(base_address + 0x98, .{
    .{
        .name = "died",
        .type = bool,
        .lsb = 28,
    },
    .{
        .name = "fast",
        .type = bool,
        .lsb = 24,
    },
    .{
        .name = "slow",
        .type = bool,
        .lsb = 20,
    },
    .{
        .name = "fail",
        .type = bool,
        .lsb = 16,
    },
    .{
        .name = "waiting",
        .type = bool,
        .lsb = 12,
    },
    .{
        .name = "running",
        .type = bool,
        .lsb = 8,
    },
    .{
        .name = "done",
        .type = bool,
        .lsb = 4,
    },
    .{
        .name = "pass",
        .type = bool,
        .lsb = 0,
    },
});

pub const fc0_result = PeripheralRegister(base_address + 0x9c, .{
    .{
        .name = "khz",
        .type = u25,
        .lsb = 5,
    },
    .{
        .name = "frac",
        .type = u5,
        .lsb = 0,
    },
});

const en1_spec = .{
    .{
        .name = "clk_sys_xosc",
        .type = bool,
        .lsb = 14,
        .default = true,
    },
    .{
        .name = "clk_sys_xip",
        .type = bool,
        .lsb = 13,
        .default = true,
    },
    .{
        .name = "clk_sys_watchdog",
        .type = bool,
        .lsb = 12,
        .default = true,
    },
    .{
        .name = "clk_usb_usbctrl",
        .type = bool,
        .lsb = 11,
        .default = true,
    },
    .{
        .name = "clk_sys_usbctrl",
        .type = bool,
        .lsb = 10,
        .default = true,
    },
    .{
        .name = "clk_sys_uart1",
        .type = bool,
        .lsb = 9,
        .default = true,
    },
    .{
        .name = "clk_peri_uart1",
        .type = bool,
        .lsb = 8,
        .default = true,
    },
    .{
        .name = "clk_sys_uart0",
        .type = bool,
        .lsb = 7,
        .default = true,
    },
    .{
        .name = "clk_peri_uart0",
        .type = bool,
        .lsb = 6,
        .default = true,
    },
    .{
        .name = "clk_sys_timer",
        .type = bool,
        .lsb = 5,
        .default = true,
    },
    .{
        .name = "clk_sys_tbman",
        .type = bool,
        .lsb = 4,
        .default = true,
    },
    .{
        .name = "clk_sys_sysinfo",
        .type = bool,
        .lsb = 3,
        .default = true,
    },
    .{
        .name = "clk_sys_syscfg",
        .type = bool,
        .lsb = 2,
        .default = true,
    },
    .{
        .name = "clk_sys_sram5",
        .type = bool,
        .lsb = 1,
        .default = true,
    },
    .{
        .name = "clk_sys_sram4",
        .type = bool,
        .lsb = 0,
        .default = true,
    },
};

const en0_spec = .{
    .{
        .name = "clk_sys_sram3",
        .type = bool,
        .lsb = 31,
        .default = true,
    },
    .{
        .name = "clk_sys_sram2",
        .type = bool,
        .lsb = 30,
        .default = true,
    },
    .{
        .name = "clk_sys_sram1",
        .type = bool,
        .lsb = 29,
        .default = true,
    },
    .{
        .name = "clk_sys_sram0",
        .type = bool,
        .lsb = 28,
        .default = true,
    },
    .{
        .name = "clk_sys_spi1",
        .type = bool,
        .lsb = 27,
        .default = true,
    },
    .{
        .name = "clk_peri_spi1",
        .type = bool,
        .lsb = 26,
        .default = true,
    },
    .{
        .name = "clk_sys_spi0",
        .type = bool,
        .lsb = 25,
        .default = true,
    },
    .{
        .name = "clk_peri_spi0",
        .type = bool,
        .lsb = 24,
        .default = true,
    },
    .{
        .name = "clk_sys_sio",
        .type = bool,
        .lsb = 23,
        .default = true,
    },
    .{
        .name = "clk_sys_rtc",
        .type = bool,
        .lsb = 22,
        .default = true,
    },
    .{
        .name = "clk_rtc_rtc",
        .type = bool,
        .lsb = 21,
        .default = true,
    },
    .{
        .name = "clk_sys_rosc",
        .type = bool,
        .lsb = 20,
        .default = true,
    },
    .{
        .name = "clk_sys_rom",
        .type = bool,
        .lsb = 19,
        .default = true,
    },
    .{
        .name = "clk_sys_resets",
        .type = bool,
        .lsb = 18,
        .default = true,
    },
    .{
        .name = "clk_sys_pwm",
        .type = bool,
        .lsb = 17,
        .default = true,
    },
    .{
        .name = "clk_sys_psm",
        .type = bool,
        .lsb = 16,
        .default = true,
    },
    .{
        .name = "clk_sys_pll_usb",
        .type = bool,
        .lsb = 15,
        .default = true,
    },
    .{
        .name = "clk_sys_pll_sys",
        .type = bool,
        .lsb = 14,
        .default = true,
    },
    .{
        .name = "clk_sys_pio1",
        .type = bool,
        .lsb = 13,
        .default = true,
    },
    .{
        .name = "clk_sys_pio0",
        .type = bool,
        .lsb = 12,
        .default = true,
    },
    .{
        .name = "clk_sys_pads",
        .type = bool,
        .lsb = 11,
        .default = true,
    },
    .{
        .name = "clk_sys_vreg_and_chip_reset",
        .type = bool,
        .lsb = 10,
        .default = true,
    },
    .{
        .name = "clk_sys_jtag",
        .type = bool,
        .lsb = 9,
        .default = true,
    },
    .{
        .name = "clk_sys_io",
        .type = bool,
        .lsb = 8,
        .default = true,
    },
    .{
        .name = "clk_sys_i2c1",
        .type = bool,
        .lsb = 7,
        .default = true,
    },
    .{
        .name = "clk_sys_i2c0",
        .type = bool,
        .lsb = 6,
        .default = true,
    },
    .{
        .name = "clk_sys_dma",
        .type = bool,
        .lsb = 5,
        .default = true,
    },
    .{
        .name = "clk_sys_busfabric",
        .type = bool,
        .lsb = 4,
        .default = true,
    },
    .{
        .name = "clk_sys_busctrl",
        .type = bool,
        .lsb = 3,
        .default = true,
    },
    .{
        .name = "clk_sys_adc",
        .type = bool,
        .lsb = 2,
        .default = true,
    },
    .{
        .name = "clk_adc_adc",
        .type = bool,
        .lsb = 1,
        .default = true,
    },
    .{
        .name = "clk_sys_clocks",
        .type = bool,
        .lsb = 0,
        .default = true,
    },
};

pub const wake_en0 = PeripheralRegister(base_address + 0xa0, en0_spec);

pub const wake_en1 = PeripheralRegister(base_address + 0xa4, en1_spec);

pub const sleep_en0 = PeripheralRegister(base_address + 0xa8, en0_spec);

pub const sleep_en1 = PeripheralRegister(base_address + 0xac, en1_spec);

pub const enabled0 = PeripheralRegister(base_address + 0xb0, en0_spec);

pub const enabled1 = PeripheralRegister(base_address + 0xb4, en1_spec);

const int_spec = .{
    .{
        .name = "clk_sys_resus",
        .type = bool,
        .lsb = 0,
        .default = false,
    },
};

pub const intr = PeripheralRegister(base_address + 0xb8, int_spec);

pub const inte = PeripheralRegister(base_address + 0xbc, int_spec);

pub const intf = PeripheralRegister(base_address + 0xc0, int_spec);

pub const ints = PeripheralRegister(base_address + 0xc4, int_spec);
