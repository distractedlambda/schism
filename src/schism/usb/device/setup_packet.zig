const connection = @import("connection.zig");
const executor = @import("../../executor.zig");
const protocol = @import("../protocol.zig");
const rp2040 = @import("../../rp2040.zig");

const Continuation = executor.Continuation;
const ContinuationQueue = executor.ContinuationQueue;
const Error = @import("error.zig").Error;

const Waiter = struct {
    continuation: Continuation,
    packet: Error!protocol.SetupPacket = undefined,
};

var waiters = ContinuationQueue{};
var next_setup_packet: ?protocol.SetupPacket = null;

pub fn receive(connection_id: connection.Id) Error!protocol.SetupPacket {
    if (connection_id != connection.current) {
        return error.ConnectionLost;
    }

    if (next_setup_packet) |setup_packet| {
        next_setup_packet = null;
        return setup_packet;
    }

    var waiter = Waiter{ .continuation = Continuation.init(@frame()) };

    suspend {
        waiters.pushBack(&waiter.continuation);
    }

    return waiter.packet;
}

pub fn handleInterrupt() void {
    const setup_packet = @intToPtr(*const volatile protocol.SetupPacket, rp2040.usb.dpram_base_address).*;
    rp2040.usb.sie_status.clear(.{ .setup_rec = true });
    if (waiters.popFront()) |continuation| {
        const waiter = @fieldParentPtr(Waiter, "continuation", continuation);
        waiter.packet = setup_packet;
        executor.submit(continuation);
    } else {
        next_setup_packet = setup_packet;
    }
}

pub fn cancelAll(err: Error) void {
    next_setup_packet = null;
    while (waiters.popFront()) |continuation| {
        const waiter = @fieldParentPtr(Waiter, "continuation", continuation);
        waiter.packet = err;
        executor.submit(continuation);
    }
}
