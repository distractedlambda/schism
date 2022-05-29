package org.schism.schismatic

@JvmInline
value class USBConfigurationValue(private val numericValue: UByte) {
    fun toUByte() = numericValue
}
