package org.schism.usb

interface UsbAlternateSetting {
    val `interface`: UsbInterface

    val interfaceClass: UByte

    val interfaceSubClass: UByte

    val interfaceProtocol: UByte

    val endpoints: List<UsbEndpoint>
}

val UsbAlternateSetting.configuration: UsbConfiguration
    get() = `interface`.configuration

val UsbAlternateSetting.device: UsbDevice
    get() = `interface`.device

val UsbAlternateSetting.backend: UsbBackend
    get() = `interface`.backend
