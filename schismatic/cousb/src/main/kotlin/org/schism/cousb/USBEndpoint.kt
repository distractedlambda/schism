package org.schism.cousb

public sealed class USBEndpoint(
    public val `interface`: USBInterface,
    public val address: USBEndpointAddress,
    public val maxPacketSize: UShort,
)
