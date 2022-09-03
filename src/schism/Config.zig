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
    drive_strength: DriveStrength = .@"4mA",
    pull_up_enabled: bool = false,
    pull_down_enabled: bool = true,
    schmitt_trigger_enabled: bool = true,
    slew_rate: SlewRate = .Slow,
    input_override: Override = .None,
    output_override: Override = .None,
    output_enable_override: Override = .None,
    function: Function = .Null,

    pub const DriveStrength = rp2040.pads_bank0.DriveStrength;

    pub const SlewRate = rp2040.pads_bank0.SlewRate;

    pub const Override = rp2040.io_bank0.Override;

    pub const Function = union(enum) {
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

    pub const SpiFunction = struct {
        controller: u1,
        signal: Signal,

        pub const Signal = enum(u4) {
            Rx,
            CsN,
            Sck,
            Tx,
        };
    };

    pub const UartFunction = struct {
        controller: u1,
        signal: Signal,

        pub const Signal = enum(u4) {
            Tx,
            Rx,
            Cts,
            Rts,
        };
    };

    pub const I2cFunction = struct {
        controller: u1,
        signal: Signal,

        pub const Signal = enum(u1) {
            Sda,
            Scl,
        };
    };

    pub const PwmFunction = struct {
        controller: u3,
        channel: Channel,

        pub const Channel = enum(u1) {
            A,
            B,
        };
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
};

pub const Usb = union(enum) {
    Device: Device,

    pub const Device = struct {
        language_id: usb.LanguageId = .EnglishUnitedStates,
        vendor_id: u16 = 0,
        product_id: u16 = 1,
        bcd_device: u16 = 0,
        manufacturer: ?[]const u8 = null,
        product: ?[]const u8 = null,
        serial_number: ?[]const u8 = null,
        interfaces: []const Interface = &.{},
        control_request_handlers: []const usb.device.ControlRequestHandler = &.{},

        pub fn addInterface(comptime self: *@This(), comptime interface: Interface) u8 {
            comptime {
                defer self.interfaces = self.interfaces ++ &.{interface};
                return @intCast(u8, self.interfaces.len);
            }
        }

        pub fn addControlRequestHandler(
            comptime self: *@This(),
            comptime request: usb.SetupRequest,
            comptime func: fn (usb.SetupPacket, usb.device.ConnectionId) anyerror!void,
        ) void {
            comptime {
                const handler = usb.device.ControlRequestHandler{ request, func };
                self.control_request_handlers = self.control_request_handlers ++ &.{handler};
            }
        }
    };

    pub const Interface = struct {
        name: ?[]const u8 = null,
        class: usb.InterfaceClass,
        subclass: u8,
        protocol: u8,
        endpoints: []const Endpoint = &.{},

        pub fn addEndpoint(comptime self: *@This(), comptime endpoint: Endpoint) u4 {
            comptime {
                defer self.endpoints = self.endpoints ++ &.{endpoint};
                return @intCast(u4, self.endpoints.len);
            }
        }
    };

    pub const Endpoint = struct {
        direction: usb.EndpointDirection,
    };
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
