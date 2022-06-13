package org.schism.usb

sealed interface UsbEndpoint {
    val alternateSetting: UsbAlternateSetting

    val maxPacketSize: UShort

    context (UsbDeviceConnection) suspend fun clearHalt()
}

val UsbEndpoint.`interface`: UsbInterface
    get() = alternateSetting.`interface`

val UsbEndpoint.configuration: UsbConfiguration
    get() = alternateSetting.configuration

val UsbEndpoint.device: UsbDevice
    get() = alternateSetting.device

val UsbEndpoint.backend: UsbBackend
    get() = alternateSetting.backend
