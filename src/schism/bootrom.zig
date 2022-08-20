const rp2040 = @import("rp2040.zig");

var memcpy_impl: fn (noalias [*]u8, noalias [*]const u8, usize) callconv(.C) [*]u8 = missingMemcpy;
var memset_impl: fn ([*]u8, u8, usize) callconv(.C) [*]u8 = missingMemset;

pub fn init() void {
    for (link_records) |link_record| {
        if (rp2040.bootrom.lookUpFunction(link_record.table_code)) |impl| {
            link_record.destination.* = impl;
        }
    }
}

const link_records = [_]LinkRecord{
    .{
        .table_code = rp2040.bootrom.tableCode("MC"),
        .destination = @ptrCast(**const anyopaque, &memcpy_impl),
    },
    .{
        .table_code = rp2040.bootrom.tableCode("MS"),
        .destination = @ptrCast(**const anyopaque, &memset_impl),
    },
};

const LinkRecord = struct {
    table_code: u16,
    destination: **const anyopaque,
};

export fn memcpy(noalias destination: [*]u8, noalias source: [*]const u8, len: usize) callconv(.C) [*]u8 {
    // LLVM is failing to turn this into a tail call, so we could live with
    // that, or switch to global asm...
    return memcpy_impl(destination, source, len);
}

fn missingMemcpy(noalias destination: [*]u8, noalias source: [*]const u8, len: usize) callconv(.C) [*]u8 {
    _ = destination;
    _ = source;
    _ = len;
    @panic("memcpy implementation was not found in bootrom _or_ bootrom functions have not been linked");
}

export fn memset(destination: [*]u8, constant: c_int, len: usize) callconv(.C) [*]u8 {
    // LLVM is failing to turn this into a tail call, so we could live with
    // that, or switch to global asm...
    return memset_impl(destination, @truncate(u8, @bitCast(c_uint, constant)), len);
}

fn missingMemset(destination: [*]u8, constant: u8, len: usize) callconv(.C) [*]u8 {
    _ = destination;
    _ = constant;
    _ = len;
    @panic("memset implementation was not found in bootrom _or_ bootrom functions have not been linked");
}
