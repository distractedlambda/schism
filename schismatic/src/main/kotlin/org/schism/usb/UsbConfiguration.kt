package org.schism.usb

interface UsbConfiguration {
    val device: UsbDevice

    val interfaces: List<UsbInterface>
}

val UsbConfiguration.backend: UsbBackend
    get() = device.backend

context (UsbDeviceConnection) suspend inline fun UsbConfiguration.isActive(): Boolean {
    return device.getActiveConfiguration() === this@UsbConfiguration
}
