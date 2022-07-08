package org.schism.usb

public class UsbConfiguration internal constructor(
    public val device: UsbDevice,
) {
    public val interfaces: List<UsbInterface>
}
