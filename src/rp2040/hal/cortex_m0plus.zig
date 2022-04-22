const std = @import("std");

const bits = @import("bits.zig");
const mmio = @import("mmio.zig");

const base_address = 0xe0000000;

pub const systick = struct {
    pub const ClockSource = enum(u1) {
        external_reference,
        processor,
    };

    pub const Status = struct {
        countflag: bool,
        clksource: ClockSource,
        tickint: bool,
        enable: bool,
    };

    pub const Calibration = struct {
        noref: bool,
        skew: bool,
        tenms: u24,
    };

    const csr_address = base_address + 0xe010;
    const rvr_address = base_address + 0xe014;
    const cvr_address = base_address + 0xe018;
    const calib_address = base_address + 0xe01c;

    pub fn status() Status {
        const value = mmio.read(csr_address);
        return .{
            .countflag = bits.get(value, 16),
            .clksource = @intToEnum(ClockSource, bits.slice(value, 2, 2)),
            .tickint = bits.get(value, 1),
            .enable = bits.get(value, 0),
        };
    }

    pub fn control(settings: struct {
        clksource: ClockSource,
        tickint: bool,
        enable: bool,
    }) void {
        const value = @boolToInt(settings.enable) | (@boolToInt(settings.tickint) << 1) | (@enumToInt(settings.clksource) << 2);
        mmio.write(csr_address, value);
    }

    pub fn setReloadValue(value: u24) void {
        mmio.write(rvr_address, value);
    }

    pub fn currentValue() u24 {
        return bits.slice(mmio.read(cvr_address), 23, 0);
    }

    pub fn clearCurrentValue() void {
        mmio.write(cvr_address);
    }

    pub fn calibration() Calibration {
        const value = mmio.read(calib_address);
        return .{
            .noref = bits.get(value, 31),
            .skew = bits.get(value, 30),
            .tenms = bits.slice(value, 23, 0),
        };
    }
};

pub const nvic = struct {
    pub const State = struct {
        nmipendset: bool,
        pendsvset: bool,
        pendstset: bool,
        isrpreempt: bool,
        isrpending: bool,
        vectpending: u9,
        vectactive: u9,
    };

    const iser_address = base_address + 0xe100;
    const icer_address = base_address + 0xe180;
    const ispr_address = base_address + 0xe200;
    const icpr_address = base_address + 0xe280;
    const ipr_base_address = base_address + 0xe400;
    const icsr_address = base_address + 0xed04;
    const vtor_address = base_address + 0xed08;

    pub fn enabledMask() u32 {
        return mmio.read(iser_address);
    }

    pub fn pendingMask() u32 {
        return mmio.read(ispr_address);
    }

    pub fn enable(mask: u32) void {
        mmio.write(iser_address, mask);
    }

    pub fn disable(mask: u32) void {
        mmio.write(icer_address, mask);
    }

    pub fn makePending(mask: u32) void {
        mmio.write(ispr_address, mask);
    }

    pub fn makeNotPending(mask: u32) void {
        mmio.write(icpr_address, mask);
    }

    pub fn setPriorities(priorities: [32]u2) void {
        var i = 0;
        while (i < 8) : (i += 1) {
            const word = (priorities[i * 4 + 0] << 6) | (priorities[i * 4 + 1] << 14) | (priorities[i * 4 + 2] << 22) | (priorities[i * 4 + 3] << 30);
            mmio.write(ipr_base_address + i * 4, word);
        }
    }

    pub fn state() State {
        const value = mmio.read(icsr_address);
        return .{
            .nmipendset = bits.get(value, 31),
            .pendsvset = bits.get(value, 28),
            .pendstset = bits.get(value, 26),
            .isrpreempt = bits.get(value, 23),
            .isrpending = bits.get(value, 22),
            .vectpending = bits.slice(value, 20, 12),
            .vectactive = bits.slice(value, 8, 0),
        };
    }

    pub fn vectorTableOffset() u32 {
        return mmio.read(vtor_address) & ~@as(u32, 0xFF);
    }

    pub fn setVectorTableOffset(offset: u32) void {
        std.debug.assert(bits.slice(offset, 7, 0) == 0);
        mmio.write(vtor_address, offset);
    }
};
