const std = @import("std");

const rp2040 = @import("../rp2040/rp2040.zig");
const usb = @import("usb/usb.zig");

pub const Config = struct {
    core0_stack_top: usize = 0x20041000,
    core1_stack_top: usize = 0x20042000,
    gpio: [30]Gpio = [1]Gpio{.{}} ** 30,
    usb: ?UsbConfig = null,

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
            Spi: Spi,
            Uart: Uart,
            I2c: I2c,
            Pwm: Pwm,
            Sio: Sio,
            Pio: u1,
            Clock: Clock,
            Usb: UsbFunction,
            Null: void,

            pub const Spi = struct {
                controller: u1,
                signal: Signal,

                pub const Signal = enum(u4) {
                    Rx,
                    CsN,
                    Sck,
                    Tx,
                };
            };

            pub const Uart = struct {
                controller: u1,
                signal: Signal,

                pub const Signal = enum(u4) {
                    Tx,
                    Rx,
                    Cts,
                    Rts,
                };
            };

            pub const I2c = struct {
                controller: u1,
                signal: Signal,

                pub const Signal = enum {
                    Sda,
                    Scl,
                };
            };

            pub const Pwm = struct {
                controller: u3,
                channel: Channel,

                pub const Channel = enum {
                    A,
                    B,
                };
            };

            pub const Sio = struct {
                allow_yield_until_low: bool = false,
                allow_yield_until_high: bool = false,
            };

            pub const Clock = union(enum) {
                Gpin: u1,
                Gpout: u2,
            };

            pub const UsbFunction = enum {
                OvcurDet,
                VbusDet,
                VbusEn,
            };

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
                    .Pio => |pio| switch (pio) {
                        0 => rp2040.io_bank0.Funcsel.Pio0,
                        1 => .Pio1,
                    },
                };
            }

            pub fn isValidFor(comptime self: @This(), gpio_num: u5) bool {
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
    };

    pub const UsbConfig = union(enum) {
        Device: Device,

        pub const Device = struct {
            language_id: usb.protocol.LanguageId = .EnglishUnitedStates,
            vendor_id: u16 = 0,
            product_id: u16 = 0,
            bcd_device: u16 = 0,
            manufacturer: ?[]const u8 = null,
            product: ?[]const u8 = null,
            serial_number: ?[]const u8 = null,
            interfaces: []const Interface,
        };

        pub const Interface = struct {
            name: ?[]const u8 = null,
            class: u8,
            subclass: u8,
            protocol: u8,
            endpoints: []const Endpoint,
        };

        pub const Endpoint = struct {
            direction: usb.protocol.EndpointDescriptor.EndpointAddress.Direction,
        };
    };
};

pub const resolved: Config = blk: {
    const config = @as(Config, @import("root").schism_config);

    inline for (config.gpio) |gpio_config, gpio_num| {
        if (!gpio_config.function.isValidFor(gpio_num)) {
            @compileError(std.format.comptimePrint("GPIO {} cannot be used with function {}", .{ gpio_num, gpio_config.function }));
        }
    }

    break :blk config;
};