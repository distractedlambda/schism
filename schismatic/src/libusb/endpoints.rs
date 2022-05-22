use std::hint::unreachable_unchecked;

use super::{EndpointAddress, EndpointAttributes, TransferDirection, TransferType};

impl EndpointAddress {
    pub fn number(self) -> u8 {
        self.0 & 0x7F
    }

    pub fn direction(self) -> TransferDirection {
        match self.0 >> 7 {
            0 => TransferDirection::Out,
            1 => TransferDirection::In,
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

impl EndpointAttributes {
    pub fn transfer_type(self) -> TransferType {
        match self.0 & 0b11 {
            0 => TransferType::Control,
            1 => TransferType::Isochronous,
            2 => TransferType::Bulk,
            3 => TransferType::Interrupt,
            _ => unsafe { unreachable_unchecked() },
        }
    }
}
