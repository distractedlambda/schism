const connection = @import("connection.zig");
const derived_config = @import("DerivedConfig.zig").resolved;
const ep0 = @import("ep0.zig");
const executor = @import("../../executor.zig");
const protocol = @import("../protocol.zig");
const rp2040 = @import("../../rp2040.zig");
const setup_packet = @import("setup_packet.zig");

fn run() void {
    executor.yield();

    reconnect_loop: while (true) {
        const connection_id = connection.current;

        setup_packet_loop: while (true) {
            const sp = setup_packet.receive(connection_id) catch
                continue :reconnect_loop;

            if (sp.request_type.recipient != .Device or sp.request_type.type != .Standard)
                continue :setup_packet_loop;

            switch (sp.request_type.direction) {
                .Out => switch (sp.request) {
                    .SetAddress => {
                        ep0.send(connection_id, &[_]u8{}, 1) catch continue :reconnect_loop;
                        rp2040.usb.addr_endp.write(0, .{ .address = @truncate(u7, sp.value) });
                    },

                    .SetConfiguration => {
                        // FIXME: handle re-configuration, un-configuration
                        ep0.send(connection_id, &[_]u8{}, 1) catch continue :reconnect_loop;

                        if (connection_id != connection.current) {
                            continue :reconnect_loop;
                        }

                        connection.configured = true;

                        executor.submitAll(&connection.waiters);
                    },

                    else => {
                        // FIXME: should we be acknowledging here even
                        // though we don't recognize the request?
                    },
                },

                .In => switch (sp.request) {
                    .GetDescriptor => switch (@intToEnum(protocol.DescriptorType, sp.value >> 8)) {
                        .Device => {
                            const len = @minimum(sp.length, derived_config.device_descriptor.len);
                            ep0.send(connection_id, derived_config.device_descriptor[0..len], 1) catch continue :reconnect_loop;
                            _ = ep0.receive(connection_id, &[_]u8{}, 1) catch continue :reconnect_loop;
                        },

                        .Configuration => {
                            const len = @minimum(sp.length, derived_config.configuration_descriptor.len);
                            // FIXME: handle long configuration descriptors
                            ep0.send(connection_id, derived_config.configuration_descriptor[0..len], 1) catch continue :reconnect_loop;
                            _ = ep0.receive(connection_id, &[_]u8{}, 1) catch continue :reconnect_loop;
                        },

                        .String => {
                            const index = @truncate(u8, sp.value);
                            if (index < derived_config.string_descriptors.len) {
                                const descriptor = derived_config.string_descriptors[index];
                                const len = @minimum(descriptor.len, sp.length);
                                // FIXME: handle long string descriptors
                                ep0.send(connection_id, descriptor[0..len], 1) catch continue :reconnect_loop;
                                _ = ep0.receive(connection_id, &[_]u8{}, 1) catch continue :reconnect_loop;
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

var run_frame: @Frame(run) = undefined;

pub fn init() void {
    run_frame = async run();
}
