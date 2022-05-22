use std::{mem::MaybeUninit, num::NonZeroU8, slice};

use super::{
    error, ffi::*, ConfigurationAttributes, ConfigurationDescriptor, Device, DeviceDescriptor,
    EndpointAttributes, EndpointDescriptor, InterfaceDescriptor, MaxPower, Result,
    StringDescriptorIndex,
};

impl Device {
    pub fn get_descriptor(&self) -> Result<DeviceDescriptor> {
        let mut descriptor = MaybeUninit::uninit();
        unsafe {
            error::check(libusb_get_device_descriptor(
                self.inner.as_ptr(),
                &mut descriptor,
            ))?;

            Ok(DeviceDescriptor::from_ffi(descriptor.assume_init_ref()))
        }
    }

    pub fn get_active_configuration_descriptor(&self) -> Result<ConfigurationDescriptor> {
        let mut descriptor = MaybeUninit::uninit();
        unsafe {
            // FIXME: free descriptor in case of panic
            error::check(libusb_get_active_config_descriptor(
                self.inner.as_ptr(),
                &mut descriptor,
            ))?;
            let descriptor = descriptor.assume_init();
            let translated = ConfigurationDescriptor::from_ffi(&*descriptor);
            libusb_free_config_descriptor(descriptor);
            Ok(translated)
        }
    }
}

impl DeviceDescriptor {
    fn from_ffi(descriptor: &libusb_device_descriptor) -> Self {
        Self {
            bcd_usb: descriptor.bcdUSB,
            device_class: descriptor.bDeviceClass,
            device_sub_class: descriptor.bDeviceSubClass,
            device_protocol: descriptor.bDeviceProtocol,
            max_packet_size: descriptor.bMaxPacketSize0,
            vendor_id: descriptor.idVendor,
            product_id: descriptor.idProduct,
            bcd_device: descriptor.bcdDevice,
            manufacturer: StringDescriptorIndex::from_ffi(descriptor.iManufacturer),
            product: StringDescriptorIndex::from_ffi(descriptor.iProduct),
            serial_number: StringDescriptorIndex::from_ffi(descriptor.iSerialNumber),
            num_configurations: descriptor.bNumConfigurations,
        }
    }
}

impl ConfigurationDescriptor {
    fn from_ffi(descriptor: &libusb_config_descriptor) -> Self {
        Self {
            configuration_value: descriptor.bConfigurationValue,
            configuration: StringDescriptorIndex::from_ffi(descriptor.iConfiguration),
            attributes: ConfigurationAttributes(descriptor.bmAttributes),
            max_power: MaxPower(descriptor.MaxPower),
            interfaces: unsafe {
                slice::from_raw_parts(
                    descriptor.interface,
                    descriptor.bNumInterfaces.try_into().unwrap(),
                )
                .into_iter()
                .flat_map(|interface| {
                    slice::from_raw_parts(
                        interface.altsetting,
                        interface.num_altsetting.try_into().unwrap(),
                    )
                    .into_iter()
                    .map(InterfaceDescriptor::from_ffi)
                })
                .collect()
            },
        }
    }
}

impl InterfaceDescriptor {
    fn from_ffi(descriptor: &libusb_interface_descriptor) -> Self {
        Self {
            interface_number: descriptor.bInterfaceNumber,
            alternate_setting: descriptor.bAlternateSetting,
            interface_class: descriptor.bInterfaceClass,
            interface_sub_class: descriptor.bInterfaceSubClass,
            interface_protocol: descriptor.bInterfaceProtocol,
            interface: StringDescriptorIndex::from_ffi(descriptor.iInterface),
            endpoints: unsafe {
                slice::from_raw_parts(
                    descriptor.endpoint,
                    descriptor.bNumEndpoints.try_into().unwrap(),
                )
                .into_iter()
                .map(EndpointDescriptor::from_ffi)
                .collect()
            },
        }
    }
}

impl EndpointDescriptor {
    fn from_ffi(descriptor: &libusb_endpoint_descriptor) -> Self {
        Self {
            endpoint_address: descriptor.bEndpointAddress.into(),
            attributes: EndpointAttributes(descriptor.bmAttributes),
            max_packet_size: descriptor.wMaxPacketSize,
            interval: descriptor.bInterval,
        }
    }
}

impl StringDescriptorIndex {
    fn from_ffi(index: u8) -> Option<Self> {
        NonZeroU8::new(index).map(|it| Self(it))
    }
}
