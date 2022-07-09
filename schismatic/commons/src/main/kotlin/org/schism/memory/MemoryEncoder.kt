package org.schism.memory

import org.schism.math.minusExact
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * A type that facilitates sequential writes, possibly of multibyte values, into an in-memory buffer.
 *
 * Writes done through a [MemoryEncoder] should be immediate and should not involve I/O or other blocking operations;
 * the purpose of this being an `interface` is _not_ to facilitate abstraction over I/O facilities. Instead, the intent
 * is to abstract over both in-heap and out-of-heap memory destinations (a single concrete type could not do this
 * efficiently, at least not on HotSpot), as well as over additional operations done as part of writing (e.g. logging,
 * checksum calculation, duplication, etc.).
 *
 * Use-sites of [MemoryEncoder]s should strive to be monomorphic (whether statically or dynamically). To that end,
 * generic utility or extension functions should be `inline` so that they do not themselves become megamorphic call
 * sites. Wrapper or adapter types may need to be specialized for the concrete types that they wrap.
 */
public interface MemoryEncoder {
    public val position: Long

    public fun skip(count: Long)

    public fun putByte(value: Byte)

    public fun putLeChar(value: Char)

    public fun putBeChar(value: Char)

    public fun putChar(value: Char)

    public fun putLeShort(value: Short)

    public fun putBeShort(value: Short)

    public fun putShort(value: Short)

    public fun putLeInt(value: Int)

    public fun putBeInt(value: Int)

    public fun putInt(value: Int)

    public fun putLeLong(value: Long)

    public fun putBeLong(value: Long)

    public fun putLong(value: Long)

    public fun putLeFloat(value: Float)

    public fun putBeFloat(value: Float)

    public fun putFloat(value: Float)

    public fun putLeDouble(value: Double)

    public fun putBeDouble(value: Double)

    public fun putDouble(value: Double)

    public companion object
}

public fun MemoryEncoder.putUByte(value: UByte) {
    putByte(value.toByte())
}

public fun MemoryEncoder.putLeUShort(value: UShort) {
    putLeShort(value.toShort())
}

public fun MemoryEncoder.putBeUShort(value: UShort) {
    putBeShort(value.toShort())
}

public fun MemoryEncoder.putUShort(value: UShort) {
    putShort(value.toShort())
}

public fun MemoryEncoder.putLeUInt(value: UInt) {
    putLeInt(value.toInt())
}

public fun MemoryEncoder.putBeUInt(value: UInt) {
    putBeInt(value.toInt())
}

public fun MemoryEncoder.putUInt(value: UInt) {
    putInt(value.toInt())
}

public fun MemoryEncoder.putLeULong(value: ULong) {
    putLeLong(value.toLong())
}

public fun MemoryEncoder.putBeULong(value: ULong) {
    putBeLong(value.toLong())
}

public fun MemoryEncoder.putULong(value: ULong) {
    putLong(value.toLong())
}

@OptIn(ExperimentalContracts::class)
public inline fun <T : MemoryEncoder> T.positionalDifference(block: T.() -> Unit): Long {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    val startingPosition = position

    block()

    return position minusExact startingPosition
}
