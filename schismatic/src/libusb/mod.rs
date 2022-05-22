#![allow(unused)]

use std::{
    marker::PhantomData,
    mem,
    mem::ManuallyDrop,
    num::NonZeroU8,
    os::raw::c_int,
    ptr::NonNull,
    result,
    sync::{atomic::AtomicBool, Arc},
    task::Waker,
    thread::JoinHandle,
};

use parking_lot::Mutex;

mod bulk_transfer;
mod context;
mod descriptors;
mod device;
mod device_handle;
mod endpoints;
mod error;
mod ffi;

use ffi::*;

pub type Result<T> = result::Result<T, Error>;

#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash)]
pub struct Error(ErrorInner);

#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash)]
pub enum ErrorInner {
    ReturnCode(c_int),
    TransferStatus(c_int),
}

#[derive(Debug)]
pub struct Context {
    inner: NonNull<libusb_context>,
    event_thread_should_exit: Arc<AtomicBool>,
    event_thread: ManuallyDrop<JoinHandle<()>>,
}

#[derive(Debug)]
pub struct Device {
    context: Arc<Context>,
    inner: NonNull<libusb_device>,
}

#[derive(Debug)]
pub struct DeviceHandle {
    context: Arc<Context>,
    inner: NonNull<libusb_device_handle>,
    claimed_interfaces: Mutex<[usize; 32 / mem::size_of::<usize>()]>,
}

#[derive(Debug)]
pub struct BulkTransferIn<'a> {
    node: Arc<BulkTransferNode>,
    destination: PhantomData<&'a mut [u8]>,
}

#[derive(Debug, Clone)]
pub struct BulkTransferOut<'a> {
    node: Arc<BulkTransferNode>,
    source: PhantomData<&'a [u8]>,
}

#[derive(Debug)]
struct BulkTransferNode {
    device_handle: Arc<DeviceHandle>,
    state: Mutex<BulkTransferState>,
}

#[derive(Debug)]
enum BulkTransferState {
    Initialized {
        buffer_start: *mut u8,
        buffer_len: c_int,
        endpoint: EndpointAddress,
    },
    Polled(Waker),
    Succeeded(usize),
    Failed(c_int),
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, PartialOrd, Ord, Hash)]
pub struct EndpointAddress(u8);

#[derive(Debug, Clone, Copy, PartialEq, Eq, PartialOrd, Ord, Hash)]
pub enum TransferDirection {
    Out,
    In,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, PartialOrd, Ord, Hash)]
pub enum TransferType {
    Control,
    Isochronous,
    Bulk,
    Interrupt,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, PartialOrd, Ord, Hash)]
pub struct StringDescriptorIndex(NonZeroU8);

#[derive(Debug, Clone, Copy, PartialEq, Eq, PartialOrd, Ord, Hash)]
pub struct ConfigurationAttributes(u8);

#[derive(Debug, Clone, Copy, PartialEq, Eq, PartialOrd, Ord, Hash)]
pub struct EndpointAttributes(u8);

#[derive(Debug, Clone, Copy, PartialEq, Eq, PartialOrd, Ord, Hash)]
pub struct MaxPower(u8);

#[derive(Debug, Clone, PartialEq, Eq, PartialOrd, Ord, Hash)]
pub struct DeviceDescriptor {
    pub bcd_usb: u16,
    pub device_class: u8,
    pub device_sub_class: u8,
    pub device_protocol: u8,
    pub max_packet_size: u8,
    pub vendor_id: u16,
    pub product_id: u16,
    pub bcd_device: u16,
    pub manufacturer: Option<StringDescriptorIndex>,
    pub product: Option<StringDescriptorIndex>,
    pub serial_number: Option<StringDescriptorIndex>,
    pub num_configurations: u8,
}

#[derive(Debug, Clone, PartialEq, Eq, PartialOrd, Ord, Hash)]
pub struct ConfigurationDescriptor {
    pub configuration_value: u8,
    pub configuration: Option<StringDescriptorIndex>,
    pub attributes: ConfigurationAttributes,
    pub max_power: MaxPower,
    pub interfaces: Vec<InterfaceDescriptor>,
}

#[derive(Debug, Clone, PartialEq, Eq, PartialOrd, Ord, Hash)]
pub struct InterfaceDescriptor {
    pub interface_number: u8,
    pub alternate_setting: u8,
    pub interface_class: u8,
    pub interface_sub_class: u8,
    pub interface_protocol: u8,
    pub interface: Option<StringDescriptorIndex>,
    pub endpoints: Vec<EndpointDescriptor>,
}

#[derive(Debug, Clone, PartialEq, Eq, PartialOrd, Ord, Hash)]
pub struct EndpointDescriptor {
    pub endpoint_address: EndpointAddress,
    pub attributes: EndpointAttributes,
    pub max_packet_size: u16,
    pub interval: u8,
}
