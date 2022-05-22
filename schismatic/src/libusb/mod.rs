#![allow(unused)]

use std::{
    marker::PhantomData,
    mem,
    mem::ManuallyDrop,
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
mod device;
mod device_handle;
mod endpoint_address;
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
pub enum EndpointDirection {
    Out,
    In,
}
