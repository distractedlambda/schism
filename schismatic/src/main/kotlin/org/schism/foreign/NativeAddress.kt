@file:Suppress("NOTHING_TO_INLINE", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package org.schism.foreign

import java.lang.Math.addExact
import java.lang.Math.subtractExact
import java.lang.foreign.Addressable
import java.lang.foreign.MemoryAddress
import kotlin.internal.InlineOnly

@JvmInline value class NativeAddress(inline val numericValue: Long) {
    @InlineOnly inline fun toMemoryAddress(): MemoryAddress {
        return MemoryAddress.ofLong(numericValue)
    }

    @InlineOnly inline operator fun plus(offset: ByteOffset): NativeAddress {
        return NativeAddress(addExact(numericValue, offset.value))
    }

    @InlineOnly inline operator fun minus(offset: ByteOffset): NativeAddress {
        return NativeAddress(subtractExact(numericValue, offset.value))
    }

    @InlineOnly inline operator fun minus(other: NativeAddress): ByteOffset {
        return ByteOffset(subtractExact(numericValue, other.numericValue))
    }

    @InlineOnly inline fun requireAlignedTo(alignment: Long) {
        numericValue.requireAlignedTo(alignment)
    }

    @InlineOnly inline fun isNULL(): Boolean {
        return numericValue == 0L
    }

    override fun toString(): String {
        return "NativeAddress(0x${numericValue.toString(16).padStart(16, '0')})"
    }

    companion object {
        @InlineOnly inline val NULL: NativeAddress get() = NativeAddress(0)
    }
}

@InlineOnly inline fun Addressable.nativeAddress(): NativeAddress {
    return NativeAddress(address().toRawLongValue())
}
