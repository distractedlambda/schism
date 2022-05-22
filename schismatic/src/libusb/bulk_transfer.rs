use std::{
    future::Future,
    marker::PhantomData,
    mem,
    ops::DerefMut,
    os::raw::c_void,
    pin::Pin,
    sync::Arc,
    task::{self, Poll},
};

use super::{
    error, ffi::*, BulkTransferIn, BulkTransferNode, BulkTransferOut, BulkTransferState,
    DeviceHandle, EndpointAddress, Error, ErrorInner, Result, TransferDirection,
};

use parking_lot::Mutex;

impl DeviceHandle {
    pub fn bulk_transfer_in(
        self: &Arc<Self>,
        destination: &mut [u8],
        endpoint: EndpointAddress,
    ) -> BulkTransferIn {
        assert_eq!(endpoint.direction(), TransferDirection::In);

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

    pub fn bulk_transfer_out(
        self: &Arc<Self>,
        source: &[u8],
        endpoint: EndpointAddress,
    ) -> BulkTransferOut {
        assert_eq!(endpoint.direction(), TransferDirection::Out);

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

impl<'a> Future for BulkTransferIn<'a> {
    type Output = Result<usize>;

    fn poll(self: Pin<&mut Self>, context: &mut task::Context) -> Poll<Self::Output> {
        unsafe { self.node.poll(context) }
    }
}

impl<'a> Future for BulkTransferOut<'a> {
    type Output = Result<usize>;

    fn poll(self: Pin<&mut Self>, context: &mut task::Context) -> Poll<Self::Output> {
        unsafe { self.node.poll(context) }
    }
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
                let transfer = libusb_alloc_transfer(0);
                (*transfer).dev_handle = self.device_handle.inner.as_ptr();
                (*transfer).endpoint = endpoint.into();
                (*transfer).r#type = LIBUSB_TRANSFER_TYPE_BULK;
                (*transfer).timeout = 0;
                (*transfer).length = buffer_len;
                (*transfer).callback = transfer_callback;
                (*transfer).user_data = Arc::into_raw(self.clone()) as *mut c_void;
                (*transfer).buffer = buffer_start;
                error::check(libusb_submit_transfer(transfer)).map_err(|it| {
                    libusb_free_transfer(transfer);
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

unsafe extern "C" fn transfer_callback(transfer: &mut libusb_transfer) {
    let new_state = if transfer.status == LIBUSB_TRANSFER_COMPLETED {
        BulkTransferState::Succeeded(transfer.actual_length.try_into().unwrap())
    } else {
        BulkTransferState::Failed(transfer.status)
    };

    libusb_free_transfer(transfer);

    let node = Arc::from_raw(transfer.user_data as *const BulkTransferNode);
    let mut state = node.state.lock();
    match mem::replace(state.deref_mut(), new_state) {
        // need to wake under lock to avoid a missed wake
        BulkTransferState::Polled(waker) => waker.wake(),
        _ => unreachable!("illegal transfer state"),
    }
}
