const rp2040 = @import("../rp2040.zig");

pub var _popcount32: fn (u32) callconv(.C) u32 = undefined;
pub var _reverse32: fn (u32) callconv(.C) u32 = undefined;
pub var _clz32: fn (u32) callconv(.C) u32 = undefined;
pub var _ctz32: fn (u32) callconv(.C) u32 = undefined;
pub var _memset: fn (*u8, u8, u32) callconv(.C) *u8 = undefined;
pub var _memcpy: fn (*u8, *u8, u32) callconv(.C) *u8 = undefined;
pub var _connect_internal_flash: fn () callconv(.C) void = undefined;
pub var _flash_exit_xip: fn () callconv(.C) void = undefined;
pub var _flash_range_erase: fn (u32, u32, u32, u8) callconv(.C) void = undefined;
pub var _flash_range_program: fn (u32, *u8, u32) callconv(.C) void = undefined;
pub var _flash_flush_cache: fn () callconv(.C) void = undefined;
pub var _flash_enter_cmd_xip: fn () callconv(.C) void = undefined;
pub var _reset_to_usb_boot: fn (u32, u32) callconv(.C) noreturn = undefined;

pub fn link() void {
    lookUpFunction("P3", &_popcount32);
    lookUpFunction("R3", &_reverse32);
    lookUpFunction("L3", &_clz32);
    lookUpFunction("T3", &_ctz32);
    lookUpFunction("MS", &_memset);
    lookUpFunction("MC", &_memcpy);
    lookUpFunction("IF", &_connect_internal_flash);
    lookUpFunction("EX", &_flash_exit_xip);
    lookUpFunction("RE", &_flash_range_erase);
    lookUpFunction("RP", &_flash_range_program);
    lookUpFunction("FC", &_flash_flush_cache);
    lookUpFunction("CX", &_flash_enter_cmd_xip);
    lookUpFunction("UB", &_reset_to_usb_boot);
}

fn tableCode(code: *const [2]u8) u32 {
    return (@as(u32, code[1]) << 8) | code[0];
}

fn tableLookupFn() (fn (*const u16, u32) callconv(.C) ?*const u8) {
    return @intToPtr(fn (*const u16, u32) callconv(.C) ?*const u8, rp2040.bootrom.table_lookup.*);
}

fn lookUpFunction(code: *const [2]u8, dst: anytype) void {
    dst.* = @ptrCast(@typeInfo(@TypeOf(dst)).Pointer.child, tableLookupFn()(@intToPtr(*const u16, rp2040.bootrom.func_table.*), tableCode(code)));
}
