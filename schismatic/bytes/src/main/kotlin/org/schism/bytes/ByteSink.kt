@file:Suppress("NOTHING_TO_INLINE")

package org.schism.bytes

import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.ByteOrder.LITTLE_ENDIAN
import java.nio.ByteOrder.nativeOrder

public interface ByteSink {
    public fun writeByte(byte: Byte)

    public fun writeZeros(byteCount: Long) {
        require(byteCount >= 0)
        for (i in 0 until byteCount) {
            writeByte(0.toByte())
        }
    }

    public fun skip(byteCount: Long) {
        writeZeros(byteCount)
    }

    public fun writeBytes(bytes: MemorySegment) {
        for (i in 0 until bytes.byteSize()) {
            writeByte(bytes[ValueLayout.JAVA_BYTE, i])
        }
    }

    public fun writeBytes(bytes: ByteArray, offset: Int = 0, size: Int = bytes.size - offset) {
        writeBytes(MemorySegment.ofArray(bytes).asSlice(offset.toLong(), size.toLong()))
    }

    public fun writeBytes(bytes: ByteBuffer) {
        writeBytes(MemorySegment.ofBuffer(bytes))
        bytes.position(bytes.limit())
    }

    public fun writeShort(value: Short, byteOrder: ByteOrder = nativeOrder()) {
        val adaptedValue = if (byteOrder == LITTLE_ENDIAN) value else java.lang.Short.reverseBytes(value)
        writeByte(adaptedValue.toByte())
        writeByte((adaptedValue.toInt() shr 8).toByte())
    }

    public fun writeInt(value: Int, byteOrder: ByteOrder = nativeOrder()) {
        val adaptedValue = if (byteOrder == LITTLE_ENDIAN) value else Integer.reverseBytes(value)
        writeByte(adaptedValue.toByte())
        writeByte((adaptedValue shr 8).toByte())
        writeByte((adaptedValue shr 16).toByte())
        writeByte((adaptedValue shr 24).toByte())
    }

    public fun writeLong(value: Long, byteOrder: ByteOrder = nativeOrder()) {
        val adaptedValue = if (byteOrder == LITTLE_ENDIAN) value else java.lang.Long.reverseBytes(value)
        writeByte(adaptedValue.toByte())
        writeByte((adaptedValue shr 8).toByte())
        writeByte((adaptedValue shr 16).toByte())
        writeByte((adaptedValue shr 24).toByte())
        writeByte((adaptedValue shr 32).toByte())
        writeByte((adaptedValue shr 40).toByte())
        writeByte((adaptedValue shr 48).toByte())
        writeByte((adaptedValue shr 56).toByte())
    }
}

public inline fun ByteSink.writeUByte(value: UByte) {
    writeByte(value.toByte())
}

public inline fun ByteSink.writeChar(value: Char, byteOrder: ByteOrder = nativeOrder()) {
    writeShort(value.code.toShort(), byteOrder)
}

public inline fun ByteSink.writeUShort(value: UShort, byteOrder: ByteOrder = nativeOrder()) {
    writeShort(value.toShort(), byteOrder)
}

public inline fun ByteSink.writeUInt(value: UInt, byteOrder: ByteOrder = nativeOrder()) {
    writeInt(value.toInt(), byteOrder)
}

public inline fun ByteSink.writeULong(value: ULong, byteOrder: ByteOrder = nativeOrder()) {
    writeLong(value.toLong(), byteOrder)
}

public inline fun ByteSink.writeFloat(value: Float, byteOrder: ByteOrder = nativeOrder()) {
    writeInt(value.toRawBits(), byteOrder)
}

public inline fun ByteSink.writeDouble(value: Double, byteOrder: ByteOrder = nativeOrder()) {
    writeLong(value.toRawBits(), byteOrder)
}
