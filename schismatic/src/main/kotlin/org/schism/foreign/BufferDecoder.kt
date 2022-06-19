@file:Suppress("NOTHING_TO_INLINE", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package org.schism.foreign

import org.schism.math.minusExact
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.internal.InlineOnly

/**
 * A type that facilitates sequential reads, possibly of multibyte values, from an in-memory buffer.
 *
 * Reads done through a [BufferDecoder] should be immediate and should not involve I/O or other blocking operations;
 * the purpose of this being an `interface` is _not_ to facilitate abstraction over I/O facilities. Instead, the intent
 * is to abstract over both in-heap and out-of-heap memory destinations (a single concrete type could not do this
 * efficiently, at least not on HotSpot), as well as over additional operations done as part of reading (e.g. logging,
 * checksum verification, duplication, etc.).
 *
 * Use-sites of [BufferDecoder]s should strive to be monomorphic (whether statically or dynamically). To that end,
 * generic utility or extension functions should be `inline` so that they do not themselves become megamorphic call
 * sites. Wrapper or adapter types may need to be specialized for the concrete types that they wrap.
 */
interface BufferDecoder {
    val position: Long

    fun skip(count: Long)

    fun nextByte(): Byte

    fun nextLeShort(): Short

    fun nextBeShort(): Short

    fun nextNativeShort(): Short

    fun nextLeInt(): Int

    fun nextBeInt(): Int

    fun nextNativeInt(): Int

    fun nextLeLong(): Long

    fun nextBeLong(): Long

    fun nextNativeLong(): Long

    fun nextLeFloat(): Float

    fun nextBeFloat(): Float

    fun nextNativeFloat(): Float

    fun nextLeDouble(): Double

    fun nextBeDouble(): Double

    fun nextNativeDouble(): Double
}

@InlineOnly inline fun BufferDecoder.nextUByte(): UByte {
    return nextByte().toUByte()
}

@InlineOnly inline fun BufferDecoder.nextLeUShort(): UShort {
    return nextLeShort().toUShort()
}

@InlineOnly inline fun BufferDecoder.nextBeUShort(): UShort {
    return nextBeShort().toUShort()
}

@InlineOnly inline fun BufferDecoder.nextNativeUShort(): UShort {
    return nextNativeShort().toUShort()
}

@InlineOnly inline fun BufferDecoder.nextLeChar(): Char {
    return Char(nextLeUShort())
}

@InlineOnly inline fun BufferDecoder.nextBeChar(): Char {
    return Char(nextBeUShort())
}

@InlineOnly inline fun BufferDecoder.nextNativeChar(): Char {
    return Char(nextNativeUShort())
}

@InlineOnly inline fun BufferDecoder.nextLeUInt(): UInt {
    return nextLeInt().toUInt()
}

@InlineOnly inline fun BufferDecoder.nextBeUInt(): UInt {
    return nextBeInt().toUInt()
}

@InlineOnly inline fun BufferDecoder.nextNativeUInt(): UInt {
    return nextNativeInt().toUInt()
}

@InlineOnly inline fun BufferDecoder.nextLeULong(): ULong {
    return nextLeLong().toULong()
}

@InlineOnly inline fun BufferDecoder.nextBeULong(): ULong {
    return nextBeLong().toULong()
}

@InlineOnly inline fun BufferDecoder.nextNativeULong(): ULong {
    return nextNativeLong().toULong()
}

inline fun BufferDecoder.nextLeUtf16(size: Int): String {
    return String(CharArray(size) { nextLeChar() })
}

inline fun BufferDecoder.nextBeUtf16(size: Int): String {
    return String(CharArray(size) { nextBeChar() })
}

inline fun BufferDecoder.nextNativeUtf16(size: Int): String {
    return String(CharArray(size) { nextNativeChar() })
}

@OptIn(ExperimentalContracts::class)
inline fun <T : BufferDecoder> T.positionalDifference(block: T.() -> Unit): Long {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    val startingPosition = position

    block()

    return position minusExact startingPosition
}
