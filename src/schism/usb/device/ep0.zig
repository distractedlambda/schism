const std = @import("std");

const buffers = @import("buffers.zig");
const connection = @import("connection.zig");
const executor = @import("../../executor.zig");
const rp2040 = @import("../../rp2040.zig");

const Continuation = executor.Continuation;
const ContinuationQueue = executor.ContinuationQueue;
const Error = @import("error.zig").Error;

var transfer_waiters = ContinuationQueue{};
var transfer_in_flight = false;

const TransferWaiter = struct {
    continuation: Continuation,
    state: union {
        pending: struct {
            pid: u1,
            buffer: union(enum) {
                Tx: []const u8,
                Rx: []u8,
            },
        },
        result: Error!union {
            tx: void,
            rx: usize,
        },
    },
};

pub fn send(connection_id: connection.Id, source: []const u8, pid: u1) Error!void {
    std.debug.assert(source.len <= 64);

    if (connection_id != connection.current) {
        return error.ConnectionLost;
    }

    if (!transfer_in_flight) {
        @memcpy(@as([]u8, buffers.ep0).ptr, source.ptr, source.len);
        buffers.submit(0, source.len, pid);
        transfer_in_flight = true;
    }

    var waiter = TransferWaiter{
        .continuation = Continuation.init(@frame()),
        .state = .{
            .pending = .{
                .pid = pid,
                .buffer = .{ .Tx = source },
            },
        },
    };

    suspend {
        transfer_waiters.pushBack(&waiter.continuation);
    }

    return (try waiter.state.result).tx;
}

pub fn receive(connection_id: connection.Id, destination: []u8, pid: u1) Error!usize {
    // FIXME: don't suspend when receiving a zero-length packet?

    std.debug.assert(destination.len <= 64);

    if (connection_id != connection.current) {
        return error.ConnectionLost;
    }

    if (!transfer_in_flight) {
        buffers.submit(1, destination.len, pid);
        transfer_in_flight = true;
    }

    var waiter = TransferWaiter{
        .continuation = Continuation.init(@frame()),
        .state = .{
            .pending = .{
                .pid = pid,
                .buffer = .{ .Rx = destination },
            },
        },
    };

    suspend {
        transfer_waiters.pushBack(&waiter.continuation);
    }

    return (try waiter.state.result).rx;
}

pub fn handleBufferStatusInterrupt(bits: u2) void {
    {
        const waiter = @fieldParentPtr(TransferWaiter, "continuation", transfer_waiters.popFront().?);
        switch (bits) {
            // TX
            0b01 => {
                _ = waiter.state.pending.buffer.Tx;
                waiter.state = .{ .result = .{ .tx = {} } };
                executor.submit(&waiter.continuation);
            },

            // RX
            0b10 => {
                const buf_len = rp2040.usb.device_ep_buf_ctrl.read(1).buf0_len;
                @memcpy(waiter.state.pending.buffer.Rx.ptr, @as([]const u8, buffers.ep0).ptr, @minimum(buf_len, waiter.state.pending.buffer.Rx.len));
                waiter.state = .{ .result = .{ .rx = buf_len } };
                executor.submit(&waiter.continuation);
            },

            else => unreachable,
        }
    }

    if (transfer_waiters.peekFront()) |continuation| {
        const waiter = @fieldParentPtr(TransferWaiter, "continuation", continuation);
        switch (waiter.state.pending.buffer) {
            .Tx => |source| {
                @memcpy(@as([]u8, buffers.ep0).ptr, source.ptr, source.len);
                buffers.submit(0, source.len, waiter.state.pending.pid);
            },

            .Rx => |destination| {
                buffers.submit(1, destination.len, waiter.state.pending.pid);
            },
        }
    } else {
        transfer_in_flight = false;
    }
}

pub fn cancelAll(err: Error) void {
    transfer_in_flight = false;
    while (transfer_waiters.popFront()) |continuation| {
        const waiter = @fieldParentPtr(TransferWaiter, "continuation", continuation);
        waiter.state = .{ .result = err };
        executor.submit(continuation);
    }
}
