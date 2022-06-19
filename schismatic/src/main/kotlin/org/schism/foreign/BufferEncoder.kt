@file:Suppress("NOTHING_TO_INLINE", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package org.schism.foreign

import kotlin.internal.InlineOnly

interface BufferEncoder {
    fun putUndefined(count: Long)

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

@InlineOnly inline fun BufferEncoder.putUByte(value: UByte) {
    putByte(value.toByte())
}

@InlineOnly inline fun BufferEncoder.putLeChar(value: Char) {
    putLeShort(value.code.toShort())
}

@InlineOnly inline fun BufferEncoder.putBeChar(value: Char) {
    putBeShort(value.code.toShort())
}

@InlineOnly inline fun BufferEncoder.putLeUShort(value: UShort) {
    putLeShort(value.toShort())
}

@InlineOnly inline fun BufferEncoder.putBeUShort(value: UShort) {
    putBeShort(value.toShort())
}

@InlineOnly inline fun BufferEncoder.putLeUInt(value: UInt) {
    putLeInt(value.toInt())
}

@InlineOnly inline fun BufferEncoder.putBeUInt(value: UInt) {
    putBeInt(value.toInt())
}

@InlineOnly inline fun BufferEncoder.putLeULong(value: ULong) {
    putLeLong(value.toLong())
}

@InlineOnly inline fun BufferEncoder.putBeULong(value: ULong) {
    putBeLong(value.toLong())
}
