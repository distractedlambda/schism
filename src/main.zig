const std = @import("std");

const bits = @import("bits.zig");
const llvmintrin = @import("llvmintrin.zig");
const picosystem = @import("picosystem.zig");
const rp2040 = @import("rp2040.zig");

comptime {
    _ = rp2040;
}

pub fn main() void {
    rp2040.takeFromReset(.{.pads_bank0, .io_bank0});

    const pin = picosystem.pins.user_led.blue;

    rp2040.registers.io_bank0.gpio_ctrl.write(pin, bits.make(.{
        .{ rp2040.registers.io_bank0.gpio_ctrl.funcsel(pin), .sio },
        .{ rp2040.registers.io_bank0.gpio_ctrl.oeover, .drive_high },
        .{ rp2040.registers.io_bank0.gpio_ctrl.outover, .drive_high },
    }));

    while (true) {
        continue;
    }
}

pub fn panic(message: []const u8, error_return_trace: ?*std.builtin.StackTrace) noreturn {
    _ = message;
    _ = error_return_trace;
    llvmintrin.trap();
}
