use std::{
    mem::{self, MaybeUninit},
    ptr::NonNull,
    sync::Arc,
};

use super::{error, ffi::*, Context, Device, DeviceHandle, EndpointAddress, Result};

use parking_lot::Mutex;
use staticvec::StaticVec;

impl Device {
    pub fn open(&self) -> Result<DeviceHandle> {
        let mut device_handle = MaybeUninit::uninit();
        unsafe {
            error::check(libusb_open(self.inner.as_ptr(), &mut device_handle))?;

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
        unsafe { libusb_get_bus_number(self.inner.as_ptr()) }
    }

    pub fn port_number(&self) -> u8 {
        unsafe { libusb_get_port_number(self.inner.as_ptr()) }
    }

    pub fn port_numbers(&self) -> Result<StaticVec<u8, 7>> {
        let mut numbers = MaybeUninit::uninit_array::<7>();
        unsafe {
            let actual_len = error::check_len(libusb_get_port_numbers(
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
        unsafe { libusb_get_device_address(self.inner.as_ptr()) }
    }

    pub fn max_packet_size(&self, endpoint: EndpointAddress) -> Result<usize> {
        Ok(error::check_len(unsafe {
            libusb_get_max_packet_size(self.inner.as_ptr(), endpoint.into())
        })?
        .try_into()
        .unwrap())
    }

    pub fn max_iso_packet_size(&self, endpoint: EndpointAddress) -> Result<usize> {
        Ok(error::check_len(unsafe {
            libusb_get_max_iso_packet_size(self.inner.as_ptr(), endpoint.into())
        })?
        .try_into()
        .unwrap())
    }
}

impl Clone for Device {
    fn clone(&self) -> Self {
        unsafe { libusb_ref_device(self.inner.as_ptr()) };
        Self {
            context: self.context.clone(),
            inner: self.inner,
        }
    }
}

impl Drop for Device {
    fn drop(&mut self) {
        unsafe { libusb_unref_device(self.inner.as_ptr()) }
    }
}

unsafe impl Send for Device {}

unsafe impl Sync for Device {}
