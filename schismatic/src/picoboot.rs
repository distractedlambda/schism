use crate::libusb;

const VENDOR_ID: u16 = 0x2e8a;
const PRODUCT_ID: u16 = 0x0003;
const INTERFACE_CLASS: u8 = 0xff;
const INTERFACE_SUB_CLASS: u8 = 0x00;
const INTERFACE_PROTOCOL: u8 = 0x00;

#[derive(Debug, Clone, Copy, PartialEq, Eq, PartialOrd, Ord, Hash)]
pub struct PicobootInterface {
    pub interface_number: u8,
    pub alternate_setting: u8,
    pub in_endpoint: libusb::EndpointAddress,
    pub out_endpoint: libusb::EndpointAddress,
}

pub fn device_descriptor_matches(device_descriptor: &libusb::DeviceDescriptor) -> bool {
    device_descriptor.vendor_id == VENDOR_ID && device_descriptor.product_id == PRODUCT_ID
}

pub fn find_interface(
    configuration: &libusb::ConfigurationDescriptor,
) -> Option<PicobootInterface> {
    for interface in &configuration.interfaces {
        if interface.interface_class != INTERFACE_CLASS {
            continue;
        }

        if interface.interface_sub_class != INTERFACE_SUB_CLASS {
            continue;
        }

        if interface.interface_protocol != INTERFACE_PROTOCOL {
            continue;
        }

        let mut in_endpoint = None;
        let mut out_endpoint = None;

        for endpoint in &interface.endpoints {
            if endpoint.attributes.transfer_type() != libusb::TransferType::Bulk {
                continue;
            }

            match endpoint.endpoint_address.direction() {
                libusb::TransferDirection::In => in_endpoint = Some(endpoint.endpoint_address),
                libusb::TransferDirection::Out => out_endpoint = Some(endpoint.endpoint_address),
            }
        }

        if let (Some(in_endpoint), Some(out_endpoint)) = (in_endpoint, out_endpoint) {
            return Some(PicobootInterface {
                interface_number: interface.interface_number,
                alternate_setting: interface.alternate_setting,
                in_endpoint,
                out_endpoint,
            });
        }
    }

    None
}
