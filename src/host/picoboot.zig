const builtin = @import("builtin");
const std = @import("std");

const libusb = @import("libusb.zig");

comptime {
    if (builtin.cpu.arch.endian() != .Little) {
        @compileError("packed struct definitions in this file assume a little-endian target");
    }
}

pub const Device = struct {
    device: *libusb.Device,
    configuration_value: u8,
    alternate_setting: u8,
    interface_number: u8,
    in_endpoint_number: u4,
    out_endpoint_number: u4,

    pub fn init(device: *libusb.Device) libusb.Error!?Device {
        const device_descriptor = try device.getDeviceDescriptor();

        if (device_descriptor.idVendor != 0x2e8a) return null;
        if (device_descriptor.idProduct != 0x0003) return null;

        var configuration_index: u8 = 0;
        while (configuration_index < device_descriptor.bNumConfigurations) : (configuration_index += 1) {
            const configuration_descriptor = try device.getConfigDescriptor(configuration_index);
            defer configuration_descriptor.free();

            for (configuration_descriptor.interfaces()) |interface| {
                for (interface.altsettings()) |interface_descriptor| {
                    if (interface_descriptor.bInterfaceClass != 0xff) continue;
                    if (interface_descriptor.bInterfaceSubClass != 0x00) continue;
                    if (interface_descriptor.bInterfaceProtocol != 0x00) continue;

                    var in_endpoint_number: ?u4 = null;
                    var out_endpoint_number: ?u4 = null;

                    for (interface_descriptor.endpoints()) |endpoint_descriptor| {
                        if (@truncate(u2, endpoint_descriptor.bmAttributes) != 0b10) continue;

                        const endpoint_number = @truncate(u4, endpoint_descriptor.bEndpointAddress);

                        if (endpoint_descriptor.bEndpointAddress >= 128) {
                            in_endpoint_number = endpoint_number;
                        } else {
                            out_endpoint_number = endpoint_number;
                        }
                    }

                    const resolved_in_endpoint_number = in_endpoint_number orelse continue;
                    const resolved_out_endpoint_number = out_endpoint_number orelse continue;

                    device.ref();

                    return Device{
                        .device = device,
                        .configuration_value = configuration_descriptor.bConfigurationValue,
                        .alternate_setting = interface_descriptor.bAlternateSetting,
                        .interface_number = interface_descriptor.bInterfaceNumber,
                        .in_endpoint_number = resolved_in_endpoint_number,
                        .out_endpoint_number = resolved_out_endpoint_number,
                    };
                }
            }
        }

        return null;
    }

    pub fn deinit(self: Device) void {
        self.device.unref();
    }

    pub fn findAny(context: *libusb.Context) libusb.Error!?Device {
        const device_list = try context.getDeviceList();
        defer libusb.Device.unrefAndFreeList(device_list);

        for (device_list) |device| {
            return Device.init(device) catch continue orelse continue;
        }

        return null;
    }

    pub fn open(self: Device) libusb.Error!Connection {
        const device_handle = try self.device.open();
        errdefer device_handle.close();
        try device_handle.setConfiguration(self.configuration_value);
        try device_handle.claimInterface(self.interface_number);
        errdefer device_handle.releaseInterface(self.interface_number) catch {};
        try device_handle.setInterfaceAlternateSetting(self.interface_number, self.alternate_setting);

        return Connection{
            .device_handle = device_handle,
            .interface_number = self.interface_number,
            .out_endpoint_number = self.out_endpoint_number,
            .in_endpoint_number = self.in_endpoint_number,
        };
    }
};

pub const Connection = struct {
    device_handle: *libusb.DeviceHandle,
    interface_number: u8,
    in_endpoint_number: u4,
    out_endpoint_number: u4,

    pub fn close(self: Connection) void {
        self.device_handle.releaseInterface(self.interface_number) catch {};
        self.device_handle.close();
    }

    pub fn setExclusiveAccess(self: Connection, exclusivity: Exclusivity) libusb.Error!void {
        try self.device_handle.bulkTransferOutExact(
            self.out_endpoint_number,
            &std.mem.toBytes(Command(Exclusivity){
                .command_id = .ExclusiveAccess,
                .transfer_length = 0,
                .args = exclusivity,
            }),
        );

        try self.device_handle.bulkTransferInExact(self.in_endpoint_number, &[_]u8{});
    }

    pub fn reboot(self: Connection, args: RebootArgs) libusb.Error!void {
        try self.device_handle.bulkTransferOutExact(
            self.out_endpoint_number,
            &std.mem.toBytes(Command(RebootArgs){
                .command_id = .Reboot,
                .transfer_length = 0,
                .args = args,
            }),
        );

        try self.device_handle.bulkTransferInExact(self.in_endpoint_number, &[_]u8{});
    }

    pub fn eraseFlash(self: Connection, first_sector: u20, sector_count: u20) libusb.Error!void {
        try self.device_handle.bulkTransferOutExact(
            self.out_endpoint_number,
            &std.mem.toBytes(Command(FlashEraseArgs){
                .command_id = .FlashErase,
                .transfer_length = 0,
                .args = .{
                    .flash_offset = @as(u32, first_sector) * 4096,
                    .len = @as(u32, sector_count) * 4096,
                },
            }),
        );

        try self.device_handle.bulkTransferInExact(self.in_endpoint_number, &[_]u8{});
    }

    pub fn read(self: Connection, buffer: []u8, device_address: u32) libusb.Error!void {
        // TODO: add safety checks for address ranges

        try self.device_handle.bulkTransferOutExact(
            self.out_endpoint_number,
            &std.mem.toBytes(Command(ReadWriteArgs){
                .command_id = .Read,
                .transfer_length = @intCast(u32, buffer.len),
                .args = .{
                    .address = device_address,
                    .len = @intCast(u32, buffer.len),
                },
            }),
        );

        try self.device_handle.bulkTransferInExact(self.in_endpoint_number, buffer);

        try self.device_handle.bulkTransferOutExact(self.out_endpoint_number, &[_]u8{});
    }

    pub fn write(self: Connection, data: []const u8, device_address: u32) libusb.Error!void {
        // TODO: add safety checks for address ranges

        try self.device_handle.bulkTransferOutExact(
            self.out_endpoint_number,
            &std.mem.toBytes(Command(ReadWriteArgs){
                .command_id = .Write,
                .transfer_length = @intCast(u32, data.len),
                .args = .{
                    .address = device_address,
                    .len = @intCast(u32, data.len),
                },
            }),
        );

        try self.device_handle.bulkTransferOutExact(self.out_endpoint_number, data);

        try self.device_handle.bulkTransferInExact(self.in_endpoint_number, &[_]u8{});
    }
};

const ReadWriteArgs = packed struct {
    address: u32,
    len: u32,
};

const FlashEraseArgs = packed struct {
    flash_offset: u32,
    len: u32,
};

pub const RebootArgs = packed struct {
    start_address: u32 = 0,
    stack_pointer: u32 = 0,
    delay_milliseconds: u32 = 0,
};

pub const Exclusivity = enum(u8) {
    NotExclusive,
    Exclusive,
    ExclusiveAndEject,
};

fn Command(comptime Args: type) type {
    comptime std.debug.assert(@bitSizeOf(Args) % 8 == 0);
    comptime std.debug.assert(@sizeOf(Args) <= 16);

    return packed struct {
        magic: u32 = 0x431fd10b,
        token: u32 = 0,
        command_id: CommandId,
        command_size: u8 = @sizeOf(Args),
        reserved: u16 = 0,
        transfer_length: u32,
        args: Args,
        args_padding: [16 - @sizeOf(Args)]u8 = [1]u8{0} ** (16 - @sizeOf(Args)),
    };
}

const CommandId = enum(u8) {
    ExclusiveAccess = 0x01,
    Reboot = 0x02,
    FlashErase = 0x03,
    Read = 0x84,
    Write = 0x05,
    ExitXip = 0x06,
    EnterXip = 0x07,
    Exec = 0x08,
    VectorizeFlash = 0x09,
};
