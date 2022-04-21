const mmio = @import("mmio.zig");

const base_address = 0xe0000000;

pub const systick = struct {
    pub const ClockSource = enum(u1) {
        external_reference,
        processor,
    };

    pub const Status = struct {
        enable: bool,
        tickint: bool,
        clksource: ClockSource,
        countflag: bool,
    };

    pub const Calibration = struct {
        tenms: u24,
        skew: bool,
        noref: bool,
    };

    const csr_address = base_address + 0xe010;
    const rvr_address = base_address + 0xe014;
    const cvr_address = base_address + 0xe018;
    const calib_address = base_address + 0xe01c;

    pub fn status() Status {
        const bits = mmio.read(csr_address);
        return .{
            .enable = (bits & 0x1) != 0,
            .tickint = (bits & 0x2) != 0,
            .clksource = @intToEnum(ClockSource, @truncate(u1, bits >> 2)),
            .countflag = (bits & 0x10000) != 0
        };
    }

    pub fn control(settings: struct {
        enable: bool,
        tickint: bool,
        clksource: ClockSource,
    }) void {
        const bits = @boolToInt(settings.enable) | (@boolToInt(settings.tickint) << 1) | (@enumToInt(settings.clksource) << 2);
        mmio.write(csr_address, bits);
    }

    pub fn setReloadValue(value: u24) void {
        mmio.write(rvr_address, value);
    }

    pub fn currentValue() u24 {
        return @truncate(u24, mmio.read(cvr_address));
    }

    pub fn clearCurrentValue() void {
        mmio.write(cvr_address);
    }

    pub fn calibration() Calibration {
        const bits = mmio.read(calib_address);
        return .{
            .tenms = @truncate(u24, bits),
            .skew = (bits >> 30) != 0,
            .noref = (bits >> 31) != 0,
        };
    }
};

pub const nvic = struct {
    const iser_address = base_address + 0xe100;
    const icer_address = base_address + 0xe180;
    const ispr_address = base_address + 0xe200;
    const icpr_address = base_address + 0xe280;
    const ipr_base_address = base_address + 0xe400;

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
};
