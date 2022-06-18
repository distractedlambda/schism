package org.schism.usb

import kotlinx.collections.immutable.PersistentSet
import kotlinx.coroutines.flow.StateFlow

interface UsbBackend {
    val attachedDevices: StateFlow<PersistentSet<UsbDevice>>
}
