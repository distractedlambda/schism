package org.schism.cousb

@JvmInline
public value class USBEndpointAddress(private val rawValue: UByte) {
    public val endpointNumber: UByte get() = rawValue and 0xFu

    public val direction: Direction get() = if (rawValue.toUInt() shr 7 == 0u) Direction.Out else Direction.In

    public fun toUByte(): UByte = rawValue

    public enum class Direction {
        Out,
        In,
    }
}
