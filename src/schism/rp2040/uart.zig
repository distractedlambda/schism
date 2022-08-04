const bits = @import("../bits.zig");

const PeripheralRegisterArray = @import("peripheral_register_array.zig").PeripheralRegisterArray;

const base_address = 0x40034000;

const controller_stride = 0x40038000 - base_address;

pub const uartdr = PeripheralRegisterArray(2, base_address + 0x000, controller_stride, .{
    .Record = &.{
        .{
            .name = "oe",
            .type = bool,
            .lsb = 11,
            .default = &false,
        },
        .{
            .name = "be",
            .type = bool,
            .lsb = 10,
            .default = &false,
        },
        .{
            .name = "pe",
            .type = bool,
            .lsb = 9,
            .default = &false,
        },
        .{
            .name = "fe",
            .type = bool,
            .lsb = 8,
            .default = &false,
        },
        .{
            .name = "data",
            .type = u8,
            .lsb = 0,
        },
    },
});

pub const uartrsr = PeripheralRegisterArray(2, base_address + 0x004, controller_stride, .{
    .Record = &.{
        .{
            .name = "oe",
            .type = bool,
            .lsb = 3,
            .default = &false,
        },
        .{
            .name = "be",
            .type = bool,
            .lsb = 2,
            .default = &false,
        },
        .{
            .name = "pe",
            .type = bool,
            .lsb = 1,
            .default = &false,
        },
        .{
            .name = "fe",
            .type = bool,
            .lsb = 0,
            .default = &false,
        },
    },
});

pub const uartfr = PeripheralRegisterArray(2, base_address + 0x018, controller_stride, .{
    .Record = &.{
        .{
            .name = "ri",
            .type = bool,
            .lsb = 8,
        },
        .{
            .name = "txfe",
            .type = bool,
            .lsb = 7,
        },
        .{
            .name = "rxff",
            .type = bool,
            .lsb = 6,
        },
        .{
            .name = "txff",
            .type = bool,
            .lsb = 5,
        },
        .{
            .name = "rxfe",
            .type = bool,
            .lsb = 4,
        },
        .{
            .name = "busy",
            .type = bool,
            .lsb = 3,
        },
        .{
            .name = "dcd",
            .type = bool,
            .lsb = 2,
        },
        .{
            .name = "dsr",
            .type = bool,
            .lsb = 1,
        },
        .{
            .name = "cts",
            .type = bool,
            .lsb = 0,
        },
    },
});

pub const uartilpr = PeripheralRegisterArray(2, base_address + 0x020, controller_stride, .{
    .Scalar = u8,
});

pub const uartibrd = PeripheralRegisterArray(2, base_address + 0x024, controller_stride, .{
    .Scalar = u16,
});

pub const uartfbrd = PeripheralRegisterArray(2, base_address + 0x028, controller_stride, .{
    .Scalar = u6,
});

pub const uartlcr_h = PeripheralRegisterArray(2, base_address + 0x02c, controller_stride, .{
    .Record = &.{
        .{
            .name = "sps",
            .type = bool,
            .lsb = 7,
            .default = &false,
        },
        .{
            .name = "wlen",
            .type = u2,
            .lsb = 5,
            .default = &@as(u2, 0),
        },
        .{
            .name = "fen",
            .type = bool,
            .lsb = 4,
            .default = &false,
        },
        .{
            .name = "stp2",
            .type = bool,
            .lsb = 3,
            .default = &false,
        },
        .{
            .name = "eps",
            .type = bool,
            .lsb = 2,
            .default = &false,
        },
        .{
            .name = "pen",
            .type = bool,
            .lsb = 1,
            .default = &false,
        },
        .{
            .name = "brk",
            .type = bool,
            .lsb = 0,
            .default = &false,
        },
    },
});

pub const uartcr = PeripheralRegisterArray(2, base_address + 0x030, controller_stride, .{
    .Record = &.{
        .{
            .name = "ctsen",
            .type = bool,
            .lsb = 15,
            .default = &false,
        },
        .{
            .name = "rtsen",
            .type = bool,
            .lsb = 14,
            .default = &false,
        },
        .{
            .name = "out2",
            .type = bool,
            .lsb = 13,
            .default = &false,
        },
        .{
            .name = "out1",
            .type = bool,
            .lsb = 12,
            .default = &false,
        },
        .{
            .name = "rts",
            .type = bool,
            .lsb = 11,
            .default = &false,
        },
        .{
            .name = "dtr",
            .type = bool,
            .lsb = 10,
            .default = &false,
        },
        .{
            .name = "rxe",
            .type = bool,
            .lsb = 9,
            .default = &true,
        },
        .{
            .name = "txe",
            .type = bool,
            .lsb = 8,
            .default = &true,
        },
        .{
            .name = "lbe",
            .type = bool,
            .lsb = 7,
            .default = &false,
        },
        .{
            .name = "sirlp",
            .type = bool,
            .lsb = 2,
            .default = &false,
        },
        .{
            .name = "siren",
            .type = bool,
            .lsb = 1,
            .default = &false,
        },
        .{
            .name = "uarten",
            .type = bool,
            .lsb = 0,
            .default = &false,
        },
    },
});

pub const uartifls = PeripheralRegisterArray(2, base_address + 0x034, controller_stride, .{
    .Record = &.{
        .{
            .name = "rxiflsel",
            .type = u3,
            .lsb = 3,
            .default = &@as(u3, 2),
        },
        .{
            .name = "txiflsel",
            .type = u3,
            .lsb = 0,
            .default = &@as(u3, 2),
        },
    },
});
