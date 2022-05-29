package org.schism.cousb

public sealed class USBEndpoint(
    public val iface: USBInterface,
    internal val address: UByte,
    public val maxPacketSize: UShort,
) {
    public val configuration: USBConfiguration
        get() = iface.configuration
    
    public val device: USBDevice
        get() = iface.device

    public companion object
}
