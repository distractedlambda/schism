const std = @import("std");

const registers = @import("../registers.zig");

const PeripheralRegisterArray = registers.PeripheralRegisterArray;
const PeripheralRegisterMatrix = registers.PeripheralRegisterMatrix;
const RegisterField = registers.RegisterField;

const base_address = 0x40014000;

pub const InterruptKind = enum(u2) {
    level_low,
    level_high,
    edge_low,
    edge_high,
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
        none,
        invert,
        drive_low,
        drive_high,
    };

    pub const irqover = RegisterField(Override, 28);
    pub const inover = RegisterField(Override, 16);
    pub const oeover = RegisterField(Override, 12);
    pub const outover = RegisterField(Override, 8);

    const funcsel_table = [30][9]?[]const u8{
        [9]?[]const u8{ "spi0_rx", "uart0_tx", "i2c0_sda", "pwm0_a", "sio", "pio0", "pio1", null, "usb_ovcur_det" },
        [9]?[]const u8{ "spi0_csn", "uart0_rx", "i2c0_scl", "pwm0_b", "sio", "pio0", "pio1", null, "usb_vbus_det" },
        [9]?[]const u8{ "spi0_sck", "uart0_cts", "i2c1_sda", "pwm1_a", "sio", "pio0", "pio1", null, "usb_vbus_en" },
        [9]?[]const u8{ "spi0_tx", "uart0_rts", "i2c1_scl", "pwm1_b", "sio", "pio0", "pio1", null, "usb_ovcur_det" },
        [9]?[]const u8{ "spi0_rx", "uart1_tx", "i2c0_sda", "pwm2_a", "sio", "pio0", "pio1", null, "usb_vbus_det" },
        [9]?[]const u8{ "spi0_csn", "uart1_rx", "i2c0_scl", "pwm2_b", "sio", "pio0", "pio1", null, "usb_vbus_en" },
        [9]?[]const u8{ "spi0_sck", "uart1_cts", "i2c1_sda", "pwm3_a", "sio", "pio0", "pio1", null, "usb_ovcur_det" },
        [9]?[]const u8{ "spi0_tx", "uart1_rts", "i2c1_scl", "pwm3_b", "sio", "pio0", "pio1", null, "usb_vbus_det" },
        [9]?[]const u8{ "spi1_rx", "uart1_tx", "i2c0_sda", "pwm4_a", "sio", "pio0", "pio1", null, "usb_vbus_en" },
        [9]?[]const u8{ "spi1_csn", "uart1_rx", "i2c0_scl", "pwm4_b", "sio", "pio0", "pio1", null, "usb_ovcur_det" },
        [9]?[]const u8{ "spi1_sck", "uart1_cts", "i2c1_sda", "pwm5_a", "sio", "pio0", "pio1", null, "usb_vbus_det" },
        [9]?[]const u8{ "spi1_tx", "uart1_rts", "i2c1_scl", "pwm5_b", "sio", "pio0", "pio1", null, "usb_vbus_en" },
        [9]?[]const u8{ "spi1_rx", "uart0_tx", "i2c0_sda", "pwm6_a", "sio", "pio0", "pio1", null, "usb_ovcur_det" },
        [9]?[]const u8{ "spi1_csn", "uart0_rx", "i2c0_scl", "pwm6_b", "sio", "pio0", "pio1", null, "usb_vbus_det" },
        [9]?[]const u8{ "spi1_sck", "uart0_cts", "i2c1_sda", "pwm7_a", "sio", "pio0", "pio1", null, "usb_vbus_en" },
        [9]?[]const u8{ "spi1_tx", "uart0_rts", "i2c1_scl", "pwm7_b", "sio", "pio0", "pio1", null, "usb_ovcur_det" },
        [9]?[]const u8{ "spi0_rx", "uart0_tx", "i2c0_sda", "pwm0_a", "sio", "pio0", "pio1", null, "usb_vbus_det" },
        [9]?[]const u8{ "spi0_csn", "uart0_rx", "i2c0_scl", "pwm0_b", "sio", "pio0", "pio1", null, "usb_vbus_en" },
        [9]?[]const u8{ "spi0_sck", "uart0_cts", "i2c1_sda", "pwm1_a", "sio", "pio0", "pio1", null, "usb_ovcur_det" },
        [9]?[]const u8{ "spi0_tx", "uart0_rts", "i2c1_scl", "pwm1_b", "sio", "pio0", "pio1", null, "usb_vbus_det" },
        [9]?[]const u8{ "spi0_rx", "uart1_tx", "i2c0_sda", "pwm2_a", "sio", "pio0", "pio1", "clock_gpin0", "usb_vbus_en" },
        [9]?[]const u8{ "spi0_csn", "uart1_rx", "i2c0_scl", "pwm2_b", "sio", "pio0", "pio1", "clock_gpout0", "usb_ovcur_det" },
        [9]?[]const u8{ "spi0_sck", "uart1_cts", "i2c1_sda", "pwm3_a", "sio", "pio0", "pio1", "clock_gpin1", "usb_vbus_det" },
        [9]?[]const u8{ "spi0_tx", "uart1_rts", "i2c1_scl", "pwm3_b", "sio", "pio0", "pio1", "clock_gpout1", "usb_vbus_en" },
        [9]?[]const u8{ "spi1_rx", "uart1_tx", "i2c0_sda", "pwm4_a", "sio", "pio0", "pio1", "clock_gpout2", "usb_ovcur_det" },
        [9]?[]const u8{ "spi1_csn", "uart1_rx", "i2c0_scl", "pwm4_b", "sio", "pio0", "pio1", "clock_gpout3", "usb_vbus_det" },
        [9]?[]const u8{ "spi1_sck", "uart1_cts", "i2c1_sda", "pwm5_a", "sio", "pio0", "pio1", null, "usb_vbus_en" },
        [9]?[]const u8{ "spi1_tx", "uart1_rts", "i2c1_scl", "pwm5_b", "sio", "pio0", "pio1", null, "usb_ovcur_det" },
        [9]?[]const u8{ "spi1_rx", "uart0_tx", "i2c0_sda", "pwm6_a", "sio", "pio0", "pio1", null, "usb_vbus_det" },
        [9]?[]const u8{ "spi1_csn", "uart0_rx", "i2c0_scl", "pwm6_b", "sio", "pio0", "pio1", null, "usb_vbus_en" },
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
