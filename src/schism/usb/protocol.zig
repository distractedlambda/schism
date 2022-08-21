const builtin = @import("builtin");
const std = @import("std");

const BitStruct = @import("../bits.zig").BitStruct;
const LittleEndian = @import("../endian.zig").LittleEndian;

pub const SetupPacket = struct {
    request_type: RequestType,
    request: Request,
    value: LittleEndian(u16),
    index: LittleEndian(u16),
    length: LittleEndian(u16),

    pub const RequestType = BitStruct(u8, .{
        .Record = &.{
            .{
                .name = "direction",
                .type = enum(u1) { Out, In },
                .lsb = 7,
            },
            .{
                .name = "type",
                .type = enum(u2) { Standard, Class, Vendor, _ },
                .lsb = 5,
            },
            .{
                .name = "recipient",
                .type = enum(u5) { Device, Interface, Endpoint, Other, _ },
                .lsb = 0,
            },
        },
    });

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
        Cdc = 0x02,
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

pub fn StringDescriptor(comptime len: usize) type {
    return packed struct {
        length: u8 = @sizeOf(@This()),
        descriptor_type: DescriptorType = .String,
        string: [len]u16,
    };
}
