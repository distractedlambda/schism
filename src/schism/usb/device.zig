const std = @import("std");

const arm = @import("../arm.zig");
const config = @import("../Config.zig").resolved;
const derived_config = @import("device/DerivedConfig.zig").resolved;
const connection = @import("device/connection.zig");
const control = @import("device/control.zig");
const ep0 = @import("device/ep0.zig");
const executor = @import("../executor.zig");
const protocol = @import("protocol.zig");
const resets = @import("../resets.zig");
const rp2040 = @import("../rp2040.zig");
const rx = @import("device/rx.zig");
const setup_packet = @import("device/setup_packet.zig");
const tx = @import("device/tx.zig");

const Continuation = executor.Continuation;
const ContinuationQueue = executor.ContinuationQueue;

pub const ConnectionId = connection.Id;
pub const Error = @import("device/error.zig").Error;
pub const TransmitBuffer = tx.Buffer;

pub const ControlRequestHandler = struct {
    request: protocol.SetupPacket.Request,
    func: fn (protocol.SetupPacket, ConnectionId) anyerror!void,
};

pub fn handleIrq() void {
    arm.disableInterrupts();
    defer arm.enableInterrupts();

    const ints = rp2040.usb.ints.read();

    if (ints.setup_req) {
        setup_packet.handleInterrupt();
    }

    if (ints.buff_status) {
        const buff_status = rp2040.usb.buff_status.read();
        rp2040.usb.buff_status.write(buff_status);

        if (@truncate(u2, buff_status) != 0) {
            ep0.handleBufferStatusInterrupt(@truncate(u2, buff_status));
        }

        rx.handleBufferStatusInterrupt(buff_status);
        tx.handleBufferStatusInterrupt(buff_status);
    }

    if (ints.bus_reset) {
        rp2040.usb.sie_status.clear(.{ .bus_reset = true });
        rp2040.usb.addr_endp.write(0, .{ .address = 0 });

        connection.invalidate();
        setup_packet.cancelAll(error.BusReset);
        ep0.cancelAll(error.BusReset);
        rx.cancelAll(error.BusReset);
        tx.cancelAll(error.BusReset);

        // FIXME: do we need to reset any buffer control stuff?
    }
}

pub fn init() void {
    // Start up USB PLL
    resets.unreset(.{ .pll_usb = true });
    rp2040.pll_usb.fbdiv_int.write(120);
    rp2040.pll_usb.pwr.clear(.{ .pd = true, .vcopd = true });
    while (!rp2040.pll_usb.cs.read().lock) {}
    rp2040.pll_usb.prim.write(.{ .postdiv1 = 6, .postdiv2 = 5 });
    rp2040.pll_usb.pwr.clear(.{ .postdivpd = true });

    // Enable USB clock
    rp2040.clocks.clk_usb_ctrl.write(.{ .enable = true });

    resets.unreset(.{ .usbctrl = true });

    rp2040.usb.usb_muxing.write(.{
        .softcon = true,
        .to_phy = true,
    });

    rp2040.usb.usb_pwr.write(.{
        .vbus_detect = true,
        .vbus_detect_override_en = true,
    });

    rp2040.usb.main_ctrl.write(.{
        .controller_en = true,
    });

    rp2040.usb.sie_ctrl.write(.{
        .ep0_int_1buf = true,
    });

    rp2040.usb.inte.write(.{
        .buff_status = true,
        .bus_reset = true,
        .setup_req = true,
    });

    rx.init();
    tx.init();

    rp2040.usb.sie_ctrl.set(.{ .pullup_en = true });

    rp2040.ppb.nvic_iser.write(.{ .usbctrl = true });

    control.init();
}

pub const connect = connection.connect;

pub const tryConnect = connection.tryConnect;

pub fn nextTransmitBuffer(connection_id: ConnectionId, comptime interface: usize, comptime endpoint_of_interface: usize) Error!TransmitBuffer {
    comptime std.debug.assert(config.usb.?.Device.interfaces[interface].endpoints[endpoint_of_interface].direction == .In);
    return tx.nextBuffer(connection_id, comptime derived_config.channel_assignments[interface][endpoint_of_interface]);
}

pub fn receive(connection_id: ConnectionId, comptime interface: usize, comptime endpoint_of_interface: usize, destination: []u8) Error!void {
    comptime std.debug.assert(config.usb.?.Device.interfaces[interface].endpoints[endpoint_of_interface].direction == .Out);
    return rx.receive(connection_id, comptime derived_config.channel_assignments[interface][endpoint_of_interface], destination);
}
