@file:Suppress("NOTHING_TO_INLINE")

package org.schism.foreign

interface BufferEncoder {
    fun skip(count: Long)

    fun putByte(value: Byte)

    fun putLeShort(value: Short)

    fun putBeShort(value: Short)

    fun putLeInt(value: Int)

    fun putBeInt(value: Int)

    fun putLeLong(value: Long)

    fun putBeLong(value: Long)

    fun putLeFloat(value: Float)

    fun putBeFloat(value: Float)

    fun putLeDouble(value: Double)

    fun putBeDouble(value: Double)
}

inline fun BufferEncoder.putUByte(value: UByte) {
    putByte(value.toByte())
}

inline fun BufferEncoder.putLeChar(value: Char) {
    putLeShort(value.code.toShort())
}

inline fun BufferEncoder.putBeChar(value: Char) {
    putBeShort(value.code.toShort())
}

inline fun BufferEncoder.putLeUShort(value: UShort) {
    putLeShort(value.toShort())
}

inline fun BufferEncoder.putBeUShort(value: UShort) {
    putBeShort(value.toShort())
}

inline fun BufferEncoder.putLeUInt(value: UInt) {
    putLeInt(value.toInt())
}

inline fun BufferEncoder.putBeUInt(value: UInt) {
    putBeInt(value.toInt())
}

inline fun BufferEncoder.putLeULong(value: ULong) {
    putLeLong(value.toLong())
}

inline fun BufferEncoder.putBeULong(value: ULong) {
    putBeLong(value.toLong())
}
