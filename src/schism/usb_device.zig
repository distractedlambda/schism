const std = @import("std");

const arm = @import("../arm.zig");
const config = @import("config.zig").resolved;
const executor = @import("executor.zig");
const usb = @import("../common/usb.zig");
const resets = @import("resets.zig");
const rp2040 = @import("../rp2040/rp2040.zig");

const Continuation = executor.Continuation;
const ContinuationQueue = executor.ContinuationQueue;

const derived_config: struct {
    device_descriptor: []const u8,
    configuration_descriptor: []const u8,
    string_descriptors: []const []const u8,
    channel_assignments: []const []const u4,
    num_tx_channels: u4,
    num_rx_channels: u4,
} = blk: {
    const StringDescriptorTable = struct {
        descriptors: []const []const u8,

        fn init(comptime languages: []const usb.LanguageId) @This() {
            comptime {
                var descriptor0_string: [languages.len]u16 = undefined;
                for (languages) |language, i| descriptor0_string[i] = @enumToInt(language);
                const descriptor0 = usb.StringDescriptor(languages.len){ .string = descriptor0_string };
                return .{ .descriptors = &[_][]const u8{&std.mem.toBytes(descriptor0)} };
            }
        }

        fn addUtf8(comptime self: *@This(), comptime utf8_or_null: ?[]const u8) u8 {
            comptime {
                const utf8 = utf8_or_null orelse return 0;
                const utf16 = std.unicode.utf8ToUtf16LeStringLiteral(utf8).*;
                const descriptor = usb.StringDescriptor(utf16.len){ .string = utf16 };
                self.descriptors = self.descriptors ++ &[_][]const u8{&std.mem.toBytes(descriptor)};
                return self.descriptors.len - 1;
            }
        }
    };

    const usb_device_config = config.usb.?.Device;

    var string_descriptor_table = StringDescriptorTable.init(&[_]usb.LanguageId{usb_device_config.language_id});
    var interface_descriptors_blob: []const u8 = &[_]u8{};
    var channel_assignments: []const []const u4 = &[_][]const u4{};
    var num_tx_channels: u4 = 0;
    var num_rx_channels: u4 = 0;

    for (usb_device_config.interfaces) |interface_config, interface_index| {
        var endpoint_channel_assignments: [interface_config.endpoints.len]u4 = undefined;
        var endpoint_descriptors_blob: []const u8 = &[_]u8{};

        for (interface_config.endpoints) |endpoint_config, endpoint_index| {
            endpoint_channel_assignments[endpoint_index] = pick_channel: {
                switch (endpoint_config.direction) {
                    .Out => {
                        defer num_rx_channels += 1;
                        break :pick_channel num_rx_channels;
                    },

                    .In => {
                        defer num_tx_channels += 1;
                        break :pick_channel num_tx_channels;
                    },
                }
            };

            const endpoint_descriptor = std.mem.toBytes(usb.EndpointDescriptor{
                .endpoint_address = .{
                    .endpoint_number = endpoint_channel_assignments[endpoint_index] + 1,
                    .direction = endpoint_config.direction,
                },
                .attributes = .{ .transfer_type = .Bulk },
                .max_packet_size = 64,
                .interval = 0, // FIXME correct value?
            });

            endpoint_descriptors_blob = endpoint_descriptors_blob ++ endpoint_descriptor;
        }

        const interface_descriptor = std.mem.toBytes(usb.InterfaceDescriptor{
            .interface_number = interface_index,
            .alternate_setting = 0,
            .num_endpoints = interface_config.endpoints.len,
            .interface_class = interface_config.class,
            .interface_subclass = interface_config.subclass,
            .interface_protocol = interface_config.protocol,
            .interface_string_index = string_descriptor_table.addUtf8(interface_config.name),
        });

        interface_descriptors_blob = interface_descriptors_blob ++ interface_descriptor ++ endpoint_descriptors_blob;
        channel_assignments = channel_assignments ++ [_][]const u4{&endpoint_channel_assignments};
    }

    const device_descriptor = std.mem.toBytes(usb.DeviceDescriptor{
        .bcd_usb = .@"1.1",
        .device_class = .Device,
        .device_subclass = 0,
        .device_protocol = 0,
        .ep0_max_packet_size = 64,
        .vendor_id = usb_device_config.vendor_id,
        .product_id = usb_device_config.product_id,
        .bcd_device = usb_device_config.bcd_device,
        .manufacturer_string_index = string_descriptor_table.addUtf8(usb_device_config.manufacturer),
        .product_string_index = string_descriptor_table.addUtf8(usb_device_config.product),
        .serial_number_string_index = string_descriptor_table.addUtf8(usb_device_config.serial_number),
        .num_configurations = 1,
    });

    const configuration_descriptor = std.mem.toBytes(usb.ConfigurationDescriptor{
        .total_length = @sizeOf(usb.ConfigurationDescriptor) + interface_descriptors_blob.len,
        .num_interfaces = usb_device_config.interfaces.len,
        .configuration_value = 1,
        .configuration_string_index = 0,
        .attributes = .{ .remote_wakeup = false, .self_powered = true },
        .max_power = 50,
    });

    break :blk .{
        .device_descriptor = &device_descriptor,
        .configuration_descriptor = &configuration_descriptor,
        .string_descriptors = string_descriptor_table.descriptors,
        .channel_assignments = channel_assignments,
        .num_tx_channels = num_tx_channels,
        .num_rx_channels = num_rx_channels,
    };
};

pub const Error = error{ BusReset, ConnectionLost };

pub const ConnectionId = enum(usize) { _ };

var configured = false;
var current_connection = @intToEnum(ConnectionId, 0);
var connection_waiters = ContinuationQueue{};

pub fn connect() ConnectionId {
    while (!configured) {
        var continuation = Continuation.init(@frame());

        suspend {
            connection_waiters.pushBack(&continuation);
        }
    }

    return current_connection;
}

const RxWaiter = struct {
    continuation: Continuation,
    state: union {
        destination: []u8,
        result: Error!usize,
    },
};

const RxChannelIndex = std.math.IntFittingRange(0, derived_config.num_rx_channels - 1);

const channel_buffers_base_address = rp2040.usb.dpram_base_address + 0x180;

const rx_buffers = @intToPtr(*const [derived_config.num_rx_channels][64]u8, channel_buffers_base_address);
var rx_waiters = [1]ContinuationQueue{.{}} ** derived_config.num_rx_channels;
var rx_in_flight = std.StaticBitSet(derived_config.num_rx_channels).initEmpty();
var next_rx_pid = std.StaticBitSet(derived_config.num_rx_channels).initEmpty();

inline fn rxBufCtrlIndex(channel: RxChannelIndex) u5 {
    return @as(u5, channel) * 2 + 3;
}

fn receiveImpl(connection: ConnectionId, channel: RxChannelIndex, destination: []u8) Error!u6 {
    std.debug.assert(channel < derived_config.num_rx_channels);
    std.debug.assert(destination.len <= 64);

    if (connection != current_connection) {
        return error.ConnectionLost;
    }

    if (!rx_in_flight.isSet(channel)) {
        startTransfer(rxBufCtrlIndex(channel), destination.len, next_rx_pid.isSet(channel));
        rx_in_flight.set(channel);
        next_rx_pid.toggle(channel);
    }

    var waiter = RxWaiter{
        .continuation = Continuation.init(@frame()),
        .destination_and_result = .{ .destination = destination },
    };

    suspend {
        rx_waiters[channel].pushBack(&waiter.continuation);
    }

    return waiter.destination_and_result.result;
}

const TxWaiter = struct {
    continuation: Continuation,
    state: union {
        source: []const u8,
        result: Error!void,
    },
};

const TxChannelIndex = std.math.IntFittingRange(0, derived_config.num_tx_channels - 1);

const tx_buffers = @intToPtr(*[derived_config.num_tx_channels][64]u8, channel_buffers_base_address + @as(usize, derived_config.num_rx_channels) * 64);
var tx_waiters = [1]ContinuationQueue{.{}} ** derived_config.num_tx_channels;
var tx_in_flight = std.StaticBitSet(derived_config.num_tx_channels).initEmpty();
var next_tx_pid = std.StaticBitSet(derived_config.num_tx_channels).initEmpty();

inline fn txBufCtrlIndex(channel: TxChannelIndex) u5 {
    return @as(u5, channel) * 2 + 2;
}

fn sendImpl(connection: ConnectionId, channel: TxChannelIndex, source: []const u8) Error!void {
    std.debug.assert(channel < derived_config.num_tx_channels);
    std.debug.assert(source.len <= 64);

    if (connection != current_connection) {
        return error.ConnectionLost;
    }

    if (!tx_in_flight.isSet(channel)) {
        tx_in_flight.set(channel);
        @memcpy(tx_buffers[channel], source, source.len);
        startTransfer(txBufCtrlIndex(channel), source.len, next_tx_pid.isSet(channel));
        next_tx_pid.toggle(channel);
    } else {
        var waiter = TxWaiter{
            .continuation = Continuation.init(@frame()),
            .source_and_result = .{ .source = source },
        };

        suspend {
            tx_waiters[channel].pushBack(&waiter.continuation);
        }

        return waiter.source_and_result.result;
    }
}

const SetupPacketWaiter = struct {
    continuation: Continuation,
    packet: Error!usb.SetupPacket = undefined,
};

const Ep0TransferWaiter = struct {
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

var setup_packet_waiters = ContinuationQueue{};
var next_setup_packet: ?usb.SetupPacket = null;

var ep0_transfer_waiters = ContinuationQueue{};
var ep0_transfer_in_flight = false;

const ep0_buffer = @intToPtr(*[64]u8, rp2040.usb.dpram_base_address + 0x100);

fn receiveSetupPacket(connection: ConnectionId) Error!usb.SetupPacket {
    if (connection != current_connection) {
        return error.ConnectionLost;
    }

    if (next_setup_packet) |setup_packet| {
        next_setup_packet = null;
        return setup_packet;
    }

    var waiter = SetupPacketWaiter{ .continuation = Continuation.init(@frame()) };

    suspend {
        setup_packet_waiters.pushBack(&waiter.continuation);
    }

    return waiter.packet;
}

fn receiveOnEp0(connection: ConnectionId, destination: []u8, pid: u1) Error!usize {
    // FIXME: don't suspend when receiving a zero-length packet?

    std.debug.assert(destination.len <= 64);

    if (connection != current_connection) {
        return error.ConnectionLost;
    }

    if (!ep0_transfer_in_flight) {
        startTransfer(1, destination.len, pid);
        ep0_transfer_in_flight = true;
    }

    var waiter = Ep0TransferWaiter{
        .continuation = Continuation.init(@frame()),
        .state = .{
            .pending = .{
                .pid = pid,
                .buffer = .{ .Rx = destination },
            },
        },
    };

    suspend {
        ep0_transfer_waiters.pushBack(&waiter.continuation);
    }

    return (try waiter.state.result).rx;
}

fn sendOnEp0(connection: ConnectionId, source: []const u8, pid: u1) Error!void {
    std.debug.assert(source.len <= 64);

    if (connection != current_connection) {
        return error.ConnectionLost;
    }

    if (!ep0_transfer_in_flight) {
        @memcpy(@as([]u8, ep0_buffer).ptr, source.ptr, source.len);
        startTransfer(0, source.len, pid);
        ep0_transfer_in_flight = true;
    }

    var waiter = Ep0TransferWaiter{
        .continuation = Continuation.init(@frame()),
        .state = .{
            .pending = .{
                .pid = pid,
                .buffer = .{ .Tx = source },
            },
        },
    };

    suspend {
        ep0_transfer_waiters.pushBack(&waiter.continuation);
    }

    return (try waiter.state.result).tx;
}

pub fn handleIrq() void {
    arm.disableInterrupts();
    defer arm.enableInterrupts();

    const ints = rp2040.usb.ints.read();

    if (ints.setup_req) {
        const setup_packet = @bitCast(usb.SetupPacket, @intToPtr(*const volatile [8]u8, rp2040.usb.dpram_base_address).*);
        rp2040.usb.sie_status.clear(.{ .setup_rec = true });
        if (setup_packet_waiters.popFront()) |continuation| {
            const waiter = @fieldParentPtr(SetupPacketWaiter, "continuation", continuation);
            waiter.packet = setup_packet;
            executor.submit(continuation);
        } else {
            next_setup_packet = setup_packet;
        }
    }

    if (ints.buff_status) {
        const buff_status = rp2040.usb.buff_status.read();
        rp2040.usb.buff_status.write(buff_status);

        if (@truncate(u2, buff_status) != 0) {
            {
                const waiter = @fieldParentPtr(Ep0TransferWaiter, "continuation", ep0_transfer_waiters.popFront().?);
                switch (@truncate(u2, buff_status)) {
                    // TX
                    0b01 => {
                        _ = waiter.state.pending.buffer.Tx;
                        waiter.state = .{ .result = .{ .tx = {} } };
                        executor.submit(&waiter.continuation);
                    },

                    // RX
                    0b10 => {
                        const buf_len = rp2040.usb.device_ep_buf_ctrl.read(1).buf0_len;
                        @memcpy(waiter.state.pending.buffer.Rx.ptr, @as([]const u8, ep0_buffer).ptr, @minimum(buf_len, waiter.state.pending.buffer.Rx.len));
                        waiter.state = .{ .result = .{ .rx = buf_len } };
                        executor.submit(&waiter.continuation);
                    },

                    else => unreachable,
                }
            }

            if (ep0_transfer_waiters.peekFront()) |continuation| {
                const waiter = @fieldParentPtr(Ep0TransferWaiter, "continuation", continuation);
                switch (waiter.state.pending.buffer) {
                    .Tx => |source| {
                        @memcpy(@as([]u8, ep0_buffer).ptr, source.ptr, source.len);
                        startTransfer(0, source.len, waiter.state.pending.pid);
                    },

                    .Rx => |destination| {
                        startTransfer(1, destination.len, waiter.state.pending.pid);
                    },
                }
            } else {
                ep0_transfer_in_flight = false;
            }
        }

        inline for (tx_waiters) |*queue, channel| {
            if (@truncate(u1, buff_status >> txBufCtrlIndex(channel)) != 0) {
                if (queue.popFront()) |continuation| {
                    const waiter = @fieldParentPtr(TxWaiter, "continuation", continuation);
                    @memcpy(@as([]u8, &tx_buffers[channel]).ptr, waiter.state.source.ptr, waiter.state.source.len);
                    startTransfer(txBufCtrlIndex(channel), waiter.state.source.len, @boolToInt(next_tx_pid.isSet(channel)));
                    next_tx_pid.toggle(channel);
                    waiter.state = .{ .result = {} };
                    executor.submit(&waiter.continuation);
                } else {
                    std.debug.assert(tx_in_flight.isSet(channel));
                    tx_in_flight.unset(channel);
                }
            }
        }

        inline for (rx_waiters) |*queue, channel| {
            if (@truncate(u1, buff_status >> rxBufCtrlIndex(channel)) != 0) {
                {
                    const buf_len = rp2040.usb.device_ep_buf_ctrl.read(rxBufCtrlIndex(channel)).buf0_len;
                    const waiter = @fieldParentPtr(RxWaiter, "continuation", queue.popFront().?);
                    @memcpy(waiter.state.destination.ptr, @as([]const u8, &rx_buffers[channel]).ptr, @minimum(buf_len, waiter.state.destination.len));
                    waiter.state = .{ .result = buf_len };
                    executor.submit(&waiter.continuation);
                }

                if (queue.peekFront()) |continuation| {
                    const waiter = @fieldParentPtr(RxWaiter, "continuation", continuation);
                    startTransfer(rxBufCtrlIndex(channel), waiter.state.destination.len, @boolToInt(next_rx_pid.isSet(channel)));
                    next_rx_pid.toggle(channel);
                } else {
                    std.debug.assert(rx_in_flight.isSet(channel));
                    rx_in_flight.unset(channel);
                }
            }
        }
    }

    if (ints.bus_reset) {
        rp2040.usb.sie_status.clear(.{ .bus_reset = true });
        rp2040.usb.addr_endp.write(0, .{ .address = 0 });

        current_connection = @intToEnum(ConnectionId, @enumToInt(current_connection) +% 1);
        configured = false;

        // FIXME: do we need to reset any buffer control stuff?

        next_setup_packet = null;
        while (setup_packet_waiters.popFront()) |continuation| {
            const waiter = @fieldParentPtr(SetupPacketWaiter, "continuation", continuation);
            waiter.packet = error.BusReset;
            executor.submit(continuation);
        }

        ep0_transfer_in_flight = false;
        while (ep0_transfer_waiters.popFront()) |continuation| {
            const waiter = @fieldParentPtr(Ep0TransferWaiter, "continuation", continuation);
            waiter.state = .{ .result = error.BusReset };
            executor.submit(continuation);
        }

        tx_in_flight = @TypeOf(tx_in_flight).initEmpty();
        next_tx_pid = @TypeOf(next_tx_pid).initEmpty();
        for (tx_waiters) |*queue| {
            while (queue.popFront()) |continuation| {
                const waiter = @fieldParentPtr(TxWaiter, "continuation", continuation);
                waiter.state = .{ .result = error.BusReset };
                executor.submit(continuation);
            }
        }

        rx_in_flight = @TypeOf(rx_in_flight).initEmpty();
        next_rx_pid = @TypeOf(next_rx_pid).initEmpty();
        for (rx_waiters) |*queue| {
            while (queue.popFront()) |continuation| {
                const waiter = @fieldParentPtr(RxWaiter, "continuation", continuation);
                waiter.state = .{ .result = error.BusReset };
                executor.submit(continuation);
            }
        }
    }
}

fn startTransfer(bufctrl_idx: u5, len: usize, pid: u1) void {
    std.debug.assert(len <= 64);

    rp2040.usb.device_ep_buf_ctrl.write(bufctrl_idx, .{
        .buf0_full = @truncate(u1, bufctrl_idx) == 0,
        .buf0_data_pid = pid,
        .buf0_len = @intCast(u10, len),
    });

    // FIXME: do we really need this dsb?
    arm.dataSynchronizationBarrier();

    // FIXME: choose the number of noops based on clock frequency bounds
    arm.nop();
    arm.nop();
    arm.nop();

    rp2040.usb.device_ep_buf_ctrl.write(bufctrl_idx, .{
        .buf0_full = @truncate(u1, bufctrl_idx) == 0,
        .buf0_data_pid = pid,
        .buf0_len = @intCast(u10, len),
        .buf0_available = true,
    });
}

fn serveEp0() void {
    executor.yield();

    reconnect_loop: while (true) {
        const connection = current_connection;

        setup_packet_loop: while (true) {
            const setup_packet = receiveSetupPacket(connection) catch
                continue :reconnect_loop;

            if (setup_packet.request_type.recipient != .Device or setup_packet.request_type.type != .Standard)
                continue :setup_packet_loop;

            switch (setup_packet.request_type.direction) {
                .Out => switch (setup_packet.request) {
                    .SetAddress => {
                        sendOnEp0(connection, &[_]u8{}, 1) catch continue :reconnect_loop;
                        rp2040.usb.addr_endp.write(0, .{ .address = @truncate(u7, setup_packet.value) });
                    },

                    .SetConfiguration => {
                        // FIXME: handle re-configuration, un-configuration
                        sendOnEp0(connection, &[_]u8{}, 1) catch continue :reconnect_loop;

                        if (connection != current_connection) {
                            continue :reconnect_loop;
                        }

                        configured = true;

                        executor.submitAll(&connection_waiters);
                    },

                    else => {
                        // FIXME: should we be acknowledging here even
                        // though we don't recognize the request?
                    },
                },

                .In => switch (setup_packet.request) {
                    .GetDescriptor => switch (@intToEnum(usb.DescriptorType, setup_packet.value >> 8)) {
                        .Device => {
                            const len = @minimum(setup_packet.length, derived_config.device_descriptor.len);
                            sendOnEp0(connection, derived_config.device_descriptor[0..len], 1) catch continue :reconnect_loop;
                            _ = receiveOnEp0(connection, &[_]u8{}, 1) catch continue :reconnect_loop;
                        },

                        .Configuration => {
                            const len = @minimum(setup_packet.length, derived_config.configuration_descriptor.len);
                            // FIXME: handle long configuration descriptors
                            sendOnEp0(connection, derived_config.configuration_descriptor[0..len], 1) catch continue :reconnect_loop;
                            _ = receiveOnEp0(connection, &[_]u8{}, 1) catch continue :reconnect_loop;
                        },

                        .String => {
                            const index = @truncate(u8, setup_packet.value);
                            if (index < derived_config.string_descriptors.len) {
                                const descriptor = derived_config.string_descriptors[index];
                                const len = @minimum(descriptor.len, setup_packet.length);
                                // FIXME: handle long string descriptors
                                sendOnEp0(connection, descriptor[0..len], 1) catch continue :reconnect_loop;
                                _ = receiveOnEp0(connection, &[_]u8{}, 1) catch continue :reconnect_loop;
                            }
                        },

                        else => {
                            // FIXME: what do we do here?
                        },
                    },

                    else => {
                        // FIXME: what do we do here?
                    },
                },
            }
        }
    }
}

var ep0_serve_frame: @Frame(serveEp0) = undefined;

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

    comptime var tx_channel: u4 = 0;
    inline while (tx_channel < derived_config.num_tx_channels) : (tx_channel += 1) {
        rp2040.usb.device_ep_ctrl.write(@as(u5, tx_channel) * 2, .{
            .en = true,
            .int_1buf = true,
            .buf_address = comptime @intCast(u10, (@ptrToInt(&tx_buffers[tx_channel]) - rp2040.usb.dpram_base_address) / 64),
            .type = .Bulk,
        });
    }

    comptime var rx_channel: u4 = 0;
    inline while (rx_channel < derived_config.num_rx_channels) : (rx_channel += 1) {
        rp2040.usb.device_ep_ctrl.write(@as(u5, rx_channel) * 2 + 1, .{
            .en = true,
            .int_1buf = true,
            .buf_address = comptime @intCast(u10, (@ptrToInt(&rx_buffers[rx_channel]) - rp2040.usb.dpram_base_address) / 64),
            .type = .Bulk,
        });
    }

    rp2040.usb.sie_ctrl.set(.{ .pullup_en = true });

    rp2040.ppb.nvic_iser.write(.{ .usbctrl = true });

    ep0_serve_frame = async serveEp0();
}

pub inline fn send(connection: ConnectionId, comptime interface: usize, comptime endpoint_of_interface: usize, data: []const u8) Error!void {
    comptime std.debug.assert(config.usb.?.Device.interfaces[interface].endpoints[endpoint_of_interface].direction == .In);
    return sendImpl(connection, comptime derived_config.channel_assignments[interface][endpoint_of_interface], data);
}

pub inline fn receive(connection: ConnectionId, comptime interface: usize, comptime endpoint_of_interface: usize, destination: []u8) Error!void {
    comptime std.debug.assert(config.usb.?.Device.interfaces[interface].endpoints[endpoint_of_interface].direction == .Out);
    return receiveImpl(connection, comptime derived_config.channel_assignments[interface][endpoint_of_interface], destination);
}
