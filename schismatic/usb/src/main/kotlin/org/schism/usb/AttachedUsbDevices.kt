package org.schism.usb

import org.schism.concurrent.BlockingStateFlow

public fun attachedUsbDevices(): BlockingStateFlow<List<UsbDevice>> =
    UsbContext.attachedDevices
