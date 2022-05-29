package org.schism.cousb

@JvmInline
public value class USBStringDescriptorIndex(private val rawValue: UByte) {
    public fun toUByte(): UByte = rawValue
}
