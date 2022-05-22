use std::hint::unreachable_unchecked;

use super::{EndpointAddress, EndpointDirection};

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
