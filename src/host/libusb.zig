const std = @import("std");

pub const Error = error{
    Access,
    Busy,
    Cancelled,
    IncompleteTransfer,
    Interrupted,
    InvalidParam,
    Io,
    NoDevice,
    NoMem,
    NotFound,
    NotSupported,
    Other,
    Overflow,
    Pipe,
    Stall,
    Timeout,
};

pub const DeviceDescriptor = extern struct {
    bLength: u8,
    bDescriptorType: u8,
    bcdUSB: u16,
    bDeviceClass: u8,
    bDeviceSubClass: u8,
    bDeviceProtocol: u8,
    bMaxPacketSize0: u8,
    idVendor: u16,
    idProduct: u16,
    bcdDevice: u16,
    iManufacturer: u8,
    iProduct: u8,
    iSerialNumber: u8,
    bNumConfigurations: u8,
};

pub const ConfigDescriptor = extern struct {
    bLength: u8,
    bDescriptorType: u8,
    wTotalLength: u16,
    bNumInterfaces: u8,
    bConfigurationValue: u8,
    iConfiguration: u8,
    bmAttributes: u8,
    MaxPower: u8,
    interface: [*]const Interface,
    extra: [*]const u8,
    extra_length: c_int,

    pub fn free(self: *ConfigDescriptor) void {
        libusb_free_config_descriptor(self);
    }

    pub fn interfaces(self: *const ConfigDescriptor) []const Interface {
        return self.interface[0..self.bNumInterfaces];
    }
};

pub const Interface = extern struct {
    altsetting: [*]const InterfaceDescriptor,
    num_altsetting: c_int,

    pub fn altsettings(self: *const Interface) []const InterfaceDescriptor {
        return self.altsetting[0..self.num_altsetting];
    }
};

pub const InterfaceDescriptor = extern struct {
    bLength: u8,
    bDescriptorType: u8,
    bInterfaceNumber: u8,
    bAlternateSetting: u8,
    bNumEndpoints: u8,
    bInterfaceClass: u8,
    bInterfaceSubClass: u8,
    bInterfaceProtocol: u8,
    iInterface: u8,
    endpoint: [*]const EndpointDescriptor,
    extra: [*]const u8,
    extra_length: c_int,

    pub fn endpoints(self: *const InterfaceDescriptor) []const EndpointDescriptor {
        return self.endpoint[0..self.bNumEndpoints];
    }
};

pub const EndpointDescriptor = extern struct {
    bLength: u8,
    bDescriptorType: u8,
    bEndpointAddress: u8,
    bmAttributes: u8,
    wMaxPacketSize: u16,
    bInterval: u8,
    bRefresh: u8,
    bSynchAddress: u8,
    extra: [*]const u8,
    extra_length: c_int,
};

pub const Context = opaque {
    pub fn init() Error!*Context {
        var result: *Context = undefined;
        try checkError(libusb_init(&result));
        return result;
    }

    pub fn exit(self: *Context) void {
        libusb_exit(self);
    }

    pub fn getDeviceList(self: *Context) Error![]*Device {
        var list: **Device = undefined;
        const len = try checkLenError(libusb_get_device_list(self, &list));
        return list[0..len];
    }

    pub fn handleEvents(self: *Context) Error!void {
        try checkError(libusb_handle_events(self));
    }
};

pub const Device = opaque {
    pub fn ref(self: *Device) void {
        _ = libusb_ref_device(self);
    }

    pub fn unref(self: *Device) void {
        libusb_unref_device(self);
    }

    pub fn unrefAndFreeList(list: []*Device) void {
        libusb_free_device_list(list.ptr, 1);
    }

    pub fn freeList(list: []*Device) void {
        libusb_free_device_list(list.ptr, 0);
    }

    pub fn open(self: *Device) Error!DeviceHandle {
        var handle: *DeviceHandle = undefined;
        try checkError(libusb_open(self, &handle));
        return handle;
    }

    pub fn getDeviceDescriptor(self: *Device) Error!DeviceDescriptor {
        var descriptor: DeviceDescriptor = undefined;
        try checkError(libusb_get_device_descriptor(self, &descriptor));
        return descriptor;
    }

    pub fn getConfigDescriptor(self: *Device, config_index: u8) Error!*ConfigDescriptor {
        var descriptor: *ConfigDescriptor = undefined;
        try checkError(libusb_get_config_descriptor(self, config_index, &descriptor));
        return descriptor;
    }

    pub fn getActiveConfigDescriptor(self: *Device) Error!*ConfigDescriptor {
        var descriptor: *ConfigDescriptor = undefined;
        try checkError(libusb_get_active_config_descriptor(self, &descriptor));
        return descriptor;
    }
};

pub const DeviceHandle = opaque {
    pub fn close(self: *DeviceHandle) void {
        libusb_close(self);
    }

    pub fn device(self: *DeviceHandle) *Device {
        return libusb_get_device(self);
    }

    pub fn reset(self: *DeviceHandle) Error!void {
        try checkError(libusb_reset_device(self));
    }

    pub fn setConfiguration(self: *DeviceHandle, configuration_value: ?u8) Error!void {
        try checkError(libusb_set_configuration(self, configuration_value orelse -1));
    }

    pub fn clearHalt(self: *DeviceHandle, endpoint: u8) Error!void {
        try checkError(libusb_clear_halt(self, endpoint));
    }

    pub fn claimInterface(self: *DeviceHandle, interface_number: u8) Error!void {
        try checkError(libusb_claim_interface(self, interface_number));
    }

    pub fn releaseInterface(self: *DeviceHandle, interface_number: u8) Error!void {
        try checkError(libusb_release_interface(self, interface_number));
    }

    pub fn setInterfaceAlternateSetting(self: *DeviceHandle, interface_number: u8, alternate_setting: u8) Error!void {
        try checkError(libusb_set_interface_alt_setting(self, interface_number, alternate_setting));
    }

    pub fn getStringDescriptorAscii(self: *DeviceHandle, index: u8, buffer: []u8) Error!usize {
        return checkLenError(libusb_get_string_descriptor_ascii(self, index, buffer.ptr, std.math.lossyCast(c_int, buffer.len)));
    }

    pub fn getStringDescriptorUtf16Le(self: *DeviceHandle, lang_id: u16, index: u8, buffer: []u8) Error!usize {
        return checkLenError(libusb_get_string_descriptor(self, index, lang_id, buffer.ptr, std.math.lossyCast(c_int, buffer.len)));
    }

    pub fn bulkTransfer(self: *DeviceHandle, endpoint: u8, buffer: []u8) Error!usize {
        const transfer = libusb_alloc_transfer(0) orelse return error.NoMem;
        defer libusb_free_transfer(transfer);

        var continuation = TransferContinuation{ .frame = @frame() };

        suspend {
            libusb_fill_bulk_transfer(
                transfer,
                self,
                endpoint,
                buffer.ptr,
                std.math.lossyCast(c_int, buffer.len),
                transferCallback,
                &continuation,
                0,
            );

            checkError(libusb_submit_transfer(transfer)) catch |err| {
                continuation.result = err;
                resume @frame();
            };
        }

        return continuation.result;
    }

    pub fn bulkTransferIn(self: *DeviceHandle, endpoint_number: u4, buffer: []u8) Error!usize {
        return self.bulkTransfer(@as(u8, endpoint_number) | 0x80, buffer);
    }

    pub fn bulkTransferOut(self: *DeviceHandle, endpoint_number: u4, buffer: []const u8) Error!usize {
        return self.bulkTransfer(@as(u8, endpoint_number), @ptrCast([]u8, buffer));
    }

    pub fn bulkTransferInExact(self: *DeviceHandle, endpoint_number: u4, buffer: []u8) Error!void {
        if (try self.bulkTransferIn(endpoint_number, buffer) != buffer.len) {
            return error.IncompleteTransfer;
        }
    }

    pub fn bulkTransferOutExact(self: *DeviceHandle, endpoint_number: u4, buffer: []const u8) Error!void {
        if (try self.bulkTransferOut(endpoint_number, buffer) != buffer.len) {
            return error.IncompleteTransfer;
        }
    }
};

const TransferContinuation = struct {
    frame: anyframe,
    result: Error!usize = undefined,
};

const TransferStatus = enum(c_int) {
    Completed,
    Error,
    TimedOut,
    Canceled,
    Stall,
    NoDevice,
    Overflow,
    _,
};

const TransferCallbackFunction = fn (*Transfer) callconv(.C) void;

const Transfer = extern struct {
    dev_handle: *DeviceHandle,
    flags: u8,
    endpoint: u8,
    type: u8,
    timeout: c_uint,
    status: TransferStatus,
    length: c_int,
    actual_length: c_int,
    callback: ?TransferCallbackFunction,
    user_data: ?*anyopaque,
    buffer: [*]u8,
    num_iso_packets: c_int,
};

fn checkError(code: c_int) Error!void {
    if (code != 0) {
        return convertError(code);
    }
}

fn checkLenError(len: anytype) Error!std.math.IntFittingRange(0, std.math.maxInt(@TypeOf(len))) {
    if (len < 0) {
        return convertError(len);
    } else {
        return @intCast(std.math.IntFittingRange(0, std.math.maxInt(@TypeOf(len))), len);
    }
}

fn convertError(code: anytype) Error {
    return switch (code) {
        -1 => error.Io,
        -2 => error.InvalidParam,
        -3 => error.Access,
        -4 => error.NoDevice,
        -5 => error.NotFound,
        -6 => error.Busy,
        -7 => error.Timeout,
        -8 => error.Overflow,
        -9 => error.Pipe,
        -10 => error.Interrupted,
        -11 => error.NoMem,
        -12 => error.NotSupported,
        -99 => error.Other,
        else => error.UnknownLibUsbError,
    };
}

fn transferCallback(transfer: *Transfer) callconv(.C) void {
    const continuation = @ptrCast(*TransferContinuation, transfer.user_data);

    continuation.result = switch (transfer.status) {
        .Completed => @intCast(usize, transfer.actual_length),
        .Error => error.Other,
        .TimedOut => error.Timeout,
        .Canceled => error.Cancelled,
        .Stall => error.Stall,
        .NoDevice => error.NoDevice,
        .Overflow => error.Overflow,
        else => error.Other,
    };

    const frame = continuation.frame;
    resume frame;
}

extern fn libusb_init(context_out: **Context) c_int;

extern fn libusb_exit(context: *Context) void;

extern fn libusb_ref_device(device: *Device) *Device;

extern fn libusb_unref_device(device: *Device) void;

extern fn libusb_get_device_list(context: ?*Context, list: *[*]*Device) isize;

extern fn libusb_free_device_list(list: [*]*Device, unref_devices: c_int) void;

extern fn libusb_open(dev: *Device, dev_handle: **DeviceHandle) c_int;

extern fn libusb_close(dev_handle: *DeviceHandle) void;

extern fn libusb_get_device(dev_handle: *DeviceHandle) *Device;

extern fn libusb_reset_device(dev_handle: *DeviceHandle) c_int;

extern fn libusb_clear_halt(dev_handle: *DeviceHandle, endpoint: u8) c_int;

extern fn libusb_claim_interface(dev_handle: *DeviceHandle, interface_number: c_int) c_int;

extern fn libusb_release_interface(dev_handle: *DeviceHandle, interface_number: c_int) c_int;

extern fn libusb_get_device_descriptor(dev: *Device, desc: *DeviceDescriptor) c_int;

extern fn libusb_free_config_descriptor(config: *ConfigDescriptor) void;

extern fn libusb_get_config_descriptor(dev: *Device, config_index: u8, config: **ConfigDescriptor) c_int;

extern fn libusb_get_active_config_descriptor(dev: *Device, config: **ConfigDescriptor) c_int;

extern fn libusb_get_string_descriptor_ascii(
    dev_handle: *DeviceHandle,
    desc_index: u8,
    data: [*]u8,
    length: c_int,
) c_int;

extern fn libusb_get_string_descriptor(
    dev_handle: *DeviceHandle,
    desc_index: u8,
    langid: u16,
    data: [*]u8,
    length: c_int,
) c_int;

extern fn libusb_alloc_transfer(iso_packets: c_int) ?*Transfer;

extern fn libusb_free_transfer(transfer: ?*Transfer) void;

extern fn libusb_submit_transfer(transfer: *Transfer) c_int;

extern fn libusb_fill_bulk_transfer(
    transfer: *Transfer,
    dev_handle: *DeviceHandle,
    endpoint: u8,
    buffer: [*]u8,
    length: c_int,
    callback: TransferCallbackFunction,
    user_data: ?*anyopaque,
    timeout: c_uint,
) void;

extern fn libusb_handle_events(ctx: ?*Context) c_int;

extern fn libusb_set_configuration(dev_handle: *DeviceHandle, configuration: c_int) c_int;

extern fn libusb_set_interface_alt_setting(
    dev_handle: *DeviceHandle,
    interface_number: c_int,
    alternate_setting: c_int,
) c_int;
