const std = @import("std");

const arm = @import("../../arm.zig");
const config = @import("../config.zig");
const executor = @import("../executor.zig");
const resets = @import("../resets.zig");
const rp2040 = @import("../../rp2040/rp2040.zig");
const protocol = @import("protocol.zig");

const Continuation = executor.Continuation;
const ContinuationQueue = executor.ContinuationQueue;

comptime {
    std.debug.assert(std.builtin.target.arch.cpu.endian() == .Little);
}

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

        fn init(comptime languages: []const protocol.LanguageId) @This() {
            return comptime .{ .descriptors = &[_][]const u8{std.mem.toBytes(protocol.stringDescriptor0(languages))} };
        }

        fn addString(comptime self: *@This(), comptime string_or_null: ?[]const u8) u8 {
            comptime {
                const string = string_or_null orelse return 0;
                self.descriptors = self.descriptors ++ &[_][]const u8{&std.mem.toBytes(protocol.StringDescriptor(string_or_null))};
                return self.descriptors.len - 1;
            }
        }
    };

    const usb_device_config = config.usb.?.Device;

    var string_descriptor_table = StringDescriptorTable.init(&usb_device_config.language_id);
    var interface_and_endpoint_descriptors: []const u8 = &[_]u8{};
    var channel_assignments: []const []const u4 = &[_][]const u4{};
    var num_tx_channels: u4 = 0;
    var num_rx_channels: u4 = 0;

    for (usb_device_config.interfaces) |interface_config, interface_index| {
        var endpoint_channel_assignments: [interface_config.endpoints.len]u4 = undefined;
        var combined_endpoint_descriptors: []const u8 = &[_]u8{};

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

            combined_endpoint_descriptors = combined_endpoint_descriptors ++ std.mem.toBytes(
                protocol.EndpointDescriptor{
                    .endpoint_address = .{
                        .endpoint_number = endpoint_channel_assignments[endpoint_index] + 1,
                        .direction = endpoint_config.direction,
                    },
                    .attributes = .{
                        .transfer_type = endpoint_config.transfer_type,
                    },
                    .max_packet_size = 64,
                    .interval = 0, // FIXME correct value?
                },
            );
        }

        interface_and_endpoint_descriptors = interface_and_endpoint_descriptors ++ std.mem.toBytes(
            protocol.InterfaceDescriptor{
                .interface_number = interface_index,
                .alternate_setting = 0,
                .num_endpoints = interface_config.endpoints.len,
                .interface_class = interface_config.class,
                .interface_subclass = interface_config.subclass,
                .interface_protocol = interface_config.protocol,
                .interface_string_index = string_descriptor_table.addString(interface_config.name),
            },
        ) ++ combined_endpoint_descriptors;

        channel_assignments = channel_assignments ++ [_][]const u4{&endpoint_channel_assignments};
    }

    const device_descriptor = std.mem.toBytes(protocol.DeviceDescriptor{
        .bcd_usb = .@"2.0",
        .device_class = .Device,
        .device_subclass = 0,
        .device_protocol = 0,
        .ep0_max_packet_size = 64,
        .vendor_id = usb_device_config.vendor_id,
        .product_id = usb_device_config.product_id,
        .bcd_device = usb_device_config.bcd_device,
        .manufacturer_string_index = string_descriptor_table.addString(usb_device_config.manufacturer),
        .product_string_index = string_descriptor_table.addString(usb_device_config.product),
        .serial_number_string_index = string_descriptor_table.addString(usb_device_config.serial_number),
        .num_configurations = 1,
    });

    const configuration_descriptor = std.mem.toBytes(protocol.ConfigurationDescriptor{
        .total_length = @sizeOf(protocol.ConfigurationDescriptor) + interface_and_endpoint_descriptors.len,
        .num_interfaces = usb_device_config.interfaces.len,
        .configuration_value = 1,
        .configuration_string_index = 0,
        .attributes = .{ .remote_wakeup = false, .self_powered = true },
        .max_power = 50,
    }) ++ interface_and_endpoint_descriptors;

    break :blk .{
        .device_descriptor = device_descriptor,
        .configuration_descriptor = configuration_descriptor,
        .string_descriptors = string_descriptor_table.descriptors,
        .channel_assignments = channel_assignments,
        .num_tx_channels = num_tx_channels,
        .num_rx_channels = num_rx_channels,
    };
};

const device_descriptor = derived_config.device_descriptor;
const configuration_descriptor = derived_config.configuration_descriptor;
const string_descriptors = derived_config.string_descriptors;
const channel_assignments = derived_config.channel_assignments;
const num_tx_channels = derived_config.num_tx_channels;
const num_rx_channels = derived_config.num_rx_channels;

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

const RxWaiter = struct {
    continuation: Continuation,
    destination_and_result: union { destination: []u8, result: Error!u6 },
};

const RxChannelIndex = std.math.IntFittingRange(0, num_rx_channels - 1);

const channel_buffers_base_address = rp2040.usb.dpram_base_address + 0x180;

const rx_buffers = @intToPtr(*const [num_rx_channels][64]u8, channel_buffers_base_address);
var rx_waiters = [1]ContinuationQueue{.{}} ** num_rx_channels;
var rx_in_flight = std.StaticBitSet(num_rx_channels).initEmpty();

inline fn rxBufCtrlIndex(channel: RxChannelIndex) u5 {
    return @as(u5, channel) * 2 + 3;
}

fn receiveImpl(connection: ConnectionId, channel: RxChannelIndex, destination: []u8) Error!u6 {
    std.debug.assert(channel < num_rx_channels);

    arm.disableInterrupts();

    if (connection != current_connection) {
        defer arm.enableInterrupts();
        return error.ConnectionLost;
    }

    if (!rx_in_flight.isSet(channel)) {
        startTransfer(rxBufCtrlIndex(channel), destination.len);
        rx_in_flight.set(channel);
    }

    var waiter = RxWaiter{
        .continuation = Continuation.init(@frame()),
        .destination_and_result = .{ .destination = destination },
    };

    suspend {
        rx_waiters[channel].pushBack(&waiter.continuation);
        arm.enableInterrupts();
    }

    return waiter.destination_and_result.result;
}

const TxWaiter = struct {
    continuation: Continuation,
    source_and_result: union { source: []const u8, result: Error!void },
};

const TxChannelIndex = std.math.IntFittingRange(0, num_tx_channels - 1);

const tx_buffers = @intToPtr(*[num_tx_channels][64]u8, channel_buffers_base_address + num_rx_channels * 64);
var tx_waiters = [1]ContinuationQueue{.{}} ** num_tx_channels;
var tx_in_flight = std.StaticBitSet(num_tx_channels).initEmpty();

inline fn txBufCtrlIndex(channel: TxChannelIndex) u5 {
    return @as(u5, channel) * 2 + 2;
}

fn sendImpl(connection: ConnectionId, channel: TxChannelIndex, source: []const u8) Error!void {
    std.debug.assert(channel < num_tx_channels);

    arm.disableInterrupts();

    if (connection != current_connection) {
        defer arm.enableInterrupts();
        return error.ConnectionLost;
    }

    if (!tx_in_flight.isSet(channel)) {
        tx_in_flight.set(channel);
        arm.enableInterrupts();
        std.mem.copy(u8, &tx_buffers[channel], source);
        arm.disableInterrupts();
        startTransfer(txBufCtrlIndex(channel), source.len);
        arm.enableInterrupts();
    } else {
        var waiter = TxWaiter{
            .continuation = Continuation.init(@frame()),
            .source_and_result = .{ .source = source },
        };

        suspend {
            tx_waiters[channel].pushBack(&waiter.continuation);
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

        for (tx_waiters) |*queue, channel| {
            if (@truncate(u1, buff_status >> txBufCtrlIndex(channel)) != 0) {
                if (queue.popFront()) |continuation| {
                    const waiter = @fieldParentPtr(TxWaiter, continuation, "continuation");
                    std.mem.copy(u8, &tx_buffers[channel], waiter.source_and_result.source);
                    startTransfer(txBufCtrlIndex(channel), waiter.source_and_result.source.len);
                    waiter.source_and_result.result = {};
                    executor.submit(&waiter.continuation);
                } else {
                    std.debug.assert(tx_in_flight.isSet(channel));
                    tx_in_flight.clear(channel);
                }
            }
        }

        for (rx_waiters) |*queue, channel| {
            if (@truncate(u1, buff_status >> rxBufCtrlIndex(channel)) != 0) {
                {
                    const len = rp2040.usb.device_ep_buf_ctrl.read(rxBufCtrlIndex(channel)).buf0_len;
                    const waiter = @fieldParentPtr(RxWaiter, queue.popFront().?, "continuation");
                    std.mem.copy(u8, waiter.destination_and_result.destination, rx_buffers[channel][0..len]);
                    waiter.destination_and_result.result = @intCast(u6, len);
                    executor.submit(&waiter.continuation);
                }

                if (queue.peekFront()) |continuation| {
                    const waiter = @fieldParentPtr(RxWaiter, continuation, "continuation");
                    startTransfer(rxBufCtrlIndex(channel), waiter.destination_and_result.destination.len);
                } else {
                    std.debug.assert(rx_in_flight.isSet(channel));
                    rx_in_flight.clear(channel);
                }
            }
        }
    }

    if (ints.bus_reset) {
        rp2040.usb.sie_status.clear(.{.bus_reset});

        device_address_to_set = null;
        configured = false;
        current_connection = @intToEnum(ConnectionId, @enumToInt(current_connection) +% 1);

        rp2040.usb.addr_endp.write(0, .{ .address = 0 });

        // FIXME: do we need to reset any buffer control stuff?

        tx_in_flight.clearAll();
        for (tx_waiters) |*queue| {
            while (queue.popFront()) |continuation| {
                const waiter = @fieldParentPtr(TxWaiter, continuation, "continuation");
                waiter.source_and_result.result = error.BusReset;
                executor.submit(&waiter.continuation);
            }
        }

        rx_in_flight.clearAll();
        for (rx_waiters) |*queue| {
            while (queue.popFront()) |continuation| {
                const waiter = @fieldParentPtr(RxWaiter, continuation, "continuation");
                waiter.destination_and_result.result = error.BusReset;
            }
        }
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

const ep0_buffer = @intToPtr(*[64]u8, rp2040.usb.dpram_base_address + 0x100);

fn handleSetupPacket(setup_packet: protocol.SetupPacket) void {
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
                .Device => {
                    next_pid.set(0);
                    std.mem.copy(u8, &ep0_buffer, device_descriptor);
                    startTransfer(0, device_descriptor.len);
                },

                .Configuration => {
                    const len = @minimum(setup_packet.length, configuration_descriptor.len);
                    std.mem.copy(u8, &ep0_buffer, configuration_descriptor[0..len]);
                    startTransfer(0, len);
                },

                .String => {
                    const index = @truncate(u8, setup_packet.value);
                    std.mem.copy(u8, &ep0_buffer, string_descriptors[index]);
                    startTransfer(0, string_descriptors[index].len);
                },

                else => {}, // FIXME panic?
            },

            else => {},
        },
    }
}

pub fn init() void {
    resets.unreset(.{.usbctrl});

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

    var tx_channel: u4 = 0;
    while (tx_channel < num_tx_channels) : (tx_channel += 1) {
        rp2040.usb.device_ep_ctrl.write(@as(u5, tx_channel) * 2, .{
            .en = true,
            .int_1buf = true,
            .buf_address = @ptrToInt(&tx_buffers[tx_channel]),
            .type = .Bulk,
        });
    }

    var rx_channel: u4 = 0;
    while (rx_channel < num_rx_channels) : (rx_channel += 1) {
        rp2040.usb.device_ep_ctrl.write(@as(u5, rx_channel) * 2 + 1, .{
            .en = true,
            .int_1buf = true,
            .buf_address = @ptrToInt(&rx_buffers[rx_channel]),
            .type = .Bulk,
        });
    }

    rp2040.usb.sie_ctrl.write(.{
        .pullup_en = true,
    });

    rp2040.ppb.nvic_iser.write(.{
        .usbctrl = true,
    });
}

pub inline fn send(connection: ConnectionId, comptime interface: usize, comptime endpoint_of_interface: usize, data: []const u8) Error!void {
    comptime std.debug.assert(config.usb.?.Device.interfaces[interface].endpoints[endpoint_of_interface].direction == .In);
    return sendImpl(connection, comptime channel_assignments[interface][endpoint_of_interface], data);
}

pub inline fn receive(connection: ConnectionId, comptime interface: usize, comptime endpoint_of_interface: usize, destination: []u8) Error!void {
    comptime std.debug.assert(config.usb.?.Device.interfaces[interface].endpoints[endpoint_of_interface].direction == .Out);
    return receiveImpl(connection, comptime channel_assignments[interface][endpoint_of_interface], destination);
}
