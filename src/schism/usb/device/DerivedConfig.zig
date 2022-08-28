const std = @import("std");

const config = @import("../../Config.zig").resolved;
const control = @import("control.zig");
const device = @import("../device.zig");
const endian = @import("../../endian.zig");
const protocol = @import("../protocol.zig");

const LittleEndian = endian.LittleEndian;

control_request_handlers: []const device.ControlRequestHandler,
device_descriptor: []const u8,
configuration_descriptor: []const u8,
string_descriptors: []const []const u8,
channel_assignments: []const []const u4,
num_tx_channels: u4,
num_rx_channels: u4,

pub const resolved: @This() = blk: {
    const StringDescriptorTable = struct {
        descriptors: []const []const u8,

        fn init(comptime languages: []const protocol.LanguageId) @This() {
            comptime {
                var descriptor0_string: [languages.len]LittleEndian(u16) = undefined;
                for (languages) |language, i| descriptor0_string[i].assign(@enumToInt(language));
                const descriptor0 = protocol.StringDescriptor(languages.len){ .string = descriptor0_string };
                return .{ .descriptors = &.{&std.mem.toBytes(descriptor0)} };
            }
        }

        fn addUtf8(comptime self: *@This(), comptime utf8_or_null: ?[]const u8) u8 {
            comptime {
                const utf8 = utf8_or_null orelse return 0;
                const utf16 = std.unicode.utf8ToUtf16LeStringLiteral(utf8).*;
                const descriptor = protocol.StringDescriptor(utf16.len){ .string = @bitCast([utf16.len]LittleEndian(u16), @as([utf16.len]u16, utf16)) };
                self.descriptors = self.descriptors ++ [_][]const u8{&std.mem.toBytes(descriptor)};
                return self.descriptors.len - 1;
            }
        }
    };

    const usb_device_config = config.usb.?.Device;

    var string_descriptor_table = StringDescriptorTable.init(&.{usb_device_config.language_id});
    var interface_descriptors_blob: []const u8 = &.{};
    var channel_assignments: []const []const u4 = &.{};
    var num_tx_channels: u4 = 0;
    var num_rx_channels: u4 = 0;

    for (usb_device_config.interfaces) |interface_config, interface_index| {
        var endpoint_channel_assignments: [interface_config.endpoints.len]u4 = undefined;
        var endpoint_descriptors_blob: []const u8 = &.{};

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

            const endpoint_descriptor = std.mem.toBytes(protocol.EndpointDescriptor{
                .endpoint_address = protocol.EndpointDescriptor.EndpointAddress.init(.{
                    .endpoint_number = endpoint_channel_assignments[endpoint_index] + 1,
                    .direction = endpoint_config.direction,
                }),
                .attributes = protocol.EndpointDescriptor.Attributes.init(.{ .transfer_type = .Bulk }),
                .max_packet_size = LittleEndian(u16).init(64),
                .interval = 0, // FIXME correct value?
            });

            endpoint_descriptors_blob = endpoint_descriptors_blob ++ endpoint_descriptor;
        }

        const interface_descriptor = std.mem.toBytes(protocol.InterfaceDescriptor{
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

    const device_descriptor = std.mem.toBytes(protocol.DeviceDescriptor{
        .bcd_usb = LittleEndian(protocol.DeviceDescriptor.BcdUsb).init(.@"1.1"),
        .device_class = .Device,
        .device_subclass = 0,
        .device_protocol = 0,
        .ep0_max_packet_size = 64,
        .vendor_id = LittleEndian(u16).init(usb_device_config.vendor_id),
        .product_id = LittleEndian(u16).init(usb_device_config.product_id),
        .bcd_device = LittleEndian(u16).init(usb_device_config.bcd_device),
        .manufacturer_string_index = string_descriptor_table.addUtf8(usb_device_config.manufacturer),
        .product_string_index = string_descriptor_table.addUtf8(usb_device_config.product),
        .serial_number_string_index = string_descriptor_table.addUtf8(usb_device_config.serial_number),
        .num_configurations = 1,
    });

    const configuration_descriptor = std.mem.toBytes(protocol.ConfigurationDescriptor{
        .total_length = LittleEndian(u16).init(@sizeOf(protocol.ConfigurationDescriptor) + interface_descriptors_blob.len),
        .num_interfaces = usb_device_config.interfaces.len,
        .configuration_value = 1,
        .configuration_string_index = 0,
        .attributes = protocol.ConfigurationDescriptor.Attributes.init(.{
            .remote_wakeup = false,
            .self_powered = true,
        }),
        .max_power = 50,
    });

    break :blk .{
        .control_request_handlers = usb_device_config.control_request_handlers ++ control.standard_request_handlers,
        .device_descriptor = &device_descriptor,
        .configuration_descriptor = &configuration_descriptor,
        .string_descriptors = string_descriptor_table.descriptors,
        .channel_assignments = channel_assignments,
        .num_tx_channels = num_tx_channels,
        .num_rx_channels = num_rx_channels,
    };
};
