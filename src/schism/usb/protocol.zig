const BitStruct = @import("../bits.zig").BitStruct;
const LittleEndian = @import("../endian.zig").LittleEndian;

// FIXME
pub const SetupPacket = packed struct {
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
    ClassSpecificInterface = 0x24,
    ClassSpecificEndpoint = 0x25,
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

pub const InterfaceClass = enum(u8) {
    Cdc = 0x02,
    Data = 0x0a,
    _,
};

// FIXME
pub const DeviceDescriptor = packed struct {
    length: u8 = @sizeOf(@This()),
    descriptor_type: DescriptorType = .Device,
    bcd_usb: LittleEndian(BcdUsb),
    device_class: DeviceClass,
    device_subclass: u8,
    device_protocol: u8,
    ep0_max_packet_size: u8,
    vendor_id: LittleEndian(u16),
    product_id: LittleEndian(u16),
    bcd_device: LittleEndian(u16),
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
};

pub const ConfigurationDescriptor = packed struct {
    length: u8 = @sizeOf(@This()),
    descriptor_type: DescriptorType = .Configuration,
    total_length: LittleEndian(u16),
    num_interfaces: u8,
    configuration_value: u8,
    configuration_string_index: u8,
    attributes: Attributes,
    max_power: u8,

    pub const Attributes = BitStruct(u8, .{
        .Record = &.{
            .{
                .name = "usb_1_0_bus_powered",
                .type = bool,
                .lsb = 7,
                .default = &true,
            },
            .{
                .name = "self_powered",
                .type = bool,
                .lsb = 6,
                .default = &false,
            },
            .{
                .name = "remote_wakeup",
                .type = bool,
                .lsb = 5,
                .default = &false,
            },
        },
    });
};

pub const InterfaceDescriptor = packed struct {
    length: u8 = @sizeOf(@This()),
    descriptor_type: DescriptorType = .Interface,
    interface_number: u8,
    alternate_setting: u8,
    num_endpoints: u8,
    interface_class: InterfaceClass,
    interface_subclass: u8,
    interface_protocol: u8,
    interface_string_index: u8,
};

pub const EndpointDescriptor = packed struct {
    length: u8 = @sizeOf(@This()),
    descriptor_type: DescriptorType = .Endpoint,
    endpoint_address: EndpointAddress,
    attributes: Attributes,
    max_packet_size: LittleEndian(u16),
    interval: u8,

    pub const EndpointAddress = BitStruct(u8, .{
        .Record = &.{
            .{
                .name = "direction",
                .type = Direction,
                .lsb = 7,
            },
            .{
                .name = "endpoint_number",
                .type = u4,
                .lsb = 0,
            },
        },
    });

    pub const Direction = enum(u1) {
        Out,
        In,
    };

    pub const Attributes = BitStruct(u8, .{
        .Record = &.{
            .{
                .name = "usage_type",
                .type = IsochronousUsageType,
                .lsb = 4,
                .default = &@as(IsochronousUsageType, .Data),
            },
            .{
                .name = "synchronization_type",
                .type = IsochronousSynchronizationType,
                .lsb = 2,
                .default = &@as(IsochronousSynchronizationType, .None),
            },
            .{
                .name = "transfer_type",
                .type = TransferType,
                .lsb = 0,
            },
        },
    });

    pub const IsochronousUsageType = enum(u2) {
        Data,
        Feedback,
        ExplicitFeedbackData,
    };

    pub const IsochronousSynchronizationType = enum(u2) {
        None,
        Asynchronous,
        Adaptive,
        Synchronous,
    };

    pub const TransferType = enum(u2) {
        Control,
        Isochronous,
        Bulk,
        Interrupt,
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
        string: [len]LittleEndian(u16),
    };
}
