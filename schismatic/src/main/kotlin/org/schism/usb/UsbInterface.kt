package org.schism.usb

interface UsbInterface {
    val configuration: UsbConfiguration

    val alternateSettings: List<UsbAlternateSetting>
}

val UsbInterface.device: UsbDevice
    get() = configuration.device

val UsbInterface.backend: UsbBackend
    get() = configuration.backend
