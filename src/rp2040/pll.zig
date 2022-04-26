const PeripheralRegister = @import("peripheral_register.zig").PeripheralRegister;
const RegisterField = @import("register_field.zig").RegisterField;

pub fn Pll(comptime base_address: u32) type {
    return struct {
        pub const cs = struct {
            pub usingnamespace PeripheralRegister(base_address + 0x0);

            pub const lock = RegisterField(bool, 31);
            pub const bypass = RegisterField(bool, 8);
            pub const refdiv = RegisterField(u6, 0);
        };

        pub const pwr = struct {
            pub usingnamespace PeripheralRegister(base_address + 0x4);

            pub const vcopd = RegisterField(bool, 5);
            pub const postdivpd = RegisterField(bool, 3);
            pub const dsmpd = RegisterField(bool, 2);
            pub const pd = RegisterField(bool, 0);
        };

        pub const fbdiv_int = struct {
            pub usingnamespace PeripheralRegister(base_address + 0x8);

            pub const value = RegisterField(u12, 0);
        };

        pub const prim = struct {
            pub usingnamespace PeripheralRegister(base_address + 0xc);

            pub const postdiv1 = RegisterField(u3, 16);
            pub const postdiv2 = RegisterField(u3, 12);
        };
    };
}
