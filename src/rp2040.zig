pub const bootrom = @import("rp2040/bootrom.zig");
pub const dma = @import("rp2040/dma.zig");
pub const io_bank0 = @import("rp2040/io_bank0.zig");
pub const pads_bank0 = @import("rp2040/pads_bank0.zig");
pub const pll_sys = @import("rp2040/pll_sys.zig");
pub const pll_usb = @import("rp2040/pll_usb.zig");
pub const ppb = @import("rp2040/ppb.zig");
pub const psm = @import("rp2040/psm.zig");
pub const pwm = @import("rp2040/pwm.zig");
pub const resets = @import("rp2040/resets.zig");
pub const rosc = @import("rp2040/rosc.zig");
pub const sio = @import("rp2040/sio.zig");
pub const syscfg = @import("rp2040/syscfg.zig");
pub const vreg_and_chip_reset = @import("rp2040/vreg_and_chip_reset.zig");
pub const xosc = @import("rp2040/xosc.zig");

pub const Irq = enum(u5) {
    TimerIrq0,
    TimerIrq1,
    TimerIrq2,
    TimerIrq3,
    PwmWrap,
    Usbctrl,
    Xip,
    Pio0Irq0,
    Pio0Irq1,
    Pio1Irq0,
    Pio1Irq1,
    DmaIrq0,
    DmaIrq1,
    IoBank0,
    IoQspi,
    SioProc0,
    SioProc1,
    Clocks,
    Spi0,
    Spi1,
    Uart0,
    Uart1,
    AdcFifo,
    I2c0,
    I2c1,
    Rtc,
};
