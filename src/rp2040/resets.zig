const bits = @import("../bits.zig");

const PeripheralRegister = @import("peripheral_register.zig").PeripheralRegister;

const base_address = 0x4000c000;

const spec = bits.BitStructSpec{
    .Record = &[_]bits.BitStructField{
        .{
            .name = "usbctrl",
            .type = bool,
            .lsb = 24,
            .default = &true,
        },
        .{
            .name = "uart1",
            .type = bool,
            .lsb = 23,
            .default = &true,
        },
        .{
            .name = "uart0",
            .type = bool,
            .lsb = 22,
            .default = &true,
        },
        .{
            .name = "timer",
            .type = bool,
            .lsb = 21,
            .default = &true,
        },
        .{
            .name = "tbman",
            .type = bool,
            .lsb = 20,
            .default = &true,
        },
        .{
            .name = "sysinfo",
            .type = bool,
            .lsb = 19,
            .default = &true,
        },
        .{
            .name = "syscfg",
            .type = bool,
            .lsb = 18,
            .default = &true,
        },
        .{
            .name = "spi1",
            .type = bool,
            .lsb = 17,
            .default = &true,
        },
        .{
            .name = "spi0",
            .type = bool,
            .lsb = 16,
            .default = &true,
        },
        .{
            .name = "rtc",
            .type = bool,
            .lsb = 15,
            .default = &true,
        },
        .{
            .name = "pwm",
            .type = bool,
            .lsb = 14,
            .default = &true,
        },
        .{
            .name = "pll_usb",
            .type = bool,
            .lsb = 13,
            .default = &true,
        },
        .{
            .name = "pll_sys",
            .type = bool,
            .lsb = 12,
            .default = &true,
        },
        .{
            .name = "pio1",
            .type = bool,
            .lsb = 11,
            .default = &true,
        },
        .{
            .name = "pio0",
            .type = bool,
            .lsb = 10,
            .default = &true,
        },
        .{
            .name = "pads_qspi",
            .type = bool,
            .lsb = 9,
            .default = &true,
        },
        .{
            .name = "pads_bank0",
            .type = bool,
            .lsb = 8,
            .default = &true,
        },
        .{
            .name = "jtag",
            .type = bool,
            .lsb = 7,
            .default = &true,
        },
        .{
            .name = "io_qspi",
            .type = bool,
            .lsb = 6,
            .default = &true,
        },
        .{
            .name = "io_bank0",
            .type = bool,
            .lsb = 5,
            .default = &true,
        },
        .{
            .name = "i2c1",
            .type = bool,
            .lsb = 4,
            .default = &true,
        },
        .{
            .name = "i2c0",
            .type = bool,
            .lsb = 3,
            .default = &true,
        },
        .{
            .name = "dma",
            .type = bool,
            .lsb = 2,
            .default = &true,
        },
        .{
            .name = "busctrl",
            .type = bool,
            .lsb = 1,
            .default = &true,
        },
        .{
            .name = "adc",
            .type = bool,
            .lsb = 0,
            .default = &true,
        },
    },
};

pub const reset = PeripheralRegister(base_address + 0x0, spec);
pub const wdsel = PeripheralRegister(base_address + 0x4, spec);
pub const reset_done = PeripheralRegister(base_address + 0x8, spec);
