@file:Suppress("NOTHING_TO_INLINE", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package org.schism.foreign

import org.schism.math.minusExact
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.InvocationKind.EXACTLY_ONCE
import kotlin.contracts.contract
import kotlin.internal.InlineOnly

/**
 * A type that facilitates sequential writes, possibly of multibyte values, into an in-memory buffer.
 *
 * Writes done through a [BufferEncoder] should be immediate and should not involve I/O or other blocking operations;
 * the purpose of this being an `interface` is _not_ to facilitate abstraction over I/O facilities. Instead, the intent
 * is to abstract over both in-heap and out-of-heap memory destinations (a single concrete type could not do this
 * efficiently, at least not on HotSpot), as well as over additional operations done as part of writing (e.g. logging,
 * checksum calculation, duplication, etc.).
 *
 * Use-sites of [BufferEncoder]s should strive to be monomorphic (whether statically or dynamically). To that end,
 * generic utility or extension functions should be `inline` so that they do not themselves become megamorphic call
 * sites. Wrapper or adapter types may need to be specialized for the concrete types that they wrap.
 */
interface BufferEncoder {
    val position: Long

    fun skip(count: Long)

    fun putByte(value: Byte)

    fun putLeShort(value: Short)

    fun putBeShort(value: Short)

    fun putNativeShort(value: Short)

    fun putLeInt(value: Int)

    fun putBeInt(value: Int)

    fun putNativeInt(value: Int)

    fun putLeLong(value: Long)

    fun putBeLong(value: Long)

    fun putNativeLong(value: Long)

    fun putLeFloat(value: Float)

    fun putBeFloat(value: Float)

    fun putNativeFloat(value: Float)

    fun putLeDouble(value: Double)

    fun putBeDouble(value: Double)

    fun putNativeDouble(value: Double)
}

@InlineOnly inline fun BufferEncoder.putUByte(value: UByte) {
    putByte(value.toByte())
}

@InlineOnly inline fun BufferEncoder.putLeChar(value: Char) {
    putLeShort(value.code.toShort())
}

@InlineOnly inline fun BufferEncoder.putBeChar(value: Char) {
    putBeShort(value.code.toShort())
}

@InlineOnly inline fun BufferEncoder.putNativeChar(value: Char) {
    putNativeShort(value.code.toShort())
}

@InlineOnly inline fun BufferEncoder.putLeUShort(value: UShort) {
    putLeShort(value.toShort())
}

@InlineOnly inline fun BufferEncoder.putBeUShort(value: UShort) {
    putBeShort(value.toShort())
}

@InlineOnly inline fun BufferEncoder.putNativeUShort(value: UShort) {
    putNativeShort(value.toShort())
}

@InlineOnly inline fun BufferEncoder.putLeUInt(value: UInt) {
    putLeInt(value.toInt())
}

@InlineOnly inline fun BufferEncoder.putBeUInt(value: UInt) {
    putBeInt(value.toInt())
}

@InlineOnly inline fun BufferEncoder.putNativeUInt(value: UInt) {
    putNativeInt(value.toInt())
}

@InlineOnly inline fun BufferEncoder.putLeULong(value: ULong) {
    putLeLong(value.toLong())
}

@InlineOnly inline fun BufferEncoder.putBeULong(value: ULong) {
    putBeLong(value.toLong())
}

@InlineOnly inline fun BufferEncoder.putNativeULong(value: ULong) {
    putNativeLong(value.toLong())
}

@OptIn(ExperimentalContracts::class)
inline fun <T : BufferEncoder> T.positionalDifference(block: T.() -> Unit): Long {
    contract {
        callsInPlace(block, EXACTLY_ONCE)
    }

    val startingPosition = position

    block()

    return position minusExact startingPosition
}
