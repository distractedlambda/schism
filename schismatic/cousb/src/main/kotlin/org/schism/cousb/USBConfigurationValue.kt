package org.schism.cousb

@JvmInline
public value class USBConfigurationValue(private val numericValue: UByte) {
    public fun toUByte(): UByte = numericValue
}
