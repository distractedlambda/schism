package org.schism.bytes

import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout.JAVA_BYTE
import java.lang.foreign.ValueLayout.JAVA_INT
import java.lang.foreign.ValueLayout.JAVA_LONG
import java.lang.foreign.ValueLayout.JAVA_SHORT
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Objects.checkFromIndexSize

public fun byteSinkInto(segment: MemorySegment): MeteredByteSink = object : MeteredByteSink {
    public override var countWritten: Long = 0L
        private set

    override suspend fun writeByte(byte: Byte) {
        segment.set(JAVA_BYTE, countWritten, byte)
        countWritten += 1
    }

    override suspend fun writeShort(value: Short, byteOrder: ByteOrder) {
        segment.set(JAVA_SHORT.withOrder(byteOrder).withBitAlignment(8), countWritten, value)
        countWritten += 2
    }

    override suspend fun writeInt(value: Int, byteOrder: ByteOrder) {
        segment.set(JAVA_INT.withOrder(byteOrder).withBitAlignment(8), countWritten, value)
        countWritten += 4
    }

    override suspend fun writeLong(value: Long, byteOrder: ByteOrder) {
        segment.set(JAVA_LONG.withOrder(byteOrder).withBitAlignment(8), countWritten, value)
        countWritten += 8
    }

    override suspend fun writeZeros(byteCount: Long) {
        segment.asSlice(countWritten, byteCount).fill(0)
        countWritten += byteCount
    }

    override suspend fun skip(byteCount: Long) {
        checkFromIndexSize(countWritten, byteCount, segment.byteSize())
        countWritten += byteCount
    }

    private fun writeWithoutSuspending(bytes: MemorySegment) {
        segment.dropFirst(countWritten).copyFrom(bytes)
        countWritten += bytes.byteSize()
    }

    override suspend fun writeBytes(bytes: MemorySegment) {
        writeWithoutSuspending(bytes)
    }

    override suspend fun writeBytes(bytes: ByteBuffer) {
        writeWithoutSuspending(bytes.asMemorySegment())
        bytes.position(bytes.limit())
    }

    override suspend fun writeBytes(bytes: ByteArray, offset: Int, size: Int) {
        writeWithoutSuspending(bytes.asMemorySegment().asSlice(offset.toLong(), size.toLong()))
    }
}
