package org.schism.usb

import kotlinx.coroutines.flow.StateFlow

interface UsbBackend {
    val attachedDevices: StateFlow<List<UsbDevice>>
}
