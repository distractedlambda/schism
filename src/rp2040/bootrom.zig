pub var _popcount32: fn (u32) callconv(.C) u32 = undefined;
pub var _reverse32: fn (u32) callconv(.C) u32 = undefined;
pub var _clz32: fn (u32) callconv(.C) u32 = undefined;
pub var _ctz32: fn (u32) callconv(.C) u32 = undefined;
pub var _memset: fn (*u8, u8, u32) callconv(.C) *u8 = undefined;
pub var _memset4: fn (*u32, u8, u32) callconv(.C) *u32 = undefined;
pub var _memcpy: fn (*u8, *u8, u32) callconv(.C) *u8 = undefined;
pub var _mempcy44: fn (*u32, *u32, u32) callconv(.C) *u8 = undefined;
pub var _connect_internal_flash: fn () callconv(.C) void = undefined;
pub var _flash_exit_xip: fn () callconv(.C) void = undefined;
pub var _flash_range_erase: fn (u32, u32, u32, u8) callconv(.C) void = undefined;
pub var _flash_range_program: fn (u32, *u8, u32) callconv(.C) void = undefined;
pub var _flash_flush_cache: fn () callconv(.C) void = undefined;
pub var _flash_enter_cmd_xip: fn () callconv(.C) void = undefined;
pub var _reset_to_usb_boot: fn (u32, u32) callconv(.C) noreturn = undefined;

pub fn link() void {
    @setRuntimeSafety(false);
    lookUpFunction("P3", &_popcount32);
    lookUpFunction("R3", &_reverse32);
    lookUpFunction("L3", &_clz32);
    lookUpFunction("T3", &_ctz32);
    lookUpFunction("MS", &_memset);
    lookUpFunction("M4", &_memset4);
    lookUpFunction("MC", &_memcpy);
    lookUpFunction("C4", &_mempcy44);
    lookUpFunction("IF", &_connect_internal_flash);
    lookUpFunction("EX", &_flash_exit_xip);
    lookUpFunction("RE", &_flash_range_erase);
    lookUpFunction("RP", &_flash_range_program);
    lookUpFunction("FC", &_flash_flush_cache);
    lookUpFunction("CX", &_flash_enter_cmd_xip);
    lookUpFunction("UB", &_reset_to_usb_boot);
}

const initial_stack_pointer = @intToPtr(*allowzero const u32, 0x00000000);
const reset_handler = @intToPtr(*const fn () void, 0x00000004);
const nmi_handler = @intToPtr(*const fn () void, 0x00000008);
const hard_fault_handler = @intToPtr(*const fn () void, 0x0000000c);
const magic = @intToPtr(*const [3]u8, 0x00000010);
const version = @intToPtr(*const u8, 0x00000013);
const func_table = @intToPtr(*const u16, 0x00000014);
const data_table = @intToPtr(*const u16, 0x00000016);
const table_lookup = @intToPtr(*const u16, 0x00000018);

fn tableCode(code: *const [2]u8) u32 {
    @setRuntimeSafety(false);
    return (@as(u32, code[1]) << 8) | code[0];
}

fn tableLookupFn() (fn (*const u16, u32) callconv(.C) ?*const u8) {
    @setRuntimeSafety(false);
    return @intToPtr(fn (*const u16, u32) callconv(.C) ?*const u8, table_lookup.*);
}

fn lookUpFunction(code: *const [2]u8, dst: anytype) void {
    @setRuntimeSafety(false);
    dst.* = @ptrCast(@typeInfo(@TypeOf(dst)).Pointer.child, tableLookupFn()(@intToPtr(*const u16, func_table.*), tableCode(code)));
}
