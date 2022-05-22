use std::{
    mem::{ManuallyDrop, MaybeUninit},
    ptr::NonNull,
    sync::{
        atomic::{self, AtomicBool},
        Arc,
    },
    thread,
};

use super::{error, ffi::*, Context, Result};

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
