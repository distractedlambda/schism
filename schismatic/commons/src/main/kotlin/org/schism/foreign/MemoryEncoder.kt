@file:Suppress("NOTHING_TO_INLINE")

package org.schism.foreign

import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout.JAVA_BYTE

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

    public fun putBytes(source: MemorySegment) {
        MemorySegment.copy(source, 0, segment, offset, source.byteSize())
        advance(source.byteSize())
    }

    public fun putBytes(source: ByteArray, sourceIndex: Int = 0, count: Int = source.size - sourceIndex) {
        MemorySegment.copy(source, sourceIndex, segment, JAVA_BYTE, offset, count)
        advance(count.toLong())
    }

    public fun putUtf8(source: String, sourceIndex: Int = 0, charCount: Int = source.length - sourceIndex) {
        putBytes(source.encodeToByteArray(sourceIndex, sourceIndex + charCount))
    }

    public inline fun putUByte(value: UByte) {
        segment.setUByte(offset, value)
        advance(1)
    }

    @ExperimentalUnsignedTypes
    public fun putUBytes(source: UByteArray, sourceIndex: Int = 0, count: Int = source.size - sourceIndex) {
        putBytes(source.asByteArray(), sourceIndex, count)
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

    public fun putShorts(source: ShortArray, sourceIndex: Int = 0, count: Int = source.size - sourceIndex) {
        MemorySegment.copy(source, sourceIndex, segment, SHORT_LAYOUT, offset, count)
        advance(count * 2L)
    }

    public fun putLeShorts(source: ShortArray, sourceIndex: Int = 0, count: Int = source.size - sourceIndex) {
        MemorySegment.copy(source, sourceIndex, segment, LE_SHORT_LAYOUT, offset, count)
        advance(count * 2L)
    }

    public fun putBeShorts(source: ShortArray, sourceIndex: Int = 0, count: Int = source.size - sourceIndex) {
        MemorySegment.copy(source, sourceIndex, segment, BE_SHORT_LAYOUT, offset, count)
        advance(count * 2L)
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

    @ExperimentalUnsignedTypes
    public fun putUShorts(source: UShortArray, sourceIndex: Int = 0, count: Int = source.size - sourceIndex) {
        putShorts(source.asShortArray(), sourceIndex, count)
    }

    @ExperimentalUnsignedTypes
    public fun putLeUShorts(source: UShortArray, sourceIndex: Int = 0, count: Int = source.size - sourceIndex) {
        putLeShorts(source.asShortArray(), sourceIndex, count)
    }

    @ExperimentalUnsignedTypes
    public fun putBeUShorts(source: UShortArray, sourceIndex: Int = 0, count: Int = source.size - sourceIndex) {
        putBeShorts(source.asShortArray(), sourceIndex, count)
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

    public fun putChars(source: CharArray, sourceIndex: Int = 0, count: Int = source.size - sourceIndex) {
        MemorySegment.copy(source, sourceIndex, segment, CHAR_LAYOUT, offset, count)
        advance(count * 2L)
    }

    public fun putLeChars(source: CharArray, sourceIndex: Int = 0, count: Int = source.size - sourceIndex) {
        MemorySegment.copy(source, sourceIndex, segment, LE_CHAR_LAYOUT, offset, count)
        advance(count * 2L)
    }

    public fun putBeChars(source: CharArray, sourceIndex: Int = 0, count: Int = source.size - sourceIndex) {
        MemorySegment.copy(source, sourceIndex, segment, BE_CHAR_LAYOUT, offset, count)
        advance(count * 2L)
    }

    public fun putUtf16(source: String, sourceIndex: Int = 0, charCount: Int = source.length - sourceIndex) {
        putChars(source.toCharArray(sourceIndex, sourceIndex + charCount))
    }

    public fun putLeUtf16(source: String, sourceIndex: Int = 0, charCount: Int = source.length - sourceIndex) {
        putLeChars(source.toCharArray(sourceIndex, sourceIndex + charCount))
    }

    public fun putBeUtf16(source: String, sourceIndex: Int = 0, charCount: Int = source.length - sourceIndex) {
        putBeChars(source.toCharArray(sourceIndex, sourceIndex + charCount))
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

    public fun putInts(source: IntArray, sourceIndex: Int = 0, count: Int = source.size - sourceIndex) {
        MemorySegment.copy(source, sourceIndex, segment, INT_LAYOUT, offset, count)
        advance(count * 4L)
    }

    public fun putLeInts(source: IntArray, sourceIndex: Int = 0, count: Int = source.size - sourceIndex) {
        MemorySegment.copy(source, sourceIndex, segment, LE_INT_LAYOUT, offset, count)
        advance(count * 4L)
    }

    public fun putBeInts(source: IntArray, sourceIndex: Int = 0, count: Int = source.size - sourceIndex) {
        MemorySegment.copy(source, sourceIndex, segment, BE_INT_LAYOUT, offset, count)
        advance(count * 4L)
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

    @ExperimentalUnsignedTypes
    public fun putUInts(source: UIntArray, sourceIndex: Int = 0, count: Int = source.size - sourceIndex) {
        putInts(source.asIntArray(), sourceIndex, count)
    }

    @ExperimentalUnsignedTypes
    public fun putLeUInts(source: UIntArray, sourceIndex: Int = 0, count: Int = source.size - sourceIndex) {
        putLeInts(source.asIntArray(), sourceIndex, count)
    }

    @ExperimentalUnsignedTypes
    public fun putBeUInts(source: UIntArray, sourceIndex: Int = 0, count: Int = source.size - sourceIndex) {
        putBeInts(source.asIntArray(), sourceIndex, count)
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

    public fun putLongs(source: LongArray, sourceIndex: Int = 0, count: Int = source.size - sourceIndex) {
        MemorySegment.copy(source, sourceIndex, segment, LONG_LAYOUT, offset, count)
        advance(count * 8L)
    }

    public fun putLeLongs(source: LongArray, sourceIndex: Int = 0, count: Int = source.size - sourceIndex) {
        MemorySegment.copy(source, sourceIndex, segment, LE_LONG_LAYOUT, offset, count)
        advance(count * 8L)
    }

    public fun putBeLongs(source: LongArray, sourceIndex: Int = 0, count: Int = source.size - sourceIndex) {
        MemorySegment.copy(source, sourceIndex, segment, BE_LONG_LAYOUT, offset, count)
        advance(count * 8L)
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

    @ExperimentalUnsignedTypes
    public fun putULongs(source: ULongArray, sourceIndex: Int = 0, count: Int = source.size - sourceIndex) {
        putLongs(source.asLongArray(), sourceIndex, count)
    }

    @ExperimentalUnsignedTypes
    public fun putLeULongs(source: ULongArray, sourceIndex: Int = 0, count: Int = source.size - sourceIndex) {
        putLeLongs(source.asLongArray(), sourceIndex, count)
    }

    @ExperimentalUnsignedTypes
    public fun putBeULongs(source: ULongArray, sourceIndex: Int = 0, count: Int = source.size - sourceIndex) {
        putBeLongs(source.asLongArray(), sourceIndex, count)
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

    public fun putFloats(source: FloatArray, sourceIndex: Int = 0, count: Int = source.size - sourceIndex) {
        MemorySegment.copy(source, sourceIndex, segment, FLOAT_LAYOUT, offset, count)
        advance(count * 4L)
    }

    public fun putLeFloats(source: FloatArray, sourceIndex: Int = 0, count: Int = source.size - sourceIndex) {
        MemorySegment.copy(source, sourceIndex, segment, LE_FLOAT_LAYOUT, offset, count)
        advance(count * 4L)
    }

    public fun putBeFloats(source: FloatArray, sourceIndex: Int = 0, count: Int = source.size - sourceIndex) {
        MemorySegment.copy(source, sourceIndex, segment, BE_FLOAT_LAYOUT, offset, count)
        advance(count * 4L)
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

    public fun putDoubles(source: DoubleArray, sourceIndex: Int = 0, count: Int = source.size - sourceIndex) {
        MemorySegment.copy(source, sourceIndex, segment, DOUBLE_LAYOUT, offset, count)
        advance(count * 8L)
    }

    public fun putLeDoubles(source: DoubleArray, sourceIndex: Int = 0, count: Int = source.size - sourceIndex) {
        MemorySegment.copy(source, sourceIndex, segment, LE_DOUBLE_LAYOUT, offset, count)
        advance(count * 8L)
    }

    public fun putBeDoubles(source: DoubleArray, sourceIndex: Int = 0, count: Int = source.size - sourceIndex) {
        MemorySegment.copy(source, sourceIndex, segment, BE_DOUBLE_LAYOUT, offset, count)
        advance(count * 8L)
    }
}

public fun MemorySegment.encoder(offset: Long = 0): MemoryEncoder {
    return MemoryEncoder(this, offset)
}
