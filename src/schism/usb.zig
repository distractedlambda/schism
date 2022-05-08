const std = @import("std");

const arm = @import("../arm.zig");
const config = @import("config.zig");
const resets = @import("resets.zig");
const rp2040 = @import("../rp2040/rp2040.zig");

comptime {
    std.debug.assert(std.builtin.target.arch.cpu.endian() == .Little);
}

pub const SetupPacket = packed struct {
    request_type: RequestType,
    request: Request,
    value: u16,
    index: u16,
    length: u16,

    pub const RequestType = packed struct {
        recipient: enum(u4) {
            Device,
            Interface,
            Endpoint,
            Other,
            _,
        },
        type: enum(u2) {
            Standard,
            Class,
            Vendor,
            Reserved,
        },
        direction: enum(u1) {
            HostToDevice,
            DeviceToHost,
            _,
        },
    };

    pub const Request = enum(u8) {
        GetStatus = 0x00,
        ClearFeature = 0x01,
        SetFeature = 0x03,
        SetAddress = 0x05,
        GetDescriptor = 0x06,
        SetDescriptor = 0x07,
        GetConfiguration = 0x08,
        SetConfiguration = 0x09,
        GetInterface = 0x0A,
        SetInterface = 0x11,
        SynchFrame = 0x12,
        _,
    };
};

pub const DescriptorType = enum(u8) {
    Device = 0x01,
    Configuration = 0x02,
    String = 0x03,
    Interface = 0x04,
    Endpoint = 0x05,
    _,
};

pub const DeviceDescriptor = packed struct {
    length: u8 = @sizeOf(@This()),
    descriptor_type: DescriptorType = .Device,
    bcd_usb: BcdUsb,
    device_class: DeviceClass,
    device_subclass: u8,
    device_protocol: u8,
    ep0_max_packet_size: u8,
    vendor_id: u16,
    product_id: u16,
    bcd_device: u16,
    manufacturer_string_index: u8,
    product_string_index: u8,
    serial_number_string_index: u8,
    num_configurations: u8,

    pub const BcdUsb = enum(u16) {
        @"1.0" = 0x0100,
        @"1.1" = 0x0110,
        @"2.0" = 0x0200,
        _,
    };

    pub const DeviceClass = enum(u8) {
        Device = 0x00,
        CDC = 0x02,
        Hub = 0x09,
        Billboard = 0x11,
        Diagnostic = 0xdc,
        Miscellaneous = 0xef,
        VendorSpecific = 0xff,
        _,
    };
};

pub const ConfigurationDescriptor = packed struct {
    length: u8 = @sizeOf(@This()),
    descriptor_type: DescriptorType = .Configuration,
    total_length: u16,
    num_interfaces: u8,
    configuration_value: u8,
    configuration_string_index: u8,
    attributes: Attributes,
    max_power: u8,

    pub const Attributes = packed struct {
        reserved_0: u5 = 0,
        remote_wakeup: bool,
        self_powered: bool,
        reserved_1: u1 = 1,
    };
};

pub const InterfaceDescriptor = packed struct {
    length: u8 = @sizeOf(@This()),
    descriptor_type: DescriptorType = .Interface,
    interface_number: u8,
    alternate_setting: u8,
    num_endpoints: u8,
    interface_class: u8,
    interface_subclass: u8,
    interface_protocol: u8,
    interface_string_index: u8,
};

pub const EndpointDescriptor = packed struct {
    length: u8 = @sizeOf(@This()),
    descriptor_type: DescriptorType = .Endpoint,
    endpoint_address: EndpointAddress,
    attributes: Attributes,
    max_packet_size: u16,
    interval: u8,

    pub const EndpointAddress = packed struct {
        endpoint_number: u4,
        reserved: u3 = 0,
        direction: Direction,

        pub const Direction = enum(u1) {
            Out,
            In,
        };
    };

    pub const Attributes = packed struct {
        transfer_type: TransferType,
        synchronization_type: SynchronizationType = .None,
        usage_type: UsageType = .Data,
        reserved: u2 = 0,

        pub const TransferType = enum(u2) {
            Control,
            Isochronous,
            Bulk,
            Interrupt,
        };

        pub const SynchronizationType = enum(u2) {
            None,
            Asynchronous,
            Adaptive,
            Synchronous,
        };

        pub const UsageType = enum(u2) {
            Data,
            Feedback,
            ExplicitFeedbackData,
        };
    };
};

pub const LanguageId = enum(u16) {
    EnglishUnitedStates = 0x0409,
    _,
};

pub fn StringDescriptor0(comptime len: usize) type {
    return packed struct {
        length: u8 = @sizeOf(@This()),
        descriptor_type: DescriptorType = .String,
        language_ids: [len]LanguageId,
    };
}

pub fn StringDescriptor(comptime len: usize) type {
    return packed struct {
        length: u8 = @sizeOf(@This()),
        descriptor_type: DescriptorType = .String,
        string: [len:0]u8,
    };
}

pub inline fn stringDescriptor0(language_ids: anytype) StringDescriptor0(language_ids.len) {
    return .{ .language_ids = language_ids };
}

pub inline fn stringDescriptor(string: anytype) StringDescriptor(string.len) {
    return .{ .string = string };
}

const setup_packet = @intToPtr(*const volatile [8]u8, rp2040.usb.dpram_base_address);

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
    comptime var next_buffer = 1;
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

            const buffer_count = if (endpoint.double_buffer) 2 else 1;

            if (next_buffer + buffer_count > 60) {
                @compileError("exceeded maximum endpoint buffer count");
            }

            defer next_buffer += buffer_count;

            rp2040.usb.device_ep_ctrl.write(reg_index - 2, .{
                .en = true,
                .double_buf = endpoint.double_buffer,
                .int_1buf = true,
                .buf_address = next_buffer,
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

pub fn handleIrq() void {
    const usb_config = config.usb orelse @panic("USB IRQ received, but USB not enabled");

    const ints = rp2040.usb.ints.read();

    if (ints.setup_req) {
        rp2040.usb.sie_status.clear(.{.setup_rec});
        const setup_packet = @bitCast(SetupPacket, @intToPtr(*const volatile [8]u8, rp2040.usb.dpram_base_address).*);

    }
}
