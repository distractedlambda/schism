package org.schism.usb

public class UsbBulkTransferInEndpoint internal constructor(
    iface: UsbInterface,
    address: UByte,
    maxPacketSize: UShort,
) : UsbEndpoint(iface, address, maxPacketSize) {
    public companion object
}
