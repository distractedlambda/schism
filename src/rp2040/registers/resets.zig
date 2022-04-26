const registers = @import("../registers.zig");

const PeripheralRegister = registers.PeripheralRegister;

const base_address = 0x4000c000;

pub const Target = enum(u5) {
    adc,
    busctrl,
    dma,
    i2c0,
    i2c1,
    io_bank0,
    io_qspi,
    jtag,
    pads_bank0,
    pads_qspi,
    pio0,
    pio1,
    pll_sys,
    pll_usb,
    pwm,
    trc,
    spi0,
    spi1,
    syscfg,
    sysinfo,
    tbman,
    timer,
    uart0,
    uart1,
    usbctrl,
};

pub const reset = PeripheralRegister(base_address + 0x0);
pub const wdsel = PeripheralRegister(base_address + 0x4);
pub const reset_done = PeripheralRegister(base_address + 0x8);
