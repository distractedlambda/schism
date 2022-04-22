const std = @import("std");

pub fn get(word: anytype, index: comptime_int) bool {
    comptime std.debug.assert(index >= 0);

    const word_type_info = @typeInfo(@TypeOf(word));
    comptime std.debug.assert(word_type_info.Int.signedness == .unsigned);
    comptime std.debug.assert(index < word_type_info.Int.bits);

    return (word >> index) != 0;
}

pub fn slice(word: anytype, high: comptime_int, low: comptime_int) std.meta.Int(.unsigned, high - low + 1) {
    comptime std.debug.assert(low <= high);
    comptime std.debug.assert(low >= 0);

    const word_type_info = @typeInfo(@TypeOf(word));
    comptime std.debug.assert(word_type_info.Int.signedness == .unsigned);
    comptime std.debug.assert(high < word_type_info.Int.bits);

    return @truncate(std.meta.Int(.unsigned, high - low + 1), word >> low);
}
