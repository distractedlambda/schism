const std = @import("std");
const rp2040 = @import("../rp2040/rp2040.zig");

comptime {
    @export(idiv, .{ .name = "__aeabi_idiv", .linkage = .Weak });
    @export(idivmod, .{ .name = "__aeabi_idivmod", .linkage = .Weak });
    @export(uidiv, .{ .name = "__aeabi_uidiv", .linkage = .Weak });
    @export(uidivmod, .{ .name = "__aeabi_uidivmod", .linkage = .Weak });
}

fn idiv(dividend: i32, divisor: i32) callconv(.C) i32 {
    rp2040.sio.div_sdividend.write(dividend);
    rp2040.sio.div_sdivisor.write(divisor);

    wait8Cycles();

    return @bitCast(i32, rp2040.sio.div_quotient.read());
}

fn idivmod(dividend: i32, divisor: i32) callconv(.C) u64 {
    rp2040.sio.div_sdividend.write(dividend);
    rp2040.sio.div_sdivisor.write(divisor);

    wait8Cycles();

    const quotient = rp2040.sio.div_quotient.read();
    const remainder = rp2040.sio.div_remainder.read();

    return (@as(u64, remainder) << 32) | quotient;
}

fn uidiv(dividend: u32, divisor: u32) callconv(.C) u32 {
    rp2040.sio.div_udividend.write(dividend);
    rp2040.sio.div_udivisor.write(divisor);

    wait8Cycles();

    return rp2040.sio.div_quotient.read();
}

fn uidivmod(dividend: u32, divisor: u32) callconv(.C) u64 {
    rp2040.sio.div_udividend.write(dividend);
    rp2040.sio.div_udivisor.write(divisor);

    wait8Cycles();

    const quotient = rp2040.sio.div_quotient.read();
    const remainder = rp2040.sio.div_remainder.read();

    return (@as(u64, remainder) << 32) | quotient;
}

inline fn wait8Cycles() void {
    // Trick and inline asm borrowed from the pico-sdk
    asm volatile (
        \\  b _1_%=
        \\_1_%=:
        \\  b _2_%=
        \\_2_%=:
        \\  b _3_%=
        \\_3_%=:
        \\  b _4_%=
        \\_4_%=:
    );
}
