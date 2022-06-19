@file:Suppress("NOTHING_TO_INLINE", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package org.schism.foreign

import kotlin.internal.InlineOnly

interface BufferDecoder {
    fun discardNext(count: Long)

    fun nextByte(): Byte

    fun nextLeShort(): Short

    fun nextBeShort(): Short

    fun nextLeInt(): Int

    fun nextBeInt(): Int

    fun nextLeLong(): Long

    fun nextBeLong(): Long

    fun nextLeFloat(): Float

    fun nextBeFloat(): Float

    fun nextLeDouble(): Double

    fun nextBeDouble(): Double
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

@InlineOnly inline fun BufferDecoder.nextLeChar(): Char {
    return Char(nextLeUShort())
}

@InlineOnly inline fun BufferDecoder.nextBeChar(): Char {
    return Char(nextBeUShort())
}

@InlineOnly inline fun BufferDecoder.nextLeUInt(): UInt {
    return nextLeInt().toUInt()
}

@InlineOnly inline fun BufferDecoder.nextBeUInt(): UInt {
    return nextBeInt().toUInt()
}

@InlineOnly inline fun BufferDecoder.nextLeULong(): ULong {
    return nextLeLong().toULong()
}

@InlineOnly inline fun BufferDecoder.nextBeULong(): ULong {
    return nextBeLong().toULong()
}
