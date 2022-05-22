use std::{
    error,
    ffi::CStr,
    fmt::{self, Display, Formatter},
    os::raw::c_int,
};

use super::{ffi::*, Error, ErrorInner, Result};

pub fn check(code: c_int) -> Result<()> {
    match code {
        0 => Ok(()),
        _ => Err(Error(ErrorInner::ReturnCode(code))),
    }
}

pub fn check_len(len: c_int) -> Result<usize> {
    if len >= 0 {
        Ok(len.try_into().unwrap())
    } else {
        Err(Error(ErrorInner::ReturnCode(len)))
    }
}

impl Display for Error {
    fn fmt(&self, formatter: &mut Formatter) -> fmt::Result {
        let message = match self.0 {
            ErrorInner::ReturnCode(code) => unsafe {
                CStr::from_ptr(libusb_strerror(code)).to_str().unwrap()
            },

            ErrorInner::TransferStatus(status) => match status {
                LIBUSB_TRANSFER_COMPLETED => unreachable!(),
                LIBUSB_TRANSFER_ERROR => "Transfer error",
                LIBUSB_TRANSFER_TIMED_OUT => "Transfer timed out",
                LIBUSB_TRANSFER_CANCELED => "Transfer canceled",
                LIBUSB_TRANSFER_STALL => "Endpoint stalled during transfer",
                LIBUSB_TRANSFER_NO_DEVICE => "Device was disconnected",
                LIBUSB_TRANSFER_OVERFLOW => "Transfer overflowed",
                _ => "Unknown transfer error",
            },
        };

        write!(formatter, "{}", message)
    }
}

impl error::Error for Error {}
