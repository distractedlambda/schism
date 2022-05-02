const std = @import("std");

const rp2040 = @import("../rp2040.zig");

pub const Config = struct {
    core0_stack_top: usize = 0x20042000,
    core0_stack_size: usize = 0x1000,
    core1_stack_top: usize = 0x20041000,
    core1_stack_size: usize = 0x1000,
    executor_spinlock: rp2040.sio.spinlock.Index = 0,
    shared_spinlock_start: rp2040.sio.spinlock.Index = 8,
    shared_spinlock_count: rp2040.sio.spinlock.Index = 24,
    gpio: [30]Gpio = [1]Gpio{.{}} ** 30,

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

        pub const Override = rp2040.io_bank0.gpio_ctrl.Override;

        pub const Function = union(enum) {
            Spi: Spi,
            Uart: Uart,
            I2c: I2c,
            Pwm: Pwm,
            Sio: void,
            Pio: u1,
            Clock: Clock,
            Usb: Usb,
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

            pub const Clock = union(enum) {
                Gpin: u1,
                Gpout: u2,
            };

            pub const Usb = enum {
                OvcurDet,
                VbusDet,
                VbusEn,
            };

            pub fn funcsel(self: @This()) u5 {
                return switch (self) {
                    .Spi => 1,
                    .Uart => 2,
                    .I2c => 3,
                    .Pwm => 4,
                    .Sio => 5,
                    .Pio => |pio| @as(u4, 6) + pio,
                    .Clock => 8,
                    .Usb => 9,
                    .Null => 31,
                };
            }

            pub fn isValidFor(gpio: u5) bool {
                std.debug.assert(gpio < 30);
                return switch (self) {
                    .Spi => |spi| spi.controller == gpio / 8 % 2 and @enumToInt(spi.signal) == gpio % 4,
                    .Uart => |uart| uart.controller == (gpio +% 4) / 8 % 2 and @enumToInt(uart.signal) == gpio % 4,
                    .I2c => |i2c| i2c.controller == gpio / 2 % 2 and @enumToInt(i2c.signal) == gpio % 2,
                    .Pwm => |pwm| pwm.controller == gpio / 2 % 8 and @enumToInt(pwm.channel) == gpio % 2,
                    .Sio => true,
                    .Pio => true,
                    .Clock => |clock| switch (clock) {
                        .Gpin => |gpin| gpio == 20 + @as(u5, gpin) * 2,
                        .Gpout => |gpout| switch (gpout) {
                            0 => gpio == 21,
                            else => gpio == 22 + @as(u5, gpout),
                        },
                    },
                    .Usb => |usb| gpio % 3 == @enumToInt(usb),
                    .Null => true,
                };
            }
        };
    };
};

pub const core0_stack_top = resolved.core0_stack_top;
pub const core0_stack_size = resolved.core0_stack_size;
pub const core1_stack_top = resolved.core1_stack_top;
pub const core1_stack_size = resolved.core1_stack_size;
pub const executor_spinlock = resolved.executor_spinlock;
pub const shared_spinlock_start = resolved.shared_spinlock_start;
pub const shared_spinlock_count = resolved.shared_spinlock_count;
pub const gpio = resolved.gpio;

const resolved: Config = blk: {
    const root = @import("root");
    if (@hasDecl(root, "runtime_config")) {
        break :blk root.runtime_config;
    } else {
        break :blk .{};
    }
};
