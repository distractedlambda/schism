const bits = @import("../bits.zig");

const PeripheralRegisterArray = @import("peripheral_register_array.zig").PeripheralRegisterArray;

const base_address = 0x4003c000;

const controller_stride = 0x40040000 - base_address;

pub const FrameFormat = enum(u2) {
    MotorolaSpi,
    TiSynchronousSerial,
    NationalMicrowire,
    _,
};

pub const sspcr0 = PeripheralRegisterArray(2, base_address + 0x000, controller_stride, .{
    .Record = &[_]bits.BitStructField{
        .{
            .name = "scr",
            .type = u8,
            .lsb = 8,
            .default = &@as(u8, 0),
        },
        .{
            .name = "sph",
            .type = u1,
            .lsb = 7,
            .default = &@as(u1, 0),
        },
        .{
            .name = "spo",
            .type = u1,
            .lsb = 6,
            .default = &@as(u1, 0),
        },
        .{
            .name = "frf",
            .type = FrameFormat,
            .lsb = 4,
            .default = &FrameFormat.MotorolaSpi,
        },
        .{
            .name = "dss",
            .type = u4,
            .lsb = 0,
            .default = &@as(u4, 0),
        },
    },
});

pub const ModeSelect = enum(u1) {
    Master,
    Slave,
};

pub const sppcr1 = PeripheralRegisterArray(2, base_address + 0x004, controller_stride, .{
    .Record = &[_]bits.BitStructField{
        .{
            .name = "sod",
            .type = bool,
            .lsb = 3,
            .default = &false,
        },
        .{
            .name = "ms",
            .type = ModeSelect,
            .lsb = 2,
            .default = &ModeSelect.Master,
        },
        .{
            .name = "sse",
            .type = bool,
            .lsb = 1,
            .default = &false,
        },
        .{
            .name = "lbm",
            .type = bool,
            .lsb = 0,
            .default = &false,
        },
    },
});

pub const sspdr = PeripheralRegisterArray(2, base_address + 0x008, controller_stride, .{
    .Scalar = u16,
});

pub const sspsr = PeripheralRegisterArray(2, base_address + 0x00c, controller_stride, .{
    .Record = &[_]bits.BitStructField{
        .{
            .name = "bsy",
            .type = bool,
            .lsb = 4,
        },
        .{
            .name = "rff",
            .type = bool,
            .lsb = 3,
        },
        .{
            .name = "rne",
            .type = bool,
            .lsb = 2,
        },
        .{
            .name = "tnf",
            .type = bool,
            .lsb = 1,
        },
        .{
            .name = "tff",
            .type = bool,
            .lsb = 0,
        },
    },
});

pub const sspcpsr = PeripheralRegisterArray(2, base_address + 0x010, controller_stride, .{
    .Scalar = u8,
});

const interrupt_spec = bits.BitStructSpec{
    .Record = &[_]bits.BitStructField{
        .{
            .name = "txim",
            .type = bool,
            .lsb = 3,
            .default = &false,
        },
        .{
            .name = "rxim",
            .type = bool,
            .lsb = 2,
            .default = &false,
        },
        .{
            .name = "rtim",
            .type = bool,
            .lsb = 1,
            .default = &false,
        },
        .{
            .name = "rorim",
            .type = bool,
            .lsb = 0,
            .default = &false,
        },
    },
};

pub const sspimsc = PeripheralRegisterArray(2, base_address + 0x014, controller_stride, interrupt_spec);

pub const sspris = PeripheralRegisterArray(2, base_address + 0x018, controller_stride, interrupt_spec);

pub const sspmis = PeripheralRegisterArray(2, base_address + 0x01c, controller_stride, interrupt_spec);

pub const sspicr = PeripheralRegisterArray(2, base_address + 0x020, controller_stride, .{
    .Record = &[_]bits.BitStructField{
        .{
            .name = "rtic",
            .type = bool,
            .lsb = 1,
            .default = &false,
        },
        .{
            .name = "roric",
            .type = bool,
            .lsb = 0,
            .default = &false,
        },
    },
});

pub const sspdmacr = PeripheralRegisterArray(2, base_address + 0x024, controller_stride, .{
    .Record = &[_]bits.BitStructField{
        .{
            .name = "txdmae",
            .type = bool,
            .lsb = 1,
            .default = &false,
        },
        .{
            .name = "rxdmae",
            .type = bool,
            .lsb = 0,
            .default = &false,
        },
    },
});
