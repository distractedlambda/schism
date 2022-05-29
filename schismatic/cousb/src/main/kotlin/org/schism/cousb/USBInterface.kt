package org.schism.cousb

public class USBInterface internal constructor(
    public val configuration: USBConfiguration,
    public val interfaceNumber: UByte,
    public val alternateSetting: UByte,
    public val interfaceClass: UByte,
    public val interfaceSubClass: UByte,
    public val interfaceProtocol: UByte,
    public val name: USBStringDescriptorIndex,
    public val endpoints: List<USBEndpoint>,
)
