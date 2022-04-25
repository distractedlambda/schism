const std = @import("std");

const bits = @import("bits.zig");
const picosystem = @import("picosystem.zig");
const rp2040 = @import("rp2040.zig");

extern fn @"llvm.trap"() noreturn;

export fn main() void {
    const pin = picosystem.pins.battery.charge_led;

    // rp2040.registers.pads_bank0.gpio.write(pin, bits.make(.{
    //     .{ rp2040.registers.pads_bank0.gpio.od, false },
    //     .{ rp2040.registers.pads_bank0.gpio.ie, true },
    //     .{ rp2040.registers.pads_bank0.gpio.drive, .@"4mA" },
    //     .{ rp2040.registers.pads_bank0.gpio.pue, false },
    //     .{ rp2040.registers.pads_bank0.gpio.pde, true },
    //     .{ rp2040.registers.pads_bank0.gpio.schmitt, true },
    //     .{ rp2040.registers.pads_bank0.gpio.slewfast, false },
    // }));

    const reset_mask = bits.make(.{
        .{ rp2040.registers.resets.reset.pads_bank0, true },
        .{ rp2040.registers.resets.reset.io_bank0, true },
    });

    rp2040.registers.resets.reset.clear(reset_mask);

    while ((rp2040.registers.resets.reset_done.read() & reset_mask) != reset_mask) {}

    rp2040.registers.io_bank0.gpio_ctrl.write(pin, bits.make(.{
        .{ rp2040.registers.io_bank0.gpio_ctrl.funcsel(pin), .sio },
        .{ rp2040.registers.io_bank0.gpio_ctrl.oeover, .drive_high },
        .{ rp2040.registers.io_bank0.gpio_ctrl.outover, .drive_low },
    }));

    // rp2040.registers.sio.gpio_oe_set.write(1 << pin);
    // rp2040.registers.sio.gpio_out_set.write(1 << pin);

    // rp2040.bootrom._reset_to_usb_boot(0, 0);

    while (true) {
        continue;
    }
}

pub fn panic(message: []const u8, error_return_trace: ?*std.builtin.StackTrace) noreturn {
    _ = message;
    _ = error_return_trace;
    @"llvm.trap"();
}
