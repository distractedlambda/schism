package org.schism.cousb

public class UsbControlEndpoint internal constructor(
    iface: UsbInterface,
    address: UByte,
    maxPacketSize: UShort,
) : UsbEndpoint(iface, address, maxPacketSize) {
    public companion object
}
