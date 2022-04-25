const std = @import("std");

const bits = @import("bits.zig");
const picosystem = @import("picosystem.zig");
const rp2040 = @import("rp2040.zig");

extern fn @"llvm.trap"() noreturn;

export fn main() void {
    const pin = picosystem.pins.user_led.blue;

    rp2040.registers.io_bank0.gpio_ctrl.write(
        pin,
        bits.make(.{
            .{ rp2040.registers.io_bank0.gpio_ctrl.funcsel(pin), .sio },
        }),
    );

    rp2040.registers.sio.gpio_oe_set.write(1 << pin);
    rp2040.registers.sio.gpio_out_set.write(1 << pin);
}

pub fn panic(message: []const u8, error_return_trace: ?*std.builtin.StackTrace) noreturn {
    _ = message;
    _ = error_return_trace;
    @"llvm.trap"();
}
