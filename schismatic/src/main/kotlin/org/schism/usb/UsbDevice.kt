package org.schism.usb

interface UsbDevice {
    val backend: UsbBackend

    val usbVersion: UShort

    val deviceClass: UByte

    val deviceSubClass: UByte

    val deviceProtocol: UByte

    val vendorId: UShort

    val productId: UShort

    val deviceVersion: UShort

    val configurations: List<UsbConfiguration>

    fun connect(): UsbDeviceConnection
}
