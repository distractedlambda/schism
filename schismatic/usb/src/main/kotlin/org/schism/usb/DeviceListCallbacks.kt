package org.schism.usb

import org.schism.concurrent.Observable

public fun attachedUsbDevices(): Observable<List<UsbDevice>> =
    UsbContext.attachedDevices
