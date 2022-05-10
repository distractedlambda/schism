const std = @import("std");

const arm = @import("../../arm.zig");
const config = @import("../config.zig");
const executor = @import("../executor.zig");
const resets = @import("../resets.zig");
const rp2040 = @import("../rp2040/rp2040.zig");
const protocol = @import("protocol.zig");

const Continuation = executor.Continuation;
const ContinuationQueue = executor.ContinuationQueue;
const ValuedContinuation = executor.ValuedContinuation;

comptime {
    std.debug.assert(std.builtin.target.arch.cpu.endian() == .Little);
}

pub const Error = error{ BusReset, ConnectionLost };

pub const ConnectionId = enum(usize) { _ };

var configured = false;
var current_connection = @intToEnum(ConnectionId, 0);
var connection_waiters = ContinuationQueue{};

pub fn connect() ConnectionId {
    arm.disableInterrupts();

    while (!configured) {
        var continuation = Continuation.init(@frame());

        suspend {
            connection_waiters.pushBack(&continuation);
            arm.enableInterrupts();
        }

        arm.disableInterrupts();
    }

    defer arm.enableInterrupts();
    return current_connection;
}

const OutTransferWaiter = struct {
    continuation: Continuation,
    destination_and_result: union { destination: []u8, result: Error!u6 },
};

const out_transfer_buffers = @intToPtr(*const [15][64]u8, rp2040.usb.dpram_base_address + 0x540);
var out_transfer_waiters = [1]ContinuationQueue{.{}} ** 15;
var out_transfers_in_flight = std.StaticBitSet(15).initEmpty();

fn receive(connection: ConnectionId, channel: u4, destination: []u8) Error!u6 {
    arm.disableInterrupts();

    if (connection != current_connection) {
        defer arm.enableInterrupts();
        return error.ConnectionLost;
    }

    if (!out_transfers_in_flight.isSet(channel)) {
        startTransfer(@as(u5, channel) * 2 + 3, destination.len);
        out_transfers_in_flight.set(channel);
    }

    var waiter = OutTransferWaiter{
        .continuation = Continuation.init(@frame()),
        .destination_and_result = .{ .destination = destination },
    };

    suspend {
        out_transfer_waiters[channel].pushBack(&waiter.continuation);
        arm.enableInterrupts();
    }

    return waiter.destination_and_result.result;
}

const InTransferWaiter = struct {
    continuation: Continuation,
    source_and_result: union { source: []const u8, result: Error!void },
};

const in_transfer_buffers = @intToPtr(*[15][64]u8, rp2040.usb.dpram_base_address + 0x180);
var in_transfer_waiters = [1]ContinuationQueue{.{}} ** 15;
var in_transfers_in_flight = std.StaticBitSet(15).initEmpty();

fn send(connection: ConnectionId, channel: u4, source: []const u8) Error!void {
    arm.disableInterrupts();

    if (connection != current_connection) {
        defer arm.enableInterrupts();
        return error.ConnectionLost;
    }

    if (!in_transfers_in_flight.isSet(channel)) {
        in_transfers_in_flight.set(channel);
        arm.enableInterrupts();
        for (source) |b, i| in_transfer_buffers[channel][i] = b;
        arm.disableInterrupts();
        startTransfer(@as(u5, channel) * 2 + 2, source.len);
        arm.enableInterrupts();
    } else {
        var waiter = InTransferWaiter{
            .continuation = Continuation.init(@frame()),
            .source_and_result = .{ .source = source },
        };

        suspend {
            in_transfer_waiters[channel].pushBack(&waiter.continuation);
            arm.enableInterrupts();
        }

        return waiter.source_and_result.result;
    }
}

var device_address_to_set: ?u7 = null;

pub fn handleIrq() void {
    const ints = rp2040.usb.ints.read();

    if (ints.setup_req) {
        const setup_packet = @bitCast(SetupPacket, @intToPtr(*const volatile [8]u8, rp2040.usb.dpram_base_address).*);
        rp2040.usb.sie_status.clear(.{.setup_rec});
        handleSetupPacket(setup_packet);
    }

    if (ints.buff_status) {
        const buff_status = rp2040.usb.buff_status.read();
        rp2040.usb.buff_status.write(buff_status);

        if (@truncate(u1, buff_status) != 0) {
            if (device_address_to_set) |address| {
                rp2040.usb.addr_endp.write(0, .{ .address = address });
                device_address_to_set = null;
            } else {
                startTransfer(1, 0);
            }
        }

        inline for (in_transfer_waiters) |queue, channel| {
            const bufctrl_index = channel * 2 + 2; // FIXME: rename bufctrl, commonize this
            if (@truncate(u1, buff_status >> bufctrl_index) != 0) {
                if (queue.popFront()) |continuation| {
                    var waiter = @fieldParentPtr(InTransferWaiter, continuation, "continuation");
                    for (waiter.source_and_result.source) |b, i| in_transfer_buffers[channel][i] = b;
                    startTransfer(bufctrl_idx, waiter.source_and_result.source.len);
                    waiter.source_and_result.result = {};
                    executor.submit(&waiter.continuation);
                } else {
                    std.debug.assert(in_transfers_in_flight.isSet(channel));
                    in_transfers_in_flight.clear(channel);
                }
            }
        }

        // FIXME: do out transfers too
    }

    if (ints.bus_reset) {
        rp2040.usb.sie_status.clear(.{.bus_reset});
        device_address_to_set = null;
        configured = false;
        rp2040.usb.addr_endp.write(0, .{ .address = 0 });
        current_connection = @intToEnum(ConnectionId, @enumToInt(current_connection) +% 1);
        // FIXME: invalidate waiters
    }
}

var next_pid = std.StaticBitSet(32).initEmpty();

fn startTransfer(bufctrl_idx: u5, len: u10) void {
    std.debug.assert(len <= 64);

    rp2040.usb.device_ep_buf_ctrl.write(bufctrl_idx, .{
        .buf0_full = @truncate(u1, bufctrl_idx) == 0,
        .buf0_data_pid = @boolToInt(next_pid.isSet(bufctrl_idx)),
        .buf0_len = len,
    });

    // FIXME: do we really need this dsb?
    arm.dataSynchronizationBarrier();

    // FIXME: choose the number of noops based on clock frequency bounds
    arm.nop();
    arm.nop();
    arm.nop();
    arm.nop();
    arm.nop();
    arm.nop();
    arm.nop();
    arm.nop();

    rp2040.usb.device_ep_buf_ctrl.write(bufctrl_idx, .{
        .buf0_full = @truncate(u1, bufctrl_idx) == 0,
        .buf0_data_pid = @boolToInt(next_pid.isSet(bufctrl_idx)),
        .buf0_len = len,
        .buf0_available = true,
    });

    next_pid.toggle(bufctrl_idx);
}

fn handleSetupPacket(setup_packet: SetupPacket) void {
    next_pid.set(0);

    if (setup_packet.request_type.recipient != .Device or setup_packet.request_type.type != .Standard) {
        return;
    }

    switch (setup_packet.request_type.direction) {
        .Out => {
            switch (setup_packet.request) {
                .SetAddress => {
                    device_address_to_set = @truncate(u7, setup_packet.value);
                },

                .SetConfiguation => {
                    configured = true;
                    executor.submitAll(&connection_waiters);
                },

                else => {},
            }

            startTransfer(0, 0);
        },

        .In => switch (setup_packet.request) {
            .GetDescriptor => switch (@intToEnum(DescriptorType, setup_packet.value >> 8)) {
                .Device => {},
                .Configuration => {},
                .String => {},
                else => {}, // FIXME panic?
            },

            else => {},
        },
    }
}

fn initDevice() void {
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

    comptime var next_endpoint_in = 1;
    comptime var next_endpoint_out = 1;
    inline for (config.usb.?.Device.interfaces) |interface, interface_num| {
        inline for (interface.endpoints) |endpoint| {
            const reg_index = blk: {
                switch (endpoint.direction) {
                    .In => {
                        if (next_endpoint_in == 16) {
                            @compileError("exceeded maximum number of IN endpoints");
                        }

                        defer next_endpoint_in += 1;
                        break :blk (next_endpoint_in - 1) * 2;
                    },

                    .Out => {
                        if (next_endpoint_out == 16) {
                            @compileError("exceeded maximum number of OUT endpoints");
                        }

                        defer next_endpoint_out += 1;
                        break :blk (next_endpoint_out - 1) * 2 + 1;
                    },
                }
            };

            rp2040.usb.device_ep_ctrl.write(reg_index, .{
                .en = true,
                .int_1buf = true,
                .buf_address = reg_index + 6,
                .type = switch (endpoint.transfer_type) {
                    .Control => .Control,
                    .Bulk => .Bulk,
                },
            });
        }
    }

    rp2040.usb.sie_ctrl.write(.{
        .pullup_en = true,
    });

    rp2040.ppb.nvic_iser.write(.{
        .usbctrl = true,
    });
}
