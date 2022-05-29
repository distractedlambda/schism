package org.schism.schismatic

import java.lang.foreign.MemoryAddress

class USBConfiguration(
    val device: USBDevice,
    val value: USBConfigurationValue,
    val name: USBStringDescriptorIndex,
    val selfPowered: Boolean,
    val remoteWakeup: Boolean,
    val maxPowerMilliamps: Int,
    interfaces: MemoryAddress,
    interfaceCount: UByte,
) {
}
