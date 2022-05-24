const std = @import("std");

pub fn log(
    comptime level: std.log.Level,
    comptime scope: @Type(.EnumLiteral),
    comptime format: []const u8,
    args: anytype,
) void {

}

const Writer = struct {

};

const Level = enum(u2) {
    err,
    warn,
    info,
    debug,
};

const MessageHeader = packed struct {
    level: Level,
    scope_len_minus_one: u6,
    message_len: u24,
};
