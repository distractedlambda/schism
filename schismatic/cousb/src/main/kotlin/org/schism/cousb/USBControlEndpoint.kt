package org.schism.cousb

public class USBControlEndpoint internal constructor(
    iface: USBInterface,
    address: UByte,
    maxPacketSize: UShort,
) : USBEndpoint(iface, address, maxPacketSize) {
    public companion object
}
