package org.schism.cousb

public class USBBulkTransferOutEndpoint internal constructor(
    iface: USBInterface,
    address: UByte,
    maxPacketSize: UShort,
) : USBEndpoint(iface, address, maxPacketSize) {
    public companion object
}
