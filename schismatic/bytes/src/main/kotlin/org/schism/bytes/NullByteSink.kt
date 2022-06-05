package org.schism.bytes

import java.lang.foreign.MemorySegment
import java.nio.ByteBuffer
import java.nio.ByteOrder

public object NullByteSink : ByteSink {
    override suspend fun writeByte(byte: Byte) {}

    override suspend fun writeZeros(byteCount: Long) {}

    override suspend fun skip(byteCount: Long) {}

    override suspend fun writeBytes(bytes: MemorySegment) {}

    override suspend fun writeBytes(bytes: ByteBuffer) {}

    override suspend fun writeBytes(bytes: ByteArray, offset: Int, size: Int) {}

    override suspend fun writeShort(value: Short, byteOrder: ByteOrder) {}

    override suspend fun writeInt(value: Int, byteOrder: ByteOrder) {}

    override suspend fun writeLong(value: Long, byteOrder: ByteOrder) {}
}
