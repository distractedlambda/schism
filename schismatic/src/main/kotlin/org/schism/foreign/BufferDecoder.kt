@file:Suppress("NOTHING_TO_INLINE")

package org.schism.foreign

interface BufferDecoder {
    fun skip(count: Long)

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

inline fun BufferDecoder.nextUByte(): UByte {
    return nextByte().toUByte()
}

inline fun BufferDecoder.nextLeUShort(): UShort {
    return nextLeShort().toUShort()
}

inline fun BufferDecoder.nextBeUShort(): UShort {
    return nextBeShort().toUShort()
}

inline fun BufferDecoder.nextLeChar(): Char {
    return Char(nextLeUShort())
}

inline fun BufferDecoder.nextBeChar(): Char {
    return Char(nextBeUShort())
}

inline fun BufferDecoder.nextLeUInt(): UInt {
    return nextLeInt().toUInt()
}

inline fun BufferDecoder.nextBeUInt(): UInt {
    return nextBeInt().toUInt()
}

inline fun BufferDecoder.nextLeULong(): ULong {
    return nextLeLong().toULong()
}

inline fun BufferDecoder.nextBeULong(): ULong {
    return nextBeLong().toULong()
}
