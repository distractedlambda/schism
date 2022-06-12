package org.schism.bytes

import java.lang.foreign.MemorySegment
import java.nio.ByteBuffer
import java.nio.ByteOrder

object NullByteSink : ByteSink {
    override fun writeByte(byte: Byte) {}

    override fun writeZeros(byteCount: Long) {}

    override fun skip(byteCount: Long) {}

    override fun writeBytes(bytes: MemorySegment) {}

    override fun writeBytes(bytes: ByteBuffer) {}

    override fun writeBytes(bytes: ByteArray, offset: Int, size: Int) {}

    override fun writeShort(value: Short, byteOrder: ByteOrder) {}

    override fun writeInt(value: Int, byteOrder: ByteOrder) {}

    override fun writeLong(value: Long, byteOrder: ByteOrder) {}
}
