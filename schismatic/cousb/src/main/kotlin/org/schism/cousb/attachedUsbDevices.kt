package org.schism.cousb

import kotlinx.coroutines.flow.StateFlow

public fun attachedUsbDevices(): StateFlow<List<UsbDevice>> =
    UsbContext.attachedDevices
