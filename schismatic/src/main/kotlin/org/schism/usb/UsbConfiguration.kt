package org.schism.usb

interface UsbConfiguration {
    val device: UsbDevice

    val interfaces: List<UsbInterface>
}

val UsbConfiguration.backend: UsbBackend
    get() = device.backend
