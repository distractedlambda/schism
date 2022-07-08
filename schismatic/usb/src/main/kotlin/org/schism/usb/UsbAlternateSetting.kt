package org.schism.usb

public class UsbAlternateSetting internal constructor(
    public val `interface`: UsbInterface,
) {
    val interfaceClass: UByte
    val interfaceSubClass: UByte
    val interfaceProtocol: UByte
    val endpoints: List<UsbEndpoint>
}
