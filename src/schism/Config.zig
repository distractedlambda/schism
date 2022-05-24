const std = @import("std");

const rp2040 = @import("rp2040.zig");
const usb = @import("usb.zig");

core0_stack_top: usize = 0x20041000,
core1_stack_top: usize = 0x20042000,
gpio: [30]Gpio = [1]Gpio{.{}} ** 30,
usb: ?Usb = null,

pub const Gpio = struct {
    input_enabled: bool = true,
    output_enabled: bool = true,
    drive_strength: GpioDriveStrength = .@"4mA",
    pull_up_enabled: bool = false,
    pull_down_enabled: bool = true,
    schmitt_trigger_enabled: bool = true,
    slew_rate: GpioSlewRate = .Slow,
    input_override: GpioOverride = .None,
    output_override: GpioOverride = .None,
    output_enable_override: GpioOverride = .None,
    function: GpioFunction = .Null,
};

pub const Usb = union(enum) {
    Device: UsbDevice,
};

pub const UsbDevice = struct {
    language_id: UsbLanguageId = .EnglishUnitedStates,
    vendor_id: u16 = 0,
    product_id: u16 = 1,
    bcd_device: u16 = 0,
    manufacturer: ?[]const u8 = null,
    product: ?[]const u8 = null,
    serial_number: ?[]const u8 = null,
    interfaces: []const UsbInterface = &[_]UsbInterface{},

    pub fn addInterface(comptime self: *@This(), comptime interface: UsbInterface) u8 {
        comptime {
            defer self.interfaces = self.interfaces ++ &[_]UsbInterface{interface};
            return @intCast(u8, self.interfaces.len);
        }
    }
};

pub const UsbInterface = struct {
    name: ?[]const u8 = null,
    class: u8,
    subclass: u8,
    protocol: u8,
    endpoints: []const UsbEndpoint = &[_]UsbEndpoint{},

    pub fn addEndpoint(comptime self: *@This(), comptime endpoint: UsbEndpoint) u4 {
        comptime {
            defer self.endpoints = self.endpoints ++ &[_]UsbEndpoint{endpoint};
            return @intCast(u4, self.endpoints.len);
        }
    }
};

pub const UsbEndpoint = struct {
    direction: UsbTransferDirection,
};

pub const UsbTransferDirection = usb.protocol.EndpointDescriptor.EndpointAddress.Direction;

pub const UsbLanguageId = usb.protocol.LanguageId;

pub const GpioDriveStrength = rp2040.pads_bank0.DriveStrength;

pub const GpioSlewRate = rp2040.pads_bank0.SlewRate;

pub const GpioOverride = rp2040.io_bank0.Override;

pub const SpiFunction = struct {
    controller: u1,
    signal: SpiSignal,
};

pub const SpiSignal = enum(u4) {
    Rx,
    CsN,
    Sck,
    Tx,
};

pub const UartFunction = struct {
    controller: u1,
    signal: UartSignal,
};

pub const UartSignal = enum(u4) {
    Tx,
    Rx,
    Cts,
    Rts,
};

pub const I2cFunction = struct {
    controller: u1,
    signal: I2cSignal,
};

pub const I2cSignal = enum(u1) {
    Sda,
    Scl,
};

pub const PwmFunction = struct {
    controller: u3,
    channel: PwmChannel,
};

pub const PwmChannel = enum(u1) {
    A,
    B,
};

pub const SioFunction = struct {
    allow_yield_until_low: bool = false,
    allow_yield_until_high: bool = false,
};

pub const PioFunction = struct {
    controller: u1,
};

pub const ClockFunction = union(enum) {
    Gpin: u1,
    Gpout: u2,
};

pub const UsbFunction = enum {
    OvcurDet,
    VbusDet,
    VbusEn,
};

pub const GpioFunction = union(enum) {
    Spi: SpiFunction,
    Uart: UartFunction,
    I2c: I2cFunction,
    Pwm: PwmFunction,
    Sio: SioFunction,
    Pio: PioFunction,
    Clock: ClockFunction,
    Usb: UsbFunction,
    Null: void,

    pub fn funcsel(self: @This()) rp2040.io_bank0.Funcsel {
        return switch (self) {
            .Spi => .Spi,
            .Uart => .Uart,
            .I2c => .I2c,
            .Pwm => .Pwm,
            .Sio => .Sio,
            .Clock => .Clock,
            .Usb => .Usb,
            .Null => .Null,
            .Pio => |pio| switch (pio.controller) {
                0 => rp2040.io_bank0.Funcsel.Pio0,
                1 => rp2040.io_bank0.Funcsel.Pio1,
            },
        };
    }

    pub fn isValidFor(self: @This(), gpio_num: u5) bool {
        std.debug.assert(gpio_num < 30);
        return switch (self) {
            .Spi => |spi| spi.controller == gpio_num / 8 % 2 and @enumToInt(spi.signal) == gpio_num % 4,
            .Uart => |uart| uart.controller == (gpio_num +% 4) / 8 % 2 and @enumToInt(uart.signal) == gpio_num % 4,
            .I2c => |i2c| i2c.controller == gpio_num / 2 % 2 and @enumToInt(i2c.signal) == gpio_num % 2,
            .Pwm => |pwm| pwm.controller == gpio_num / 2 % 8 and @enumToInt(pwm.channel) == gpio_num % 2,
            .Sio => true,
            .Pio => true,
            .Clock => |clock| switch (clock) {
                .Gpin => |gpin| gpio_num == 20 + @as(u5, gpin) * 2,
                .Gpout => |gpout| switch (gpout) {
                    0 => gpio_num == 21,
                    else => gpio_num == 22 + @as(u5, gpout),
                },
            },
            .Usb => |func| gpio_num % 3 == @enumToInt(func),
            .Null => true,
        };
    }
};

pub const resolved: @This() = blk: {
    const config = @as(@This(), @import("root").schism_config);

    inline for (config.gpio) |gpio_config, gpio_num| {
        if (!gpio_config.function.isValidFor(gpio_num)) {
            @compileError(std.format.comptimePrint(
                "GPIO {} cannot be used with function {}",
                .{ gpio_num, gpio_config.function },
            ));
        }
    }

    break :blk config;
};
