package org.schism.foreign

@JvmInline value class ByteOffset(inline val value: Long)

inline val Int.byteOffset: ByteOffset get() = ByteOffset(this.toLong())

inline val Long.byteOffset: ByteOffset get() = ByteOffset(this)
