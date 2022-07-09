package org.schism.memory

import org.schism.math.minusExact
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

public interface MemoryDecoder {
    public val position: Long

    public fun hasRemaining(): Boolean

    public fun skip(count: Long)

    public fun nextBytes(destination: Memory)

    public fun nextByte(): Byte

    public fun nextLeChar(): Char

    public fun nextBeChar(): Char

    public fun nextChar(): Char

    public fun nextLeShort(): Short

    public fun nextBeShort(): Short

    public fun nextShort(): Short

    public fun nextLeInt(): Int

    public fun nextBeInt(): Int

    public fun nextInt(): Int

    public fun nextLeLong(): Long

    public fun nextBeLong(): Long

    public fun nextLong(): Long

    public fun nextLeFloat(): Float

    public fun nextBeFloat(): Float

    public fun nextFloat(): Float

    public fun nextLeDouble(): Double

    public fun nextBeDouble(): Double

    public fun nextDouble(): Double

    public companion object
}

public fun MemoryDecoder.nextUByte(): UByte {
    return nextByte().toUByte()
}

public fun MemoryDecoder.nextLeUShort(): UShort {
    return nextLeShort().toUShort()
}

public fun MemoryDecoder.nextBeUShort(): UShort {
    return nextBeShort().toUShort()
}

public fun MemoryDecoder.nextUShort(): UShort {
    return nextShort().toUShort()
}

public fun MemoryDecoder.nextLeUInt(): UInt {
    return nextLeInt().toUInt()
}

public fun MemoryDecoder.nextBeUInt(): UInt {
    return nextBeInt().toUInt()
}

public fun MemoryDecoder.nextUInt(): UInt {
    return nextInt().toUInt()
}

public fun MemoryDecoder.nextLeULong(): ULong {
    return nextLeLong().toULong()
}

public fun MemoryDecoder.nextBeULong(): ULong {
    return nextBeLong().toULong()
}

public fun MemoryDecoder.nextULong(): ULong {
    return nextLong().toULong()
}

public fun MemoryDecoder.nextLeUtf16(size: Int): String {
    return String(CharArray(size) { nextLeChar() })
}

public fun MemoryDecoder.nextBeUtf16(size: Int): String {
    return String(CharArray(size) { nextBeChar() })
}

public fun MemoryDecoder.nextUtf16(size: Int): String {
    return String(CharArray(size) { nextChar() })
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
