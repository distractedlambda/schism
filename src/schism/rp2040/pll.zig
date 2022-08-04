const bits = @import("../bits.zig");

const PeripheralRegister = @import("peripheral_register.zig").PeripheralRegister;

pub fn Pll(comptime base_address: u32) type {
    return struct {
        pub const cs = PeripheralRegister(base_address + 0x0, .{
            .Record = &.{
                .{
                    .name = "lock",
                    .type = bool,
                    .lsb = 31,
                    .default = &false,
                },
                .{
                    .name = "bypass",
                    .type = bool,
                    .lsb = 8,
                    .default = &false,
                },
                .{
                    .name = "refdiv",
                    .type = u6,
                    .lsb = 0,
                    .default = &@as(u6, 1),
                },
            },
        });

        pub const pwr = PeripheralRegister(base_address + 0x4, .{
            .Record = &.{
                .{
                    .name = "vcopd",
                    .type = bool,
                    .lsb = 5,
                    .default = &true,
                },
                .{
                    .name = "postdivpd",
                    .type = bool,
                    .lsb = 3,
                    .default = &true,
                },
                .{
                    .name = "dsmpd",
                    .type = bool,
                    .lsb = 2,
                    .default = &true,
                },
                .{
                    .name = "pd",
                    .type = bool,
                    .lsb = 0,
                    .default = &true,
                },
            },
        });

        pub const fbdiv_int = PeripheralRegister(base_address + 0x8, .{
            .Scalar = u12,
        });

        pub const prim = PeripheralRegister(base_address + 0xc, .{
            .Record = &.{
                .{
                    .name = "postdiv1",
                    .type = u3,
                    .lsb = 16,
                    .default = &@as(u3, 0x7),
                },
                .{
                    .name = "postdiv2",
                    .type = u3,
                    .lsb = 12,
                    .default = &@as(u3, 0x7),
                },
            },
        });
    };
}
