const std = @import("std");

const buffers = @import("buffers.zig");
const connection = @import("connection.zig");
const derived_config = @import("DerivedConfig.zig").resolved;
const executor = @import("../../executor.zig");
const rp2040 = @import("../../rp2040.zig");

const Continuation = executor.Continuation;
const ContinuationQueue = executor.ContinuationQueue;
const Error = @import("error.zig").Error;

var waiters = [1]ContinuationQueue{.{}} ** derived_config.num_rx_channels;
var in_flight = std.StaticBitSet(derived_config.num_rx_channels).initEmpty();
var next_pid = std.StaticBitSet(derived_config.num_rx_channels).initEmpty();

const Waiter = struct {
    continuation: Continuation,
    state: union {
        destination: []u8,
        result: Error!usize,
    },
};

const ChannelIndex = std.math.IntFittingRange(0, derived_config.num_rx_channels - 1);

inline fn bufCtrlIndex(channel: ChannelIndex) u5 {
    return @as(u5, channel) * 2 + 3;
}

pub fn receive(connection_id: connection.Id, channel: ChannelIndex, destination: []u8) Error!u6 {
    std.debug.assert(channel < derived_config.num_rx_channels);
    std.debug.assert(destination.len <= 64);

    if (connection_id != connection.current) {
        return error.ConnectionLost;
    }

    if (!in_flight.isSet(channel)) {
        buffers.submit(bufCtrlIndex(channel), destination.len, next_pid.isSet(channel));
        in_flight.set(channel);
        next_pid.toggle(channel);
    }

    var waiter = Waiter{
        .continuation = Continuation.init(@frame()),
        .destination_and_result = .{ .destination = destination },
    };

    suspend {
        waiters[channel].pushBack(&waiter.continuation);
    }

    return waiter.destination_and_result.result;
}

pub fn handleBufferStatusInterrupt(status: u32) void {
    for (waiters) |*queue, channel| {
        if (@truncate(u1, status >> bufCtrlIndex(@intCast(ChannelIndex, channel))) != 0) {
            {
                const buf_len = rp2040.usb.device_ep_buf_ctrl.read(bufCtrlIndex(@intCast(ChannelIndex, channel))).buf0_len;
                const waiter = @fieldParentPtr(Waiter, "continuation", queue.popFront().?);
                @memcpy(waiter.state.destination.ptr, @as([]const u8, &buffers.rx[channel]).ptr, @minimum(buf_len, waiter.state.destination.len));
                waiter.state = .{ .result = buf_len };
                executor.submit(&waiter.continuation);
            }

            if (queue.peekFront()) |continuation| {
                const waiter = @fieldParentPtr(Waiter, "continuation", continuation);
                buffers.submit(bufCtrlIndex(@intCast(ChannelIndex, channel)), waiter.state.destination.len, @boolToInt(next_pid.isSet(channel)));
                next_pid.toggle(channel);
            } else {
                std.debug.assert(in_flight.isSet(channel));
                in_flight.unset(channel);
            }
        }
    }
}

pub fn cancelAll(err: Error) void {
    in_flight = @TypeOf(in_flight).initEmpty();
    next_pid = @TypeOf(next_pid).initEmpty();
    for (waiters) |*queue| {
        while (queue.popFront()) |continuation| {
            const waiter = @fieldParentPtr(Waiter, "continuation", continuation);
            waiter.state = .{ .result = err };
            executor.submit(continuation);
        }
    }
}

pub fn init() void {
    var channel: u4 = 0;
    while (channel < derived_config.num_rx_channels) : (channel += 1) {
        rp2040.usb.device_ep_ctrl.write(@intCast(u5, channel) * 2 + 1, .{
            .en = true,
            .int_1buf = true,
            .buf_address = @intCast(u10, (@ptrToInt(&buffers.rx[channel]) - rp2040.usb.dpram_base_address) / 64),
            .type = .Bulk,
        });
    }
}
