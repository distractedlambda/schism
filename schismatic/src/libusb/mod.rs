use std::{
    error,
    ffi::{c_void, CStr},
    fmt::{self, Display, Formatter},
    future::Future,
    hint::unreachable_unchecked,
    marker::PhantomData,
    mem::{self, ManuallyDrop, MaybeUninit},
    num::NonZeroU8,
    ops::DerefMut,
    os::raw::c_int,
    pin::Pin,
    ptr::NonNull,
    result,
    sync::{
        atomic::{self, AtomicBool},
        Arc,
    },
    task::Waker,
    task::{self, Poll},
    thread::{self, JoinHandle},
};

use staticvec::StaticVec;

use parking_lot::Mutex;

mod ffi;

#[derive(Debug)]
pub struct Context {
    inner: NonNull<ffi::libusb_context>,
    event_thread_should_exit: Arc<AtomicBool>,
    event_thread: ManuallyDrop<JoinHandle<()>>,
}

impl Context {
    pub fn new() -> Result<Arc<Self>> {
        let mut context = MaybeUninit::uninit();

        let inner = unsafe {
            check_error(ffi::libusb_init(Some(&mut context)))?;
            NonNull::new_unchecked(context.assume_init())
        };

        let event_thread_should_exit = Arc::new(AtomicBool::new(false));

        let event_thread = ManuallyDrop::new(thread::spawn({
            let should_exit = event_thread_should_exit.clone();
            let context = inner.as_ptr() as usize; // FIXME: wrap with Send somehow
            move || {
                while !should_exit.load(atomic::Ordering::Relaxed) {
                    unsafe {
                        // FIXME: handle errors here somehow
                        let _ = ffi::libusb_handle_events(context as *mut ffi::libusb_context);
                    }
                }
            }
        }));

        Ok(Arc::new(Self {
            inner,
            event_thread_should_exit,
            event_thread,
        }))
    }
}

impl Drop for Context {
    fn drop(&mut self) {
        self.event_thread_should_exit
            .store(true, atomic::Ordering::Relaxed);

        unsafe {
            ffi::libusb_interrupt_event_handler(self.inner.as_ptr());
            let _ = ManuallyDrop::take(&mut self.event_thread).join();
            ffi::libusb_exit(self.inner.as_ptr());
        }
    }
}

unsafe impl Send for Context {}

unsafe impl Sync for Context {}

#[derive(Debug)]
pub struct Device {
    context: Arc<Context>,
    inner: NonNull<ffi::libusb_device>,
}

impl Device {
    pub fn open(&self) -> Result<DeviceHandle> {
        let mut device_handle = MaybeUninit::uninit();
        unsafe {
            check_error(ffi::libusb_open(self.inner.as_ptr(), &mut device_handle))?;

            Ok(DeviceHandle {
                context: self.context.clone(),
                inner: NonNull::new_unchecked(device_handle.assume_init()),
                claimed_interfaces: Mutex::new([0; 32 / mem::size_of::<usize>()]),
            })
        }
    }

    pub fn context(&self) -> &Arc<Context> {
        &self.context
    }

    pub fn bus_number(&self) -> u8 {
        unsafe { ffi::libusb_get_bus_number(self.inner.as_ptr()) }
    }

    pub fn port_number(&self) -> u8 {
        unsafe { ffi::libusb_get_port_number(self.inner.as_ptr()) }
    }

    pub fn port_numbers(&self) -> Result<StaticVec<u8, 7>> {
        let mut numbers = MaybeUninit::uninit_array::<7>();
        unsafe {
            let actual_len = check_len_error(ffi::libusb_get_port_numbers(
                self.inner.as_ptr(),
                numbers.as_mut_ptr() as *mut u8,
                numbers.len().try_into().unwrap(),
            ))?;

            Ok(StaticVec::new_from_slice(
                MaybeUninit::slice_assume_init_ref(&numbers[..actual_len.try_into().unwrap()]),
            ))
        }
    }

    pub fn address(&self) -> u8 {
        unsafe { ffi::libusb_get_device_address(self.inner.as_ptr()) }
    }

    pub fn max_packet_size(&self, endpoint: EndpointAddress) -> Result<usize> {
        Ok(check_len_error(unsafe {
            ffi::libusb_get_max_packet_size(self.inner.as_ptr(), endpoint.into())
        })?
        .try_into()
        .unwrap())
    }

    pub fn max_iso_packet_size(&self, endpoint: EndpointAddress) -> Result<usize> {
        Ok(check_len_error(unsafe {
            ffi::libusb_get_max_iso_packet_size(self.inner.as_ptr(), endpoint.into())
        })?
        .try_into()
        .unwrap())
    }
}

impl Clone for Device {
    fn clone(&self) -> Self {
        unsafe { ffi::libusb_ref_device(self.inner.as_ptr()) };
        Self {
            context: self.context.clone(),
            inner: self.inner,
        }
    }
}

impl Drop for Device {
    fn drop(&mut self) {
        unsafe { ffi::libusb_unref_device(self.inner.as_ptr()) }
    }
}

#[derive(Debug)]
pub struct DeviceHandle {
    context: Arc<Context>,
    inner: NonNull<ffi::libusb_device_handle>,
    claimed_interfaces: Mutex<[usize; 32 / mem::size_of::<usize>()]>,
}

impl DeviceHandle {
    pub fn context(&self) -> &Arc<Context> {
        &self.context
    }

    pub fn device(&self) -> Device {
        Device {
            context: self.context.clone(),
            inner: unsafe { NonNull::new_unchecked(ffi::libusb_get_device(self.inner.as_ptr())) },
        }
    }

    pub fn get_configuration(&self) -> Result<Option<NonZeroU8>> {
        let mut configuration = MaybeUninit::uninit();
        unsafe {
            check_error(ffi::libusb_get_configuration(
                self.inner.as_ptr(),
                &mut configuration,
            ))?;
            Ok(NonZeroU8::new(configuration.assume_init() as u8))
        }
    }

    pub fn set_configuration(&self, configuration: Option<u8>) -> Result<()> {
        unsafe {
            check_error(ffi::libusb_set_configuration(
                self.inner.as_ptr(),
                configuration.map(|it| it as c_int).unwrap_or(-1),
            ))
        }
    }

    pub fn claim_interface(&self, interface_number: u8) -> Result<()> {
        let mut claimed_interfaces = self.claimed_interfaces.lock();

        check_error(unsafe {
            ffi::libusb_claim_interface(self.inner.as_ptr(), interface_number as c_int)
        })?;

        claimed_interfaces[interface_number as usize / usize::BITS as usize] |=
            1 << (interface_number as usize % usize::BITS as usize);

        Ok(())
    }

    pub fn release_interface(&self, interface_number: u8) -> Result<()> {
        let mut claimed_interfaces = self.claimed_interfaces.lock();

        check_error(unsafe {
            ffi::libusb_release_interface(self.inner.as_ptr(), interface_number as c_int)
        })?;

        claimed_interfaces[interface_number as usize / usize::BITS as usize] &=
            !(1 << (interface_number as usize % usize::BITS as usize));

        Ok(())
    }

    pub fn set_alternate_setting(&self, interface_number: u8, alternate_setting: u8) -> Result<()> {
        check_error(unsafe {
            ffi::libusb_set_interface_alt_setting(
                self.inner.as_ptr(),
                interface_number as c_int,
                alternate_setting as c_int,
            )
        })
    }

    pub fn clear_halt(&self, endpoint: EndpointAddress) -> Result<()> {
        check_error(unsafe { ffi::libusb_clear_halt(self.inner.as_ptr(), endpoint.into()) })
    }

    pub fn reset_device(&self) -> Result<()> {
        check_error(unsafe { ffi::libusb_reset_device(self.inner.as_ptr()) })
    }

    pub fn bulkTransferIn(
        self: &Arc<Self>,
        destination: &mut [u8],
        endpoint: EndpointAddress,
    ) -> BulkTransferIn {
        assert_eq!(endpoint.direction(), EndpointDirection::In);

        BulkTransferIn {
            node: Arc::new(BulkTransferNode {
                device_handle: self.clone(),
                state: Mutex::new(BulkTransferState::Initialized {
                    buffer_start: destination.as_mut_ptr(),
                    buffer_len: destination.len().try_into().unwrap(),
                    endpoint,
                }),
            }),
            destination: PhantomData,
        }
    }

    pub fn bulkTransferOut(
        self: &Arc<Self>,
        source: &[u8],
        endpoint: EndpointAddress,
    ) -> BulkTransferOut {
        assert_eq!(endpoint.direction(), EndpointDirection::Out);

        BulkTransferOut {
            node: Arc::new(BulkTransferNode {
                device_handle: self.clone(),
                state: Mutex::new(BulkTransferState::Initialized {
                    buffer_start: source.as_ptr() as *mut _,
                    buffer_len: source.len().try_into().unwrap(),
                    endpoint,
                }),
            }),
            source: PhantomData,
        }
    }
}

#[derive(Debug)]
pub struct BulkTransferIn<'a> {
    node: Arc<BulkTransferNode>,
    destination: PhantomData<&'a mut [u8]>,
}

impl<'a> Future for BulkTransferIn<'a> {
    type Output = Result<usize>;

    fn poll(self: Pin<&mut Self>, context: &mut task::Context) -> Poll<Self::Output> {
        unsafe { self.node.poll(context) }
    }
}

unsafe extern "C" fn transfer_callback(transfer: &mut ffi::libusb_transfer) {
    let new_state = if transfer.status == ffi::LIBUSB_TRANSFER_COMPLETED {
        BulkTransferState::Succeeded(transfer.actual_length.try_into().unwrap())
    } else {
        BulkTransferState::Failed(transfer.status)
    };

    ffi::libusb_free_transfer(transfer);

    let node = Arc::from_raw(transfer.user_data as *const BulkTransferNode);
    let mut state = node.state.lock();
    match mem::replace(state.deref_mut(), new_state) {
        // need to wake under lock to avoid a missed wake
        BulkTransferState::Polled(waker) => waker.wake(),
        _ => unreachable!("illegal transfer state"),
    }
}

#[derive(Debug, Clone)]
pub struct BulkTransferOut<'a> {
    node: Arc<BulkTransferNode>,
    source: PhantomData<&'a [u8]>,
}

impl<'a> Future for BulkTransferOut<'a> {
    type Output = Result<usize>;

    fn poll(self: Pin<&mut Self>, context: &mut task::Context) -> Poll<Self::Output> {
        unsafe { self.node.poll(context) }
    }
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

impl BulkTransferNode {
    unsafe fn poll(self: &Arc<Self>, context: &mut task::Context) -> Poll<Result<usize>> {
        let mut state = self.state.lock();
        match state.deref_mut() {
            &mut BulkTransferState::Initialized {
                buffer_start,
                buffer_len,
                endpoint,
            } => {
                let transfer = ffi::libusb_alloc_transfer(0);
                (*transfer).dev_handle = self.device_handle.inner.as_ptr();
                (*transfer).endpoint = endpoint.into();
                (*transfer).r#type = ffi::LIBUSB_TRANSFER_TYPE_BULK;
                (*transfer).timeout = 0;
                (*transfer).length = buffer_len;
                (*transfer).callback = transfer_callback;
                (*transfer).user_data = Arc::into_raw(self.clone()) as *mut c_void;
                (*transfer).buffer = buffer_start;
                check_error(ffi::libusb_submit_transfer(transfer)).map_err(|it| {
                    ffi::libusb_free_transfer(transfer);
                    it
                })?;
                *state = BulkTransferState::Polled(context.waker().clone());
                Poll::Pending
            }

            &mut BulkTransferState::Polled(ref mut waker) => {
                *waker = context.waker().clone();
                Poll::Pending
            }

            &mut BulkTransferState::Succeeded(len) => Poll::Ready(Ok(len)),

            &mut BulkTransferState::Failed(transfer_status) => {
                Poll::Ready(Err(Error(ErrorInner::TransferStatus(transfer_status))))
            }
        }
    }
}

impl Drop for DeviceHandle {
    fn drop(&mut self) {
        for (word_index, &word) in self
            .claimed_interfaces
            .get_mut() // FIXME: handle poisoning?
            .iter()
            .enumerate()
        {
            let mut remainder = word;
            loop {
                match remainder.trailing_zeros() {
                    usize::BITS => break,
                    bit_index => {
                        let interface_number = (word_index as u32) * usize::BITS + bit_index;

                        let _ = unsafe {
                            ffi::libusb_release_interface(
                                self.inner.as_ptr(),
                                interface_number as c_int,
                            )
                        };

                        remainder ^= 1 << bit_index;
                    }
                }
            }
        }

        unsafe { ffi::libusb_close(self.inner.as_ptr()) }
    }
}

unsafe impl Send for DeviceHandle {}

unsafe impl Sync for DeviceHandle {}

#[derive(Debug, Clone, Copy, PartialEq, Eq, PartialOrd, Ord, Hash)]
pub struct EndpointAddress(u8);

impl EndpointAddress {
    pub fn number(self) -> u8 {
        self.0 & 0x7F
    }

    pub fn direction(self) -> EndpointDirection {
        match self.0 >> 7 {
            0 => EndpointDirection::Out,
            1 => EndpointDirection::In,
            _ => unsafe { unreachable_unchecked() },
        }
    }
}

impl From<u8> for EndpointAddress {
    fn from(other: u8) -> Self {
        Self(other)
    }
}

impl Into<u8> for EndpointAddress {
    fn into(self) -> u8 {
        self.0
    }
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, PartialOrd, Ord, Hash)]
pub enum EndpointDirection {
    Out,
    In,
}

unsafe impl Send for Device {}

unsafe impl Sync for Device {}

fn check_error(code: c_int) -> Result<()> {
    match code {
        0 => Ok(()),
        _ => Err(Error(ErrorInner::ReturnCode(code))),
    }
}

fn check_len_error(len: c_int) -> Result<usize> {
    if len >= 0 {
        Ok(len.try_into().unwrap())
    } else {
        Err(Error(ErrorInner::ReturnCode(len)))
    }
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash)]
pub struct Error(ErrorInner);

#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash)]
enum ErrorInner {
    ReturnCode(c_int),
    TransferStatus(c_int),
}

pub type Result<T> = result::Result<T, Error>;

impl Display for Error {
    fn fmt(&self, formatter: &mut Formatter) -> fmt::Result {
        let message = match self.0 {
            ErrorInner::ReturnCode(code) => unsafe {
                CStr::from_ptr(ffi::libusb_strerror(code)).to_str().unwrap()
            },

            ErrorInner::TransferStatus(status) => match status {
                ffi::LIBUSB_TRANSFER_COMPLETED => unreachable!(),
                ffi::LIBUSB_TRANSFER_ERROR => "Transfer error",
                ffi::LIBUSB_TRANSFER_TIMED_OUT => "Transfer timed out",
                ffi::LIBUSB_TRANSFER_CANCELED => "Transfer canceled",
                ffi::LIBUSB_TRANSFER_STALL => "Endpoint stalled during transfer",
                ffi::LIBUSB_TRANSFER_NO_DEVICE => "Device was disconnected",
                ffi::LIBUSB_TRANSFER_OVERFLOW => "Transfer overflowed",
                _ => "Unknown transfer error",
            },
        };

        write!(formatter, "{}", message)
    }
}

impl error::Error for Error {}
