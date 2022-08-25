const BitStruct = @import("../bits.zig").BitStruct;
const DescriptorType = @import("protocol.zig").DescriptorType;

pub const Subclass = enum(u8) {
    DirectLineControl = 0x01,
    AbstractControl = 0x02,
    TelephoneControl = 0x03,
    MultiChannelControl = 0x04,
    CapiControl = 0x05,
    EthernetNetworkingControl = 0x06,
    AtmNetworkingControl = 0x07,
    WirelessHandsetControl = 0x08,
    DeviceManagement = 0x09,
    MobileDirectLine = 0x0a,
    Obex = 0x0b,
    EthernetEmulation = 0x0c,
    NetworkControl = 0x0d,
    _,
};

pub const DescriptorSubtype = enum(u8) {
    Header = 0x00,
    CallManagement = 0x01,
    AbstractControlManagement = 0x02,
    DirectLineManagement = 0x03,
    TelephoneRinger = 0x04,
    TelephoneCallAndLineStateReportingCapabilities = 0x05,
    Union = 0x06,
    CountrySelection = 0x07,
    TelephoneOperationalModes = 0x08,
    UsbTerminal = 0x09,
    NetworkChannelTerminal = 0x0a,
    ProtocolUnit = 0x0b,
    ExtensionUnit = 0x0c,
    MultiChannelManagement = 0x0d,
    CapiControlManagement = 0x0e,
    EthernetNetworking = 0x0f,
    AtmNetworking = 0x10,
    WirelessHandsetControlModel = 0x11,
    ModileDirectLineModel = 0x12,
    MdlmDetail = 0x13,
    DeviceManagementModel = 0x14,
    Obex = 0x15,
    CommandSet = 0x16,
    CommandSetDetail = 0x17,
    TelephoneControlModel = 0x18,
    ObexServiceIdentifier = 0x19,
    Ncm = 0x1a,
    _,
};

pub const HeaderDescriptor = packed struct {
    function_length: u8 = @sizeOf(@This()),
    descriptor_type: DescriptorType = .ClassSpecificInterface,
    descriptor_subtype: DescriptorSubtype = .Header,
    bcd_cdc: u8 = 0x0120,
};

pub fn UnionDescriptor(comptime subordinate_interfaces_len: usize) type {
    return packed struct {
        function_length: u8 = @sizeOf(@This()),
        descriptor_type: DescriptorType = .ClassSpecificInterface,
        descriptor_subtype: DescriptorSubtype = .Union,
        control_interface: u8,
        subordinate_interfaces: [subordinate_interfaces_len]u8,
    };
}

pub const AtmNetworkingDescriptor = packed struct {
    function_length: u8 = @sizeOf(@This()),
    descriptor_type: DescriptorType = .ClassSpecificInterface,
    descriptor_subtype: DescriptorSubtype = .AtmNetworking,
    end_system_identifier_index: u8,
    data_capabilities: DataCapabilities,
    device_statistics: DeviceStatistics,
    type_2_max_segment_size: u16,
    type_3_max_segment_size: u16,
    max_vc: u16,

    pub const DataCapabilities = BitStruct(u8, .{
        .Record = &.{
            .{
                .name = "type_3",
                .type = bool,
                .lsb = 3,
                .default = &false,
            },
            .{
                .name = "type_2",
                .type = bool,
                .lsb = 2,
                .default = &false,
            },
            .{
                .name = "type_1",
                .type = bool,
                .lsb = 1,
                .default = &true,
            },
        },
    });

    pub const DeviceStatistics = BitStruct(u8, .{
        .Record = &.{
            .{
                .name = "vc_us_cells_sent",
                .type = bool,
                .lsb = 4,
                .default = &false,
            },
            .{
                .name = "vc_ds_cells_received",
                .type = bool,
                .lsb = 3,
                .default = &false,
            },
            .{
                .name = "ds_cells_hec_error_corrected",
                .type = bool,
                .lsb = 2,
                .default = &false,
            },
            .{
                .name = "us_cells_sent",
                .type = bool,
                .lsb = 1,
                .default = &false,
            },
            .{
                .name = "ds_cells_received",
                .type = bool,
                .lsb = 0,
                .default = &false,
            },
        },
    });
};

pub const Request = enum(u8) {
    SendEncapsulatedCommand = 0x00,
    GetEncapsulatedResponse = 0x01,
    SetCommFeature = 0x02,
    GetCommFeature = 0x03,
    ClearCommFeature = 0x04,
    SetAuxLineState = 0x10,
    SetHookState = 0x11,
    PulseSetup = 0x12,
    SendPulse = 0x13,
    SetPulseTime = 0x14,
    RingAuxJack = 0x15,
    SetLineCoding = 0x20,
    GetLineCoding = 0x21,
    SetControlLineState = 0x22,
    SendBreak = 0x23,
    SetRingerParms = 0x30,
    GetRingerParms = 0x31,
    SetOperationParms = 0x32,
    GetOperationParms = 0x33,
    SetLineParms = 0x34,
    GetLineParms = 0x35,
    DialDigits = 0x36,
    SetUnitParameter = 0x37,
    GetUnitParameter = 0x38,
    ClearUnitParameter = 0x39,
    GetProfile = 0x3a,
    SetEthernetMulticastFilters = 0x40,
    SetEthernetPowerManagementPatternFilter = 0x41,
    GetEthernetPowerManagementPatternFilter = 0x42,
    SetEthernetPacketFilter = 0x43,
    GetEthernetStatistic = 0x44,
    SetAtmDataFormat = 0x50,
    GetAtmDeviceStatistics = 0x51,
    SetAtmDefaultVc = 0x52,
    GetAtmVcStatistics = 0x53,
    GetNtbParameters = 0x80,
    GetNetAddress = 0x81,
    SetNetAddress = 0x82,
    GetNtbFormat = 0x83,
    SetNtbFormat = 0x84,
    GetNtbInputSize = 0x85,
    SetNtbInputSize = 0x86,
    GetMaxDatagramSize = 0x87,
    SetMaxDatagramSize = 0x88,
    GetCrcMode = 0x89,
    SetCrcMode = 0x8a,
    _,
};

pub const Notification = enum(u8) {
    NetworkConnection = 0x00,
    ResponseAvailable = 0x01,
    AuxJackHookState = 0x08,
    RingDetect = 0x09,
    SerialState = 0x20,
    CallStateChange = 0x28,
    LineStateChange = 0x29,
    ConnectionSpeedChange = 0x2a,
    _,
};

pub const LineCoding = packed struct {
    data_terminal_rate: u32,
    char_format: StopBits,
    parity_type: Parity,
    data_bits: u8,

    pub const StopBits = enum(u8) {
        @"1",
        @"1.5",
        @"2",
    };

    pub const Parity = enum(u8) {
        None,
        Odd,
        Even,
        Mark,
        Space,
    };
};
