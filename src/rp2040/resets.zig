const registers = @import("../registers.zig");

const PeripheralRegister = registers.PeripheralRegister;

const base_address = 0x4000c000;

pub const Target = enum(u5) {
    Adc,
    Busctrl,
    Dma,
    I2c0,
    I2c1,
    IoBank0,
    IoQspi,
    Jtag,
    PadsBank0,
    PadsQspi,
    Pio0,
    Pio1,
    PllSys,
    PllUsb,
    Pwm,
    Trc,
    Spi0,
    Spi1,
    Syscfg,
    Sysinfo,
    Tbman,
    Timer,
    Uart0,
    Uart1,
    Usbctrl,
};

pub const reset = PeripheralRegister(base_address + 0x0);
pub const wdsel = PeripheralRegister(base_address + 0x4);
pub const reset_done = PeripheralRegister(base_address + 0x8);
