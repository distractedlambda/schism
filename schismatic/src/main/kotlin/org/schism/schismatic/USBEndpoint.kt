package org.schism.schismatic

sealed class USBEndpoint(
    val `interface`: USBInterface,
    val address: USBEndpointAddress,
    val maxPacketSize: UShort,
)
