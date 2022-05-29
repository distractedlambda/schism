package org.schism.schismatic

@JvmInline
value class USBStringDescriptorIndex(private val rawValue: UByte) {
    fun toUByte() = rawValue
}
