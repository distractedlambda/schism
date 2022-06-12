package org.schism.usb

import org.schism.util.SuspendingAutocloseable

interface UsbDeviceConnection : SuspendingAutocloseable {
    val device: UsbDevice
}
