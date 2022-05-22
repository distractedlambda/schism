#![allow(non_camel_case_types, unused, non_snake_case)]

use std::{
    mem::MaybeUninit,
    os::raw::{c_char, c_int, c_uchar, c_uint, c_void},
};

#[link(name = "usb-1.0")]
extern "C" {
    pub fn libusb_set_log_cb(ctx: *mut libusb_context, cb: Option<libusb_log_cb>, mode: c_int);

    pub fn libusb_set_option(ctx: *mut libusb_context, option: libusb_option, ...) -> c_int;

    pub fn libusb_init(ctx: Option<&mut MaybeUninit<*mut libusb_context>>) -> c_int;

    pub fn libusb_exit(ctx: *mut libusb_context);

    pub fn libusb_error_name(error_code: c_int) -> *const c_char;

    pub fn libusb_has_capability(capability: u32) -> c_int;

    pub fn libusb_setlocale(locale: *const c_char) -> c_int;

    pub fn libusb_strerror(errcode: c_int) -> *const c_char;

    pub fn libusb_get_device_list(
        ctx: *mut libusb_context,
        list: &mut MaybeUninit<*mut *mut libusb_device>,
    ) -> isize;

    pub fn libusb_free_device_list(list: *mut *mut libusb_device, unref_devices: c_int);

    pub fn libusb_get_bus_number(dev: *mut libusb_device) -> u8;

    pub fn libusb_get_port_number(dev: *mut libusb_device) -> u8;

    pub fn libusb_get_port_numbers(
        dev: *mut libusb_device,
        port_numbers: *mut u8,
        port_numbers_len: c_int,
    ) -> c_int;

    pub fn libusb_get_device_address(dev: *mut libusb_device) -> u8;

    pub fn libusb_get_device_speed(dev: *mut libusb_device) -> c_int;

    pub fn libusb_get_max_packet_size(dev: *mut libusb_device, endpoint: c_uchar) -> c_int;

    pub fn libusb_get_max_iso_packet_size(dev: *mut libusb_device, endpoint: c_uchar) -> c_int;

    pub fn libusb_ref_device(dev: *mut libusb_device) -> *mut libusb_device;

    pub fn libusb_unref_device(dev: *mut libusb_device);

    pub fn libusb_open(
        dev: *mut libusb_device,
        dev_handle: &mut MaybeUninit<*mut libusb_device_handle>,
    ) -> c_int;

    pub fn libusb_close(dev_handle: *mut libusb_device_handle);

    pub fn libusb_get_device(dev_handle: *mut libusb_device_handle) -> *mut libusb_device;

    pub fn libusb_get_configuration(
        dev_handle: *mut libusb_device_handle,
        config: &mut MaybeUninit<c_int>,
    ) -> c_int;

    pub fn libusb_set_configuration(
        dev_handle: *mut libusb_device_handle,
        configuration: c_int,
    ) -> c_int;

    pub fn libusb_claim_interface(
        dev_handle: *mut libusb_device_handle,
        interface_number: c_int,
    ) -> c_int;

    pub fn libusb_release_interface(
        dev_handle: *mut libusb_device_handle,
        interface_number: c_int,
    ) -> c_int;

    pub fn libusb_set_interface_alt_setting(
        dev_handle: *mut libusb_device_handle,
        interface_number: c_int,
        alternate_setting: c_int,
    ) -> c_int;

    pub fn libusb_clear_halt(dev_handle: *mut libusb_device_handle, endpoint: c_uchar) -> c_int;

    pub fn libusb_reset_device(dev_handle: *mut libusb_device_handle) -> c_int;

    pub fn libusb_kernel_driver_active(
        dev_handle: *mut libusb_device_handle,
        interface_number: c_int,
    ) -> c_int;

    pub fn libusb_detach_kernel_driver(
        dev_handle: *mut libusb_device_handle,
        interface_number: c_int,
    ) -> c_int;

    pub fn libusb_attach_kernel_driver(
        dev_handle: *mut libusb_device_handle,
        interface_number: c_int,
    ) -> c_int;

    pub fn libusb_set_auto_detach_kernel_driver(
        dev_handle: *mut libusb_device_handle,
        enable: c_int,
    ) -> c_int;

    pub fn libusb_get_device_descriptor(
        dev: *mut libusb_device,
        desc: &mut MaybeUninit<libusb_device_descriptor>,
    ) -> c_int;

    pub fn libusb_get_active_config_descriptor(
        dev: *mut libusb_device,
        config: &mut MaybeUninit<*mut libusb_config_descriptor>,
    ) -> c_int;

    pub fn libusb_get_config_descriptor(
        dev: *mut libusb_device,
        config_index: u8,
        config: &mut MaybeUninit<*mut libusb_config_descriptor>,
    ) -> c_int;

    pub fn libusb_get_config_descriptor_by_value(
        dev: *mut libusb_device,
        bConfigurationValue: u8,
        config: &mut MaybeUninit<*mut libusb_config_descriptor>,
    ) -> c_int;

    pub fn libusb_free_config_descriptor(config: *mut libusb_config_descriptor);

    pub fn libusb_alloc_transfer(iso_packets: c_int) -> *mut libusb_transfer;

    pub fn libusb_free_transfer(transfer: *mut libusb_transfer);

    pub fn libusb_submit_transfer(transfer: *mut libusb_transfer) -> c_int;

    pub fn libusb_cancel_transfer(transfer: *mut libusb_transfer) -> c_int;

    pub fn libusb_interrupt_event_handler(ctx: *mut libusb_context);

    pub fn libusb_handle_events(ctx: *mut libusb_context) -> c_int;
}

pub const LIBUSB_TRANSFER_COMPLETED: c_int = 0;
pub const LIBUSB_TRANSFER_ERROR: c_int = 1;
pub const LIBUSB_TRANSFER_TIMED_OUT: c_int = 2;
pub const LIBUSB_TRANSFER_CANCELED: c_int = 3;
pub const LIBUSB_TRANSFER_STALL: c_int = 4;
pub const LIBUSB_TRANSFER_NO_DEVICE: c_int = 5;
pub const LIBUSB_TRANSFER_OVERFLOW: c_int = 6;

pub const LIBUSB_TRANSFER_TYPE_CONTROL: c_uchar = 0;
pub const LIBUSB_TRANSFER_TYPE_ISOCHRONOUS: c_uchar = 1;
pub const LIBUSB_TRANSFER_TYPE_BULK: c_uchar = 2;
pub const LIBUSB_TRANSFER_TYPE_INTERRUPT: c_uchar = 3;
pub const LIBUSB_TRANSFER_TYPE_BULK_STREAM: c_uchar = 4;

pub const LIBUSB_CAP_HAS_CAPABILITY: u32 = 0x0000;
pub const LIBUSB_CAP_HAS_HOTPLUG: u32 = 0x0001;
pub const LIBUSB_CAP_HAS_HID_ACCESS: u32 = 0x0100;
pub const LIBUSB_CAP_SUPPORTS_DETACH_KERNEL_DRIVER: u32 = 0x0101;

pub const LIBUSB_LOG_CB_GLOBAL: c_int = 0x1;
pub const LIBUSB_LOG_CB_CONTEXT: c_int = 0x2;

pub type libusb_transfer_cb_fn = unsafe extern "C" fn(transfer: &mut libusb_transfer);

type libusb_log_cb =
    unsafe extern "C" fn(ctx: *mut libusb_context, level: libusb_log_level, str: *const c_char);

#[repr(C)]
pub struct libusb_transfer {
    pub dev_handle: *mut libusb_device_handle,
    pub flags: u8,
    pub endpoint: c_uchar,
    pub r#type: c_uchar,
    pub timeout: c_uint,
    pub status: c_int,
    pub length: c_int,
    pub actual_length: c_int,
    pub callback: libusb_transfer_cb_fn,
    pub user_data: *mut c_void,
    pub buffer: *mut c_uchar,
    pub num_iso_packets: c_int,
}

#[repr(C)]
pub struct libusb_device_descriptor {
    pub bLength: u8,
    pub bDescriptorType: u8,
    pub bcdUSB: u16,
    pub bDeviceClass: u8,
    pub bDeviceSubClass: u8,
    pub bDeviceProtocol: u8,
    pub bMaxPacketSize0: u8,
    pub idVendor: u16,
    pub idProduct: u16,
    pub bcdDevice: u16,
    pub iManufacturer: u8,
    pub iProduct: u8,
    pub iSerialNumber: u8,
    pub bNumConfigurations: u8,
}

#[repr(C)]
pub struct libusb_config_descriptor {
    pub bLength: u8,
    pub bDescriptorType: u8,
    pub wTotalLength: u16,
    pub bNumInterfaces: u8,
    pub bConfigurationValue: u8,
    pub iConfiguration: u8,
    pub bmAttributes: u8,
    pub MaxPower: u8,
    pub interface: *const libusb_interface,
    pub extra: *const c_uchar,
    pub extra_length: c_int,
}

#[repr(C)]
pub struct libusb_interface {
    pub altsetting: *const libusb_interface_descriptor,
    pub num_altsetting: c_int,
}

#[repr(C)]
pub struct libusb_interface_descriptor {
    pub bLength: u8,
    pub bDescriptorType: u8,
    pub bInterfaceNumber: u8,
    pub bAlternateSetting: u8,
    pub bNumEndpoints: u8,
    pub bInterfaceClass: u8,
    pub bInterfaceSubClass: u8,
    pub bInterfaceProtocol: u8,
    pub iInterface: u8,
    pub endpoint: *const libusb_endpoint_descriptor,
    pub extra: *const c_uchar,
    pub extra_length: c_int,
}

#[repr(C)]
pub struct libusb_endpoint_descriptor {
    pub bLength: u8,
    pub bDescriptorType: u8,
    pub bEndpointAddress: u8,
    pub bmAttributes: u8,
    pub wMaxPacketSize: u16,
    pub bInterval: u8,
    pub bRefresh: u8,
    pub bSynchAddress: u8,
    pub extra: *const c_uchar,
    pub extra_length: c_int,
}

#[repr(C)]
pub struct libusb_context {
    _empty: [u8; 0],
}

#[repr(C)]
pub struct libusb_device {
    _empty: [u8; 0],
}

#[repr(C)]
pub struct libusb_device_handle {
    _empty: [u8; 0],
}

#[repr(C)]
#[non_exhaustive]
pub enum libusb_log_level {
    LIBUSB_LOG_LEVEL_NONE = 0,
    LIBUSB_LOG_LEVEL_ERROR = 1,
    LIBUSB_LOG_LEVEL_WARNING = 2,
    LIBUSB_LOG_LEVEL_INFO = 3,
    LIBUSB_LOG_LEVEL_DEBUG = 4,
}

#[repr(C)]
#[non_exhaustive]
pub enum libusb_option {
    LIBUSB_OPTION_LOG_LEVEL = 0,
    LIBUSB_OPTION_USE_USBDK = 1,
    LIBUSB_OPTION_NO_DEVICE_DISCOVERY = 2,
}
