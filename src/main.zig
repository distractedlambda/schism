const std = @import("std");

const bits = @import("bits.zig");
const llvmintrin = @import("llvmintrin.zig");
const picosystem = @import("picosystem.zig");
const rp2040 = @import("rp2040.zig");

comptime {
    _ = rp2040;
}

pub fn main() void {
    const pin = picosystem.pins.battery.charge_led;

    const reset_mask = bits.make(.{
        .{ rp2040.registers.resets.pads_bank0, true },
        .{ rp2040.registers.resets.io_bank0, true },
    });

    rp2040.registers.resets.reset.clear(reset_mask);

    while ((rp2040.registers.resets.reset_done.read() & reset_mask) != reset_mask) {}

    rp2040.registers.io_bank0.gpio_ctrl.write(pin, bits.make(.{
        .{ rp2040.registers.io_bank0.gpio_ctrl.funcsel(pin), .sio },
        .{ rp2040.registers.io_bank0.gpio_ctrl.oeover, .drive_high },
        .{ rp2040.registers.io_bank0.gpio_ctrl.outover, .drive_low },
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
