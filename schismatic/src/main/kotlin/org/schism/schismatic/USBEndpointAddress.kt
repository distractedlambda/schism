package org.schism.schismatic

@JvmInline
value class USBEndpointAddress(private val rawValue: UByte) {
    val endpointNumber get() = rawValue and 0xFu

    val direction get() = if (rawValue.toUInt() shr 7 == 0u) Direction.Out else Direction.In

    fun toUByte() = rawValue

    enum class Direction {
        Out,
        In,
    }
}
