const bits = @import("bits.zig");
const mmio = @import("mmio.zig");

const FixedPoint = @import("../../fixed_point.zig").FixedPoint;

const base_address = 0x40008000;

const enable_bit = 1 << 11;

pub const gpout = struct {
    pub const Source = enum(u4) {
        clksrc_pll_sys,
        clksrc_gpin0,
        clksrc_gpin1,
        clksrc_pll_usb,
        rosc_clksrc_ph,
        xosc_clksrc,
        clk_sys,
        clk_usb,
        clk_adc,
        clk_rtc,
        clk_ref,
    };

    pub const Divisor = FixedPoint(u32, -8);

    pub const Configuration = struct {
        phase_shift: u2 = 0,
        duty_cycle_correction: bool = true,
        source: Source,

        fn toInt(self: @This()) u32 {
            return (self.phase_shift << 16)
                | (@boolToInt(self.duty_cycle_correction) << 12)
                | (@enumToInt(self.source) << 5);
        }
    };

    fn ctrlAddress(index: u2) u32 {
        return base_address + @as(u32, index) * 0x0c;
    }

    pub fn enable(index: u2) void {
        mmio.peripheralSet(ctrlAddress(index), enable_bit);
    }

    pub fn disable(index: u2) void {
        mmio.peripheralSet(ctrlAddress(index), enable_bit);
    }

    pub fn disableAndConfigure(index: u2, configuartion: Configuration) void {
        mmio.write(ctrlAddress, configuartion.toInt());
    }

    fn divAddress(index: u2) u32 {
        return ctrlAddress(index) + 0x04;
    }

    pub fn divisor(index: u2) Divisor {
        return Divisor.new(mmio.read(divAddress(index)));
    }

    pub fn setDivisor(index: u2, divisor: Divisor) void {
        mmio.write(divAddress(index), divisor.significand);
    }
};

pub const ref = struct {
    pub const AuxiliarySource = enum(u2) {
        clksrc_pll_usb,
        clksrc_gpin0,
        clksrc_gpin1,
    };

    pub const Source = enum(u2) {
        rosc_clksrc_ph,
        clksrc_clk_ref_aux,
        xosc_clksrc,
    };

    pub const Divisor = u2;

    const ctrl_address = base_address + 0x30;
    const div_address = base_address + 0x34;
    const selected_address = base_address + 0x38;

    pub fn setSources(source: Source, auxiliary_source: AuxiliarySource) void {
        mmio.write(ctrl_address, @enumToInt(source) | (@enumToInt(auxiliary_source) << 5));
    }

    pub fn activeSource() ?Source {
        return switch (mmio.read(selected_address)) {
            0b000 => null,
            0b001 => .rosc_clksrc_ph,
            0b010 => .clksrc_clk_ref_aux,
            0b100 => .xosc_clksrc,
            else => unreachable,
        };
    }

    pub fn divisor() Divisor {
        return @truncate(Divisor, mmio.read(div_address) >> 8);
    }

    pub fn setDivisor(divisor: Divisor) void {
        mmio.write(div_address, divisor << 8);
    }
};

pub const sys = struct {
    pub const AuxiliarySource = enum(u3) {
        clksrc_pll_sys,
        clksrc_pll_usb,
        rosc_clksrc,
        xosc_clksrc,
        clksrc_gpin0,
        clksrc_gpin1,
    };

    pub const Source = enum(u1) {
        clk_ref,
        clksrc_clk_sys_aux,
    };

    pub const Divisor = FixedPoint(u32, -8);

    const ctrl_address = base_address + 0x3c;
    const div_address = base_address + 0x40;
    const selected_address = base_address + 0x44;

    pub fn setSources(source: Source, auxiliary_source: AuxiliarySource) void {
        mmio.write(ctrl_address, @enumToInt(source) | (@enumToInt(auxiliary_source) << 5));
    }

    pub fn activeSource() ?Source {
        return switch (mmio.read(selected_address)) {
            0b00 => null,
            0b01 => .clk_ref,
            0b10 => .clksrc_clk_sys_aux,
            else => unreachable,
        };
    }

    pub fn divisor() Divisor {
        return Divisor.new(mmio.read(div_address));
    }

    pub fn setDivisor(divisor: Divisor) void {
        mmio.write(div_address, divisor.significand);
    }
};

pub const peri = struct {
    pub const Source = enum(u3) {
        clk_sys,
        clksrc_pll_sys,
        clksrc_pll_usb,
        rosc_clksrc_ph,
        xosc_clksrc,
        clksrc_gpin0,
        clksrc_gpin1,
    };

    const ctrl_address = base_address + 0x48;

    pub fn disable() void {
        mmio.peripheralClear(ctrl_address, enable_bit);
    }

    pub fn enable() void {
        mmio.peripheralSet(ctrl_address, enable_bit);
    }

    pub fn disableAndSetSource(source: Source) void {
        mmio.write(ctrl_address, @enumToInt(source) << 5);
    }
};

pub const usb = struct {
    pub const Source = enum(u3) {
        clksrc_pll_usb,
        clksrc_pll_sys,
        rosc_clksrc_ph,
        xosc_clksrc,
        clksrc_gpin0,
        clksrc_gpin1,
    };

    pub const Configuration = struct {
        phase_shift: u2 = 0,
        source: Source,
    };

    pub const Divisor = u2;

    const ctrl_address = base_address + 0x54;
    const div_address = base_address + 0x58;

    pub fn disable() void {
        mmio.peripheralClear(ctrl_address, enable_bit);
    }

    pub fn enable() void {
        mmio.peripheralSet(ctrl_address, enable_bit);
    }

    pub fn disableAndConfigure(configuration: Configuration) void {
        mmio.write(ctrl_address, (configuration.phase_shift << 16) | (@enumToInt(configuration.source) << 5));
    }

    pub fn divisor() Divisor {
        return @truncate(Divisor, mmio.read(div_address) >> 8);
    }

    pub fn setDivisor(divisor: Divisor) void {
        mmio.write(div_address, divisor << 8);
    }
};

pub const adc = struct {
    pub const Source = enum(u3) {
        clksrc_pll_usb,
        clksrc_pll_sys,
        rosc_clksrc_ph,
        xosc_clksrc,
        clksrc_gpin0,
        clksrc_gpin1,
    };

    pub const Configuration = struct {
        phase_shift: u2 = 0,
        source: Source,
    };

    pub const Divisor = u2;

    const ctrl_address = base_address + 0x60;
    const div_address = base_address + 0x64;

    pub fn disable() void {
        mmio.peripheralClear(ctrl_address, enable_bit);
    }

    pub fn enable() void {
        mmio.peripheralSet(ctrl_address, enable_bit);
    }

    pub fn disableAndConfigure(configuration: Configuration) void {
        mmio.write(ctrl_address, (configuration.phase_shift << 16) | (@enumToInt(configuration.source) << 5));
    }

    pub fn divisor() Divisor {
        return @truncate(Divisor, mmio.read(div_address) >> 8);
    }

    pub fn setDivisor(divisor: Divisor) void {
        mmio.write(div_address, divisor << 8);
    }
};

pub const rtc = struct {
    pub const Source = enum(u3) {
        clksrc_pll_usb,
        clksrc_pll_sys,
        rosc_clksrc_ph,
        xosc_clksrc,
        clksrc_gpin0,
        clksrc_gpin1,
    };

    pub const Configuration = struct {
        phase_shift: u2 = 0,
        source: Source,
    };

    pub const Divisor = FixedPoint(u32, -8);

    const ctrl_address = base_address + 0x60;
    const div_address = base_address + 0x64;

    pub fn disable() void {
        mmio.peripheralClear(ctrl_address, enable_bit);
    }

    pub fn enable() void {
        mmio.peripheralSet(ctrl_address, enable_bit);
    }

    pub fn disableAndConfigure(configuration: Configuration) void {
        mmio.write(ctrl_address, (configuration.phase_shift << 16) | (@enumToInt(configuration.source) << 5));
    }

    pub fn divisor() Divisor {
        return Divisor.new(mmio.read(div_address));
    }

    pub fn setDivisor(divisor: Divisor) void {
        mmio.write(div_address, divisor.significand);
    }
};
