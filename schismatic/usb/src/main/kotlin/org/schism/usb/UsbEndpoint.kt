package org.schism.usb

public sealed class UsbEndpoint(
    public val alternateSetting: UsbAlternateSetting,
    public val maxPacketSize: UShort,
)
