package org.schism.cousb

public class UsbBulkTransferOutEndpoint internal constructor(
    iface: UsbInterface,
    address: UByte,
    maxPacketSize: UShort,
) : UsbEndpoint(iface, address, maxPacketSize) {
    public companion object
}
