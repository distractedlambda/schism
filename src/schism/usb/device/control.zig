const connection = @import("connection.zig");
const derived_config = @import("DerivedConfig.zig").resolved;
const device = @import("../device.zig");
const ep0 = @import("ep0.zig");
const executor = @import("../../executor.zig");
const protocol = @import("../protocol.zig");
const rp2040 = @import("../../rp2040.zig");
const setup_packet = @import("setup_packet.zig");

fn run() void {
    executor.yield();

    reconnect_loop: while (true) {
        const connection_id = connection.current;

        while (true) {
            const sp = setup_packet.receive(connection_id) catch continue :reconnect_loop;

            inline for (derived_config.control_request_handlers) |handler| {
                if (sp.request.get() == handler.request) {
                    (handler.func)(sp, connection_id) catch continue :reconnect_loop;
                    break;
                }
            }
        }
    }
}

fn handleSetAddress(sp: protocol.SetupPacket, connection_id: connection.Id) anyerror!void {
    try ep0.send(connection_id, &.{}, 1);
    rp2040.usb.addr_endp.write(0, .{ .address = @truncate(u7, sp.value.get()) });
}

fn handleSetConfiguration(_: protocol.SetupPacket, connection_id: connection.Id) anyerror!void {
    // FIXME: handle re-configuration, un-configuration
    try ep0.send(connection_id, &.{}, 1);

    if (connection_id != connection.current) {
        return error.ConnectionLost;
    }

    connection.configured = true;

    executor.submitAll(&connection.waiters);
}

fn handleGetDescriptor(sp: protocol.SetupPacket, connection_id: connection.Id) anyerror!void {
    switch (@intToEnum(protocol.DescriptorType, sp.value.get() >> 8)) {
        .Device => {
            const len = @minimum(sp.length.get(), derived_config.device_descriptor.len);
            try ep0.send(connection_id, derived_config.device_descriptor[0..len], 1);
            _ = try ep0.receive(connection_id, &.{}, 1);
        },

        .Configuration => {
            const len = @minimum(sp.length.get(), derived_config.configuration_descriptor.len);
            // FIXME: handle long configuration descriptors
            try ep0.send(connection_id, derived_config.configuration_descriptor[0..len], 1);
            _ = try ep0.receive(connection_id, &.{}, 1);
        },

        .String => {
            const index = @truncate(u8, sp.value.get());
            if (index < derived_config.string_descriptors.len) {
                const descriptor = derived_config.string_descriptors[index];
                const len = @minimum(descriptor.len, sp.length.get());
                // FIXME: handle long string descriptors
                try ep0.send(connection_id, descriptor[0..len], 1);
                _ = try ep0.receive(connection_id, &.{}, 1);
            }
        },

        else => {
            // FIXME: what do we do here?
        },
    }
}

var run_frame: @Frame(run) = undefined;

pub const standard_request_handlers: []const device.ControlRequestHandler = &.{
    .{
        .request = protocol.SetupPacket.Request.init(.{
            .recipient = .Device,
            .type = .Standard,
            .direction = .Out,
            .code = 0x05,
        }),
        .func = handleSetAddress,
    },
    .{
        .request = protocol.SetupPacket.Request.init(.{
            .recipient = .Device,
            .type = .Standard,
            .direction = .Out,
            .code = 0x09,
        }),
        .func = handleSetConfiguration,
    },
    .{
        .request = protocol.SetupPacket.Request.init(.{
            .recipient = .Device,
            .type = .Standard,
            .direction = .In,
            .code = 0x06,
        }),
        .func = handleGetDescriptor,
    },
};

pub fn init() void {
    run_frame = async run();
}
