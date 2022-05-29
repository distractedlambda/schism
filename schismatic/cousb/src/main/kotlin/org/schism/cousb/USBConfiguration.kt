package org.schism.cousb

import java.lang.foreign.MemoryAddress

public class USBConfiguration internal constructor(
    public val device: USBDevice,
    public val value: USBConfigurationValue,
    public val name: USBStringDescriptorIndex,
    public val selfPowered: Boolean,
    public val remoteWakeup: Boolean,
    public val maxPowerMilliamps: Int,
    interfaces: MemoryAddress,
    interfaceCount: UByte,
)
