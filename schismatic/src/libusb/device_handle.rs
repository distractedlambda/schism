use std::{mem::MaybeUninit, num::NonZeroU8, os::raw::c_int, ptr::NonNull, sync::Arc};

use super::{error, ffi::*, Context, Device, DeviceHandle, EndpointAddress, Result};

impl DeviceHandle {
    pub fn context(&self) -> &Arc<Context> {
        &self.context
    }

    pub fn device(&self) -> Device {
        Device {
            context: self.context.clone(),
            inner: unsafe { NonNull::new_unchecked(libusb_get_device(self.inner.as_ptr())) },
        }
    }

    pub fn get_configuration(&self) -> Result<Option<NonZeroU8>> {
        let mut configuration = MaybeUninit::uninit();
        unsafe {
            error::check(libusb_get_configuration(
                self.inner.as_ptr(),
                &mut configuration,
            ))?;
            Ok(NonZeroU8::new(configuration.assume_init() as u8))
        }
    }

    pub fn set_configuration(&self, configuration: Option<u8>) -> Result<()> {
        unsafe {
            error::check(libusb_set_configuration(
                self.inner.as_ptr(),
                configuration.map(|it| it as c_int).unwrap_or(-1),
            ))
        }
    }

    pub fn claim_interface(&self, interface_number: u8) -> Result<()> {
        let mut claimed_interfaces = self.claimed_interfaces.lock();

        error::check(unsafe {
            libusb_claim_interface(self.inner.as_ptr(), interface_number as c_int)
        })?;

        claimed_interfaces[interface_number as usize / usize::BITS as usize] |=
            1 << (interface_number as usize % usize::BITS as usize);

        Ok(())
    }

    pub fn release_interface(&self, interface_number: u8) -> Result<()> {
        let mut claimed_interfaces = self.claimed_interfaces.lock();

        error::check(unsafe {
            libusb_release_interface(self.inner.as_ptr(), interface_number as c_int)
        })?;

        claimed_interfaces[interface_number as usize / usize::BITS as usize] &=
            !(1 << (interface_number as usize % usize::BITS as usize));

        Ok(())
    }

    pub fn set_alternate_setting(&self, interface_number: u8, alternate_setting: u8) -> Result<()> {
        error::check(unsafe {
            libusb_set_interface_alt_setting(
                self.inner.as_ptr(),
                interface_number as c_int,
                alternate_setting as c_int,
            )
        })
    }

    pub fn clear_halt(&self, endpoint: EndpointAddress) -> Result<()> {
        error::check(unsafe { libusb_clear_halt(self.inner.as_ptr(), endpoint.into()) })
    }

    pub fn reset_device(&self) -> Result<()> {
        error::check(unsafe { libusb_reset_device(self.inner.as_ptr()) })
    }
}

unsafe impl Send for DeviceHandle {}

unsafe impl Sync for DeviceHandle {}

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
                            libusb_release_interface(self.inner.as_ptr(), interface_number as c_int)
                        };

                        remainder ^= 1 << bit_index;
                    }
                }
            }
        }

        unsafe { libusb_close(self.inner.as_ptr()) }
    }
}
