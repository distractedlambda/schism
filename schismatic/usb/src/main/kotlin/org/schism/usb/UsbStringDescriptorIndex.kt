package org.schism.usb

@JvmInline
public value class UsbStringDescriptorIndex(private val rawValue: UByte) {
    public fun toUByte(): UByte = rawValue

    public companion object
}
