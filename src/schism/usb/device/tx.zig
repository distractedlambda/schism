const std = @import("std");

const buffers = @import("buffers.zig");
const connection = @import("connection.zig");
const derived_config = @import("DerivedConfig.zig").resolved;
const executor = @import("../../executor.zig");
const rp2040 = @import("../../rp2040.zig");

const Continuation = executor.Continuation;
const ContinuationQueue = executor.ContinuationQueue;
const Error = @import("error.zig").Error;

var buffer_waiters = [1]ContinuationQueue{.{}} ** derived_config.num_tx_channels;
var buffer_used = std.StaticBitSet(derived_config.num_tx_channels).initEmpty();
var next_pid = std.StaticBitSet(derived_config.num_tx_channels).initEmpty();

const BufferWaiter = struct {
    continuation: Continuation,
    result: Error!void = undefined,
};

pub const Buffer = struct {
    channel: ChannelIndex,
    pid: u1,
    len: usize = 0,

    pub fn submit(self: @This()) void {
        buffers.submit(bufCtrlIndex(self.channel), self.len, self.pid);
    }

    pub fn write(self: *@This(), data: []const u8) usize {
        const copy_len = @minimum(64 - self.len, data.len);
        @memcpy(buffers.tx[self.channel][self.len..].ptr, data.ptr, copy_len);
        self.len += copy_len;
        return copy_len;
    }

    pub fn isFull(self: @This()) bool {
        return self.len == 64;
    }
};

const ChannelIndex = std.math.IntFittingRange(0, derived_config.num_tx_channels - 1);

inline fn bufCtrlIndex(channel: ChannelIndex) u5 {
    return @as(u5, channel) * 2 + 2;
}

pub fn nextBuffer(connection_id: connection.Id, channel: ChannelIndex) Error!Buffer {
    std.debug.assert(channel < derived_config.num_tx_channels);

    if (connection_id != connection.current) {
        return error.ConnectionLost;
    }

    const pid = @boolToInt(next_pid.isSet(channel));
    next_pid.toggle(channel);

    if (!buffer_used.isSet(channel)) {
        buffer_used.set(channel);
    } else {
        var waiter = BufferWaiter{ .continuation = Continuation.init(@frame()) };

        suspend {
            buffer_waiters[channel].pushBack(&waiter.continuation);
        }

        try waiter.result;
    }

    return Buffer{ .channel = channel, .pid = pid };
}

pub fn handleBufferStatusInterrupt(status: u32) void {
    for (buffer_waiters) |*queue, channel| {
        if (@truncate(u1, status >> bufCtrlIndex(@intCast(ChannelIndex, channel))) != 0) {
            if (queue.popFront()) |continuation| {
                const waiter = @fieldParentPtr(BufferWaiter, "continuation", continuation);
                waiter.result = {};
                executor.submit(&waiter.continuation);
            } else {
                std.debug.assert(buffer_used.isSet(channel));
                buffer_used.unset(channel);
            }
        }
    }
}

pub fn cancelAll(err: Error) void {
    buffer_used = @TypeOf(buffer_used).initEmpty();
    next_pid = @TypeOf(next_pid).initEmpty();
    for (buffer_waiters) |*queue| {
        while (queue.popFront()) |continuation| {
            const waiter = @fieldParentPtr(BufferWaiter, "continuation", continuation);
            waiter.result = err;
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
