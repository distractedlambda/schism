@file:Suppress("NOTHING_TO_INLINE")

package org.schism.memory

import org.schism.math.minusExact
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * A type that facilitates sequential reads, possibly of multibyte values, from an in-memory buffer.
 *
 * Reads done through a [MemoryDecoder] should be immediate and should not involve I/O or other blocking operations;
 * the purpose of this being an `interface` is _not_ to facilitate abstraction over I/O facilities. Instead, the intent
 * is to abstract over both in-heap and out-of-heap memory destinations (a single concrete type could not do this
 * efficiently, at least not on HotSpot), as well as over additional operations done as part of reading (e.g. logging,
 * checksum verification, duplication, etc.).
 *
 * Use-sites of [MemoryDecoder]s should strive to be monomorphic (whether statically or dynamically). To that end,
 * generic utility or extension functions should be `inline` so that they do not themselves become megamorphic call
 * sites. Wrapper or adapter types may need to be specialized for the concrete types that they wrap.
 */
public interface MemoryDecoder {
    public val position: Long

    public fun skip(count: Long)

    public fun nextByte(): Byte

    public fun nextLeChar(): Char

    public fun nextBeChar(): Char

    public fun nextNativeChar(): Char

    public fun nextLeShort(): Short

    public fun nextBeShort(): Short

    public fun nextNativeShort(): Short

    public fun nextLeInt(): Int

    public fun nextBeInt(): Int

    public fun nextNativeInt(): Int

    public fun nextLeLong(): Long

    public fun nextBeLong(): Long

    public fun nextNativeLong(): Long

    public fun nextLeFloat(): Float

    public fun nextBeFloat(): Float

    public fun nextNativeFloat(): Float

    public fun nextLeDouble(): Double

    public fun nextBeDouble(): Double

    public fun nextNativeDouble(): Double
}

public inline fun MemoryDecoder.nextUByte(): UByte {
    return nextByte().toUByte()
}

public inline fun MemoryDecoder.nextLeUShort(): UShort {
    return nextLeShort().toUShort()
}

public inline fun MemoryDecoder.nextBeUShort(): UShort {
    return nextBeShort().toUShort()
}

public inline fun MemoryDecoder.nextNativeUShort(): UShort {
    return nextNativeShort().toUShort()
}

public inline fun MemoryDecoder.nextLeUInt(): UInt {
    return nextLeInt().toUInt()
}

public inline fun MemoryDecoder.nextBeUInt(): UInt {
    return nextBeInt().toUInt()
}

public inline fun MemoryDecoder.nextNativeUInt(): UInt {
    return nextNativeInt().toUInt()
}

public inline fun MemoryDecoder.nextLeULong(): ULong {
    return nextLeLong().toULong()
}

public inline fun MemoryDecoder.nextBeULong(): ULong {
    return nextBeLong().toULong()
}

public inline fun MemoryDecoder.nextNativeULong(): ULong {
    return nextNativeLong().toULong()
}

public inline fun MemoryDecoder.nextLeUtf16(size: Int): String {
    return String(CharArray(size) { nextLeChar() })
}

public inline fun MemoryDecoder.nextBeUtf16(size: Int): String {
    return String(CharArray(size) { nextBeChar() })
}

public inline fun MemoryDecoder.nextNativeUtf16(size: Int): String {
    return String(CharArray(size) { nextNativeChar() })
}

@OptIn(ExperimentalContracts::class)
public inline fun <T : MemoryDecoder> T.positionalDifference(block: T.() -> Unit): Long {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    val startingPosition = position

    block()

    return position minusExact startingPosition
}
