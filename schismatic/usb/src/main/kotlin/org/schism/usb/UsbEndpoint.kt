package org.schism.usb

public sealed class UsbEndpoint(
    public val iface: UsbInterface,
    internal val address: UByte,
    public val maxPacketSize: UShort,
) {
    public val configuration: UsbConfiguration
        get() = iface.configuration

    public val device: UsbDevice
        get() = iface.device

    public companion object
}
