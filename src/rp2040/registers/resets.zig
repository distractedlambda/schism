const registers = @import("../registers.zig");

const PeripheralRegister = registers.PeripheralRegister;
const RegisterField = registers.RegisterField;

const base_address = 0x4000c000;

const common = struct {
    pub const usbctrl = RegisterField(bool, 24);
    pub const uart1 = RegisterField(bool, 23);
    pub const uart0 = RegisterField(bool, 22);
    pub const timer = RegisterField(bool, 21);
    pub const tbman = RegisterField(bool, 20);
    pub const sysinfo = RegisterField(bool, 19);
    pub const syscfg = RegisterField(bool, 18);
    pub const spi1 = RegisterField(bool, 17);
    pub const spi0 = RegisterField(bool, 16);
    pub const rtc = RegisterField(bool, 15);
    pub const pwm = RegisterField(bool, 14);
    pub const pll_usb = RegisterField(bool, 13);
    pub const pll_sys = RegisterField(bool, 12);
    pub const pio1 = RegisterField(bool, 11);
    pub const pio0 = RegisterField(bool, 10);
    pub const pads_qspi = RegisterField(bool, 9);
    pub const pads_bank0 = RegisterField(bool, 8);
    pub const jtag = RegisterField(bool, 7);
    pub const io_qspi = RegisterField(bool, 6);
    pub const io_bank0 = RegisterField(bool, 5);
    pub const i2c1 = RegisterField(bool, 4);
    pub const i2c0 = RegisterField(bool, 3);
    pub const dma = RegisterField(bool, 2);
    pub const busctrl = RegisterField(bool, 1);
    pub const adc = RegisterField(bool, 0);
};

pub const reset = struct {
    pub usingnamespace PeripheralRegister(base_address + 0x0);
    pub usingnamespace common;
};

pub const wdsel = struct {
    pub usingnamespace PeripheralRegister(base_address + 0x4);
    pub usingnamespace common;
};

pub const reset_done = struct {
    pub usingnamespace PeripheralRegister(base_address + 0x8);
    pub usingnamespace common;
};
