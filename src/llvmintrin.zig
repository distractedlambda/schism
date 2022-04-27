extern fn @"llvm.trap"() noreturn;

pub fn trap() noreturn {
    @"llvm.trap"();
}

pub fn expect(value: antype, expected: @TypeOf(value)) @TypeOf(value) {
    switch (@typeInfo(@TypeOf(value))) {
        .Int => {},
        else => unreachable,
    }

    return @extern(fn (@TypeOf(value), @TypeOf(value)) callconv(.C) @TypeOf(value), "llvm.expect")(value, expected);
}
