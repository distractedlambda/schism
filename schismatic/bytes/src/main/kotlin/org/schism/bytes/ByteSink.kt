package org.schism.bytes

import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.ByteOrder.LITTLE_ENDIAN
import java.nio.ByteOrder.nativeOrder

public interface ByteSink {
    public suspend fun writeByte(byte: Byte)

    public suspend fun writeZeros(byteCount: Long) {
        require(byteCount >= 0)
        for (i in 0 until byteCount) {
            writeByte(0.toByte())
        }
    }

    public suspend fun skip(byteCount: Long) {
        writeZeros(byteCount)
    }

    public suspend fun writeBytes(bytes: MemorySegment) {
        for (i in 0 until bytes.byteSize()) {
            writeByte(bytes[ValueLayout.JAVA_BYTE, i])
        }
    }

    public suspend fun writeBytes(bytes: ByteArray, offset: Int = 0, size: Int = bytes.size - offset) {
        writeBytes(MemorySegment.ofArray(bytes).asSlice(offset.toLong(), size.toLong()))
    }

    public suspend fun writeBytes(bytes: ByteBuffer) {
        writeBytes(MemorySegment.ofBuffer(bytes))
        bytes.position(bytes.limit())
    }

    public suspend fun writeShort(value: Short, byteOrder: ByteOrder = nativeOrder()) {
        val adaptedValue = if (byteOrder == LITTLE_ENDIAN) value else java.lang.Short.reverseBytes(value)
        writeByte(adaptedValue.toByte())
        writeByte((adaptedValue.toInt() shr 8).toByte())
    }

    public suspend fun writeInt(value: Int, byteOrder: ByteOrder = nativeOrder()) {
        val adaptedValue = if (byteOrder == LITTLE_ENDIAN) value else Integer.reverseBytes(value)
        writeByte(adaptedValue.toByte())
        writeByte((adaptedValue shr 8).toByte())
        writeByte((adaptedValue shr 16).toByte())
        writeByte((adaptedValue shr 24).toByte())
    }

    public suspend fun writeLong(value: Long, byteOrder: ByteOrder = nativeOrder()) {
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

public suspend inline fun ByteSink.writeUByte(value: UByte) {
    writeByte(value.toByte())
}

public suspend inline fun ByteSink.writeChar(value: Char, byteOrder: ByteOrder = nativeOrder()) {
    writeShort(value.code.toShort(), byteOrder)
}

public suspend inline fun ByteSink.writeUShort(value: UShort, byteOrder: ByteOrder = nativeOrder()) {
    writeShort(value.toShort(), byteOrder)
}

public suspend inline fun ByteSink.writeUInt(value: UInt, byteOrder: ByteOrder = nativeOrder()) {
    writeInt(value.toInt(), byteOrder)
}

public suspend inline fun ByteSink.writeULong(value: ULong, byteOrder: ByteOrder = nativeOrder()) {
    writeLong(value.toLong(), byteOrder)
}

public suspend inline fun ByteSink.writeFloat(value: Float, byteOrder: ByteOrder = nativeOrder()) {
    writeInt(value.toRawBits(), byteOrder)
}

public suspend inline fun ByteSink.writeDouble(value: Double, byteOrder: ByteOrder = nativeOrder()) {
    writeLong(value.toRawBits(), byteOrder)
}
