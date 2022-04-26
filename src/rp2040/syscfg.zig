const PeripheralRegister = @import("peripheral_register.zig").PeripheralRegister;
const PeripheralRegisterArray = @import("peripheral_register_array.zig").PeripheralRegisterArray;
const RegisterField = @import("register_field.zig").RegisterField;

const base_address = 0x40004000;

pub const proc_nmi_mask = PeripheralRegisterArray(2, base_address + 0x00, 0x4);

pub const proc_config = struct {
    pub usingnamespace PeripheralRegister(base_address + 0x08);

    pub const proc1_dap_instid = RegisterField(u4, 28);
    pub const proc0_dap_instid = RegisterField(u4, 24);
    pub const proc1_halted = RegisterField(bool, 1);
    pub const proc0_halted = RegisterField(bool, 0);
};

pub const proc_in_sync_bypass = PeripheralRegister(base_address + 0x0c);

pub const proc_in_sync_bypass_hi = PeripheralRegister(base_address + 0x10);

pub const dbgforce = struct {
    pub usingnamespace PeripheralRegister(base_address + 0x14);

    pub const proc1_attach = RegisterField(bool, 7);
    pub const proc1_swclk = RegisterField(bool, 6);
    pub const proc1_swdi = RegisterField(bool, 5);
    pub const proc1_swdo = RegisterField(bool, 4);
    pub const proc0_attach = RegisterField(bool, 3);
    pub const proc0_swclk = RegisterField(bool, 2);
    pub const proc0_swdi = RegisterField(bool, 1);
    pub const proc0_swdo = RegisterField(bool, 0);
};

pub const mempowerdown = struct {
    pub usingnamespace PeripheralRegister(base_address + 0x18);

    pub const rom = RegisterField(bool, 7);
    pub const usb = RegisterField(bool, 6);
    pub const sram5 = RegisterField(bool, 5);
    pub const sram4 = RegisterField(bool, 4);
    pub const sram3 = RegisterField(bool, 3);
    pub const sram2 = RegisterField(bool, 2);
    pub const sram1 = RegisterField(bool, 1);
    pub const sram0 = RegisterField(bool, 0);
};
