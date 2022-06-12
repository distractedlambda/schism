package org.schism.foreign

@JvmInline value class ByteOffset(inline val value: Long)

inline val Long.byteOffset: ByteOffset get() = ByteOffset(this)
