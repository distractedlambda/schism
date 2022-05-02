const std = @import("std");

const PeripheralRegisterArray = @import("peripheral_register_array.zig").PeripheralRegisterArray;
const PeripheralRegisterMatrix = @import("peripheral_register_matrix.zig").PeripheralRegisterMatrix;
const RegisterField = @import("register_field.zig").RegisterField;

const base_address = 0x40014000;

pub const InterruptKind = enum(u2) {
    LevelLow,
    LevelHigh,
    EdgeLow,
    EdgeHigh,
};

pub fn interruptBitIndex(gpio: u5, kind: InterruptKind) u32 {
    std.debug.assert(gpio <= 30);
    return @as(u32, gpio) * 4 + @enumToInt(kind);
}

pub const gpio_status = struct {
    pub usingnamespace PeripheralRegisterArray(30, base_address, 0x8);

    pub const irqtoproc = RegisterField(bool, 26);
    pub const irqfrompad = RegisterField(bool, 24);
    pub const intoperi = RegisterField(bool, 19);
    pub const infrompad = RegisterField(bool, 17);
    pub const oetopad = RegisterField(bool, 13);
    pub const oefromperi = RegisterField(bool, 12);
    pub const outtopad = RegisterField(bool, 9);
    pub const outfromperi = RegisterField(bool, 8);
};

pub const gpio_ctrl = struct {
    pub usingnamespace PeripheralRegisterArray(30, base_address + 0x004, 0x8);

    pub const Override = enum(u2) {
        None,
        Invert,
        DriveLow,
        DriveHigh,
    };

    pub const irqover = RegisterField(Override, 28);
    pub const inover = RegisterField(Override, 16);
    pub const oeover = RegisterField(Override, 12);
    pub const outover = RegisterField(Override, 8);

    const funcsel_table = [30][9]?[]const u8{
        [9]?[]const u8{ "Spi0Rx", "Uart0Tx", "I2C0Sda", "Pwm0A", "Sio", "Pio0", "Pio1", null, "UsbOvcurDet" },
        [9]?[]const u8{ "Spi0CsN", "Uart0Rx", "I2C0Scl", "Pwm0B", "Sio", "Pio0", "Pio1", null, "UsbVbusDet" },
        [9]?[]const u8{ "Spi0Sck", "Uart0Cts", "I2C1Sda", "Pwm1A", "Sio", "Pio0", "Pio1", null, "UsbVbusEn" },
        [9]?[]const u8{ "Spi0Tx", "Uart0Rts", "I2C1Scl", "Pwm1B", "Sio", "Pio0", "Pio1", null, "UsbOvcurDet" },
        [9]?[]const u8{ "Spi0Rx", "Uart1Tx", "I2C0Sda", "Pwm2A", "Sio", "Pio0", "Pio1", null, "UsbVbusDet" },
        [9]?[]const u8{ "Spi0CsN", "Uart1Rx", "I2C0Scl", "Pwm2B", "Sio", "Pio0", "Pio1", null, "UsbVbusEn" },
        [9]?[]const u8{ "Spi0Sck", "Uart1Cts", "I2C1Sda", "Pwm3A", "Sio", "Pio0", "Pio1", null, "UsbOvcurDet" },
        [9]?[]const u8{ "Spi0Tx", "Uart1Rts", "I2C1Scl", "Pwm3B", "Sio", "Pio0", "Pio1", null, "UsbVbusDet" },
        [9]?[]const u8{ "Spi1Rx", "Uart1Tx", "I2C0Sda", "Pwm4A", "Sio", "Pio0", "Pio1", null, "UsbVbusEn" },
        [9]?[]const u8{ "Spi1CsN", "Uart1Rx", "I2C0Scl", "Pwm4B", "Sio", "Pio0", "Pio1", null, "UsbOvcurDet" },
        [9]?[]const u8{ "Spi1Sck", "Uart1Cts", "I2C1Sda", "Pwm5A", "Sio", "Pio0", "Pio1", null, "UsbVbusDet" },
        [9]?[]const u8{ "Spi1Tx", "Uart1Rts", "I2C1Scl", "Pwm5B", "Sio", "Pio0", "Pio1", null, "UsbVbusEn" },
        [9]?[]const u8{ "Spi1Rx", "Uart0Tx", "I2C0Sda", "Pwm6A", "Sio", "Pio0", "Pio1", null, "UsbOvcurDet" },
        [9]?[]const u8{ "Spi1CsN", "Uart0Rx", "I2C0Scl", "Pwm6B", "Sio", "Pio0", "Pio1", null, "UsbVbusDet" },
        [9]?[]const u8{ "Spi1Sck", "Uart0Cts", "I2C1Sda", "Pwm7A", "Sio", "Pio0", "Pio1", null, "UsbVbusEn" },
        [9]?[]const u8{ "Spi1Tx", "Uart0Rts", "I2C1Scl", "Pwm7B", "Sio", "Pio0", "Pio1", null, "UsbOvcurDet" },
        [9]?[]const u8{ "Spi0Rx", "Uart0Tx", "I2C0Sda", "Pwm0A", "Sio", "Pio0", "Pio1", null, "UsbVbusDet" },
        [9]?[]const u8{ "Spi0CsN", "Uart0Rx", "I2C0Scl", "Pwm0B", "Sio", "Pio0", "Pio1", null, "UsbVbusEn" },
        [9]?[]const u8{ "Spi0Sck", "Uart0Cts", "I2C1Sda", "Pwm1A", "Sio", "Pio0", "Pio1", null, "UsbOvcurDet" },
        [9]?[]const u8{ "Spi0Tx", "Uart0Rts", "I2C1Scl", "Pwm1B", "Sio", "Pio0", "Pio1", null, "UsbVbusDet" },
        [9]?[]const u8{ "Spi0Rx", "Uart1Tx", "I2C0Sda", "Pwm2A", "Sio", "Pio0", "Pio1", "ClockGpin0", "UsbVbusEn" },
        [9]?[]const u8{ "Spi0CsN", "Uart1Rx", "I2C0Scl", "Pwm2B", "Sio", "Pio0", "Pio1", "ClockGpout0", "UsbOvcurDet" },
        [9]?[]const u8{ "Spi0Sck", "Uart1Cts", "I2C1Sda", "Pwm3A", "Sio", "Pio0", "Pio1", "ClockGpin1", "UsbVbusDet" },
        [9]?[]const u8{ "Spi0Tx", "Uart1Rts", "I2C1Scl", "Pwm3B", "Sio", "Pio0", "Pio1", "ClockGpout1", "UsbVbusEn" },
        [9]?[]const u8{ "Spi1Rx", "Uart1Tx", "I2C0Sda", "Pwm4A", "Sio", "Pio0", "Pio1", "ClockGpout2", "UsbOvcurDet" },
        [9]?[]const u8{ "Spi1CsN", "Uart1Rx", "I2C0Scl", "Pwm4B", "Sio", "Pio0", "Pio1", "ClockGpout3", "UsbVbusDet" },
        [9]?[]const u8{ "Spi1Sck", "Uart1Cts", "I2C1Sda", "Pwm5A", "Sio", "Pio0", "Pio1", null, "UsbVbusEn" },
        [9]?[]const u8{ "Spi1Tx", "Uart1Rts", "I2C1Scl", "Pwm5B", "Sio", "Pio0", "Pio1", null, "UsbOvcurDet" },
        [9]?[]const u8{ "Spi1Rx", "Uart0Tx", "I2C0Sda", "Pwm6A", "Sio", "Pio0", "Pio1", null, "UsbVbusDet" },
        [9]?[]const u8{ "Spi1CsN", "Uart0Rx", "I2C0Scl", "Pwm6B", "Sio", "Pio0", "Pio1", null, "UsbVbusEn" },
    };

    pub fn funcsel(comptime index: u5) type {
        comptime var enum_fields: [9]std.builtin.Type.EnumField = undefined;
        comptime var next_enum_field: comptime_int = 0;

        inline for (funcsel_table[index]) |field_name, i| {
            if (field_name) |fname| {
                enum_fields[next_enum_field] = .{ .name = fname, .value = i + 1 };
                next_enum_field += 1;
            }
        }

        const enum_type = @Type(.{ .Enum = .{
            .layout = .Auto,
            .tag_type = u5,
            .fields = enum_fields[0..next_enum_field],
            .decls = &[_]std.builtin.Type.Declaration{},
            .is_exhaustive = true,
        } });

        return RegisterField(enum_type, 0);
    }
};

pub const intr = PeripheralRegisterArray(4, base_address + 0x0f0, 0x4);

pub const proc_inte = PeripheralRegisterMatrix(2, 30, base_address + 0x100, 0x30, 0x4);
pub const proc_intf = PeripheralRegisterMatrix(2, 30, base_address + 0x110, 0x30, 0x4);
pub const proc_ints = PeripheralRegisterMatrix(2, 30, base_address + 0x120, 0x30, 0x4);

pub const dormant_wake_inte = PeripheralRegisterArray(30, base_address + 0x160, 0x4);
pub const dormant_wake_intf = PeripheralRegisterArray(30, base_address + 0x170, 0x4);
pub const dormant_wake_ints = PeripheralRegisterArray(30, base_address + 0x180, 0x4);
