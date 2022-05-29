package org.schism.schismatic

class USBInterface(
    val configuration: USBConfiguration,
    val interfaceNumber: UByte,
    val alternateSetting: UByte,
    val interfaceClass: UByte,
    val interfaceSubClass: UByte,
    val interfaceProtocol: UByte,
    val name: USBStringDescriptorIndex,
    val endpoints: List<USBEndpoint>,
)
