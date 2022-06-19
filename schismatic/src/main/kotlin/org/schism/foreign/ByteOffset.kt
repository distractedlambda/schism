@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package org.schism.foreign

import java.lang.foreign.MemoryAddress
import kotlin.internal.InlineOnly

@JvmInline value class ByteOffset(inline val value: Long)

@InlineOnly inline val Int.byteOffset: ByteOffset
    get() = ByteOffset(this.toLong())

@InlineOnly inline val Long.byteOffset: ByteOffset
    get() = ByteOffset(this)

@InlineOnly inline operator fun MemoryAddress.plus(rhs: ByteOffset): MemoryAddress {
    // FIXME: check overflow?
    return addOffset(rhs.value)
}
