@file:Suppress("NOTHING_TO_INLINE")

package org.schism.foreign

import java.lang.foreign.MemorySegment

public class MemoryEncoder(public val segment: MemorySegment, public var offset: Long = 0) {
    public val remaining: Long get() {
        return segment.byteSize() - offset
    }

    public val hasRemaining: Boolean get() {
        return remaining != 0L
    }

    public fun advance(count: Long) {
        offset += count
    }

    public inline fun putByte(value: Byte) {
        segment.setByte(offset, value)
        advance(1)
    }

    public inline fun putUByte(value: UByte) {
        segment.setUByte(offset, value)
        advance(1)
    }

    public inline fun putShort(value: Short) {
        segment.setShort(offset, value)
        advance(2)
    }

    public inline fun putLeShort(value: Short) {
        segment.setLeShort(offset, value)
        advance(2)
    }

    public inline fun putBeShort(value: Short) {
        segment.setBeShort(offset, value)
        advance(2)
    }

    public inline fun putUShort(value: UShort) {
        segment.setUShort(offset, value)
        advance(2)
    }

    public inline fun putLeUShort(value: UShort) {
        segment.setLeUShort(offset, value)
        advance(2)
    }

    public inline fun putBeUShort(value: UShort) {
        segment.setBeUShort(offset, value)
        advance(2)
    }

    public inline fun putChar(value: Char) {
        segment.setChar(offset, value)
        advance(2)
    }

    public inline fun putLeChar(value: Char) {
        segment.setLeChar(offset, value)
        advance(2)
    }

    public inline fun putBeChar(value: Char) {
        segment.setBeChar(offset, value)
        advance(2)
    }

    public inline fun putInt(value: Int) {
        segment.setInt(offset, value)
        advance(4)
    }

    public inline fun putLeInt(value: Int) {
        segment.setLeInt(offset, value)
        advance(4)
    }

    public inline fun putBeInt(value: Int) {
        segment.setBeInt(offset, value)
        advance(4)
    }

    public inline fun putUInt(value: UInt) {
        segment.setUInt(offset, value)
        advance(4)
    }

    public inline fun putLeUInt(value: UInt) {
        segment.setLeUInt(offset, value)
        advance(4)
    }

    public inline fun putBeUInt(value: UInt) {
        segment.setBeUInt(offset, value)
        advance(4)
    }

    public inline fun putLong(value: Long) {
        segment.setLong(offset, value)
        advance(8)
    }

    public inline fun putLeLong(value: Long) {
        segment.setLeLong(offset, value)
        advance(8)
    }

    public inline fun putBeLong(value: Long) {
        segment.setBeLong(offset, value)
        advance(8)
    }

    public inline fun putULong(value: ULong) {
        segment.setULong(offset, value)
        advance(8)
    }

    public inline fun putLeULong(value: ULong) {
        segment.setLeULong(offset, value)
        advance(8)
    }

    public inline fun putBeULong(value: ULong) {
        segment.setBeULong(offset, value)
        advance(8)
    }

    public inline fun putFloat(value: Float) {
        segment.setFloat(offset, value)
        advance(4)
    }

    public inline fun putLeFloat(value: Float) {
        segment.setLeFloat(offset, value)
        advance(4)
    }

    public inline fun putBeFloat(value: Float) {
        segment.setBeFloat(offset, value)
        advance(4)
    }

    public inline fun putDouble(value: Double) {
        segment.setDouble(offset, value)
        advance(8)
    }

    public inline fun putLeDouble(value: Double) {
        segment.setLeDouble(offset, value)
        advance(8)
    }

    public inline fun putBeDouble(value: Double) {
        segment.setBeDouble(offset, value)
        advance(8)
    }
}

public fun MemorySegment.encoder(offset: Long = 0): MemoryEncoder {
    return MemoryEncoder(this, offset)
}
