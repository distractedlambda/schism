@file:Suppress("NOTHING_TO_INLINE")

package org.schism.foreign

import java.lang.Math.addExact
import java.lang.Math.subtractExact
import java.lang.foreign.Addressable
import java.lang.foreign.MemoryAddress

@JvmInline value class NativeAddress(inline val numericValue: Long) {
    inline fun toMemoryAddress(): MemoryAddress {
        return MemoryAddress.ofLong(numericValue)
    }

    inline operator fun plus(offset: ByteOffset): NativeAddress {
        return NativeAddress(addExact(numericValue, offset.value))
    }

    inline operator fun minus(offset: ByteOffset): NativeAddress {
        return NativeAddress(subtractExact(numericValue, offset.value))
    }

    inline operator fun minus(other: NativeAddress): ByteOffset {
        return ByteOffset(subtractExact(numericValue, other.numericValue))
    }

    inline fun requireAlignedTo(alignment: Long) {
        numericValue.requireAlignedTo(alignment)
    }

    inline fun isNULL(): Boolean {
        return numericValue == 0L
    }

    override fun toString(): String {
        return "NativeAddress(0x${numericValue.toString(16).padStart(16, '0')})"
    }

    companion object {
        inline val NULL: NativeAddress get() = NativeAddress(0)
    }
}

inline fun Addressable.nativeAddress(): NativeAddress {
    return NativeAddress(address().toRawLongValue())
}
