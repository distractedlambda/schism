pub const initial_stack_pointer = @intToPtr(*allowzero const u32, 0x00000000);
pub const reset_handler = @intToPtr(*const fn () void, 0x00000004);
pub const nmi_handler = @intToPtr(*const fn () void, 0x00000008);
pub const hard_fault_handler = @intToPtr(*const fn () void, 0x0000000c);
pub const magic = @intToPtr(*const [3]u8, 0x00000010);
pub const version = @intToPtr(*const u8, 0x00000013);
pub const func_table = @intToPtr(*const u16, 0x00000014);
pub const data_table = @intToPtr(*const u16, 0x00000016);
pub const table_lookup = @intToPtr(*const u16, 0x00000018);

pub fn tableCode(name: *const [2]u8) u16 {
    return (@as(u16, name[1]) << 8) | name[0];
}

pub fn tableLookupFn() (fn (*const u16, u32) callconv(.C) ?*const anyopaque) {
    return @intToPtr(fn (*const u16, u32) callconv(.C) ?*const anyopaque, table_lookup.*);
}

pub fn lookUpFunction(code: u16) ?*const anyopaque {
    return tableLookupFn()(@intToPtr(*const u16, func_table.*), code);
}

pub fn lookUpData(code: u16) ?*const anyopaque {
    return tableLookupFn()(@intToPtr(*const u16, data_table.*), code);
}
