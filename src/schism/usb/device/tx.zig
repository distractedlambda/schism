const std = @import("std");

const buffers = @import("buffers.zig");
const connection = @import("connection.zig");
const derived_config = @import("DerivedConfig.zig").resolved;
const executor = @import("../../executor.zig");
const rp2040 = @import("../../rp2040.zig");

const Continuation = executor.Continuation;
const ContinuationQueue = executor.ContinuationQueue;
const Error = @import("error.zig").Error;

var waiters = [1]ContinuationQueue{.{}} ** derived_config.num_tx_channels;
var in_flight = std.StaticBitSet(derived_config.num_tx_channels).initEmpty();
var next_pid = std.StaticBitSet(derived_config.num_tx_channels).initEmpty();

const Waiter = struct {
    continuation: Continuation,
    state: union {
        source: []const u8,
        result: Error!void,
    },
};

const ChannelIndex = std.math.IntFittingRange(0, derived_config.num_tx_channels - 1);

inline fn bufCtrlIndex(channel: ChannelIndex) u5 {
    return @as(u5, channel) * 2 + 2;
}

pub fn send(connection_id: connection.Id, channel: ChannelIndex, source: []const u8) Error!void {
    std.debug.assert(channel < derived_config.num_tx_channels);
    std.debug.assert(source.len <= 64);

    if (connection_id != connection.current) {
        return error.ConnectionLost;
    }

    if (!in_flight.isSet(channel)) {
        in_flight.set(channel);
        @memcpy(buffers.tx[channel], source, source.len);
        buffers.submit(bufCtrlIndex(channel), source.len, next_pid.isSet(channel));
        next_pid.toggle(channel);
    } else {
        var waiter = Waiter{
            .continuation = Continuation.init(@frame()),
            .source_and_result = .{ .source = source },
        };

        suspend {
            waiters[channel].pushBack(&waiter.continuation);
        }

        return waiter.source_and_result.result;
    }
}

pub fn handleBufferStatusInterrupt(status: u32) void {
    for (waiters) |*queue, channel| {
        if (@truncate(u1, status >> bufCtrlIndex(@intCast(ChannelIndex, channel))) != 0) {
            if (queue.popFront()) |continuation| {
                const waiter = @fieldParentPtr(Waiter, "continuation", continuation);
                @memcpy(@as([]u8, &buffers.tx[channel]).ptr, waiter.state.source.ptr, waiter.state.source.len);
                buffers.submit(bufCtrlIndex(@intCast(ChannelIndex, channel)), waiter.state.source.len, @boolToInt(next_pid.isSet(channel)));
                next_pid.toggle(channel);
                waiter.state = .{ .result = {} };
                executor.submit(&waiter.continuation);
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
    while (channel < derived_config.num_tx_channels) : (channel += 1) {
        rp2040.usb.device_ep_ctrl.write(@intCast(u5, channel) * 2, .{
            .en = true,
            .int_1buf = true,
            .buf_address = @intCast(u10, (@ptrToInt(&buffers.tx[channel]) - rp2040.usb.dpram_base_address) / 64),
            .type = .Bulk,
        });
    }
}
