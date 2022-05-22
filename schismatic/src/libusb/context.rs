use std::{
    mem::{ManuallyDrop, MaybeUninit},
    ptr::NonNull,
    slice,
    sync::{
        atomic::{self, AtomicBool},
        Arc,
    },
    thread,
};

use super::{error, ffi::*, Context, Device, Error, ErrorInner, Result};

impl Context {
    pub fn new() -> Result<Arc<Self>> {
        let mut context = MaybeUninit::uninit();

        let inner = unsafe {
            error::check(libusb_init(Some(&mut context)))?;
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
                        let _ = libusb_handle_events(context as *mut libusb_context);
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

    pub fn get_devices<B: FromIterator<Device>>(self: &Arc<Self>) -> Result<B> {
        let mut list = MaybeUninit::uninit();
        unsafe {
            let len_or_err = libusb_get_device_list(self.inner.as_ptr(), &mut list);

            if len_or_err < 0 {
                return Err(Error(ErrorInner::ReturnCode(
                    len_or_err.try_into().unwrap(),
                )));
            }

            let list = list.assume_init();

            let collected = slice::from_raw_parts(list, len_or_err as usize)
                .into_iter()
                .map(|&it| Device {
                    context: self.clone(),
                    inner: NonNull::new_unchecked(it),
                })
                .collect();

            libusb_free_device_list(list, 0);

            Ok(collected)
        }
    }
}

impl Drop for Context {
    fn drop(&mut self) {
        self.event_thread_should_exit
            .store(true, atomic::Ordering::Relaxed);

        unsafe {
            libusb_interrupt_event_handler(self.inner.as_ptr());
            let _ = ManuallyDrop::take(&mut self.event_thread).join();
            libusb_exit(self.inner.as_ptr());
        }
    }
}

unsafe impl Send for Context {}

unsafe impl Sync for Context {}
