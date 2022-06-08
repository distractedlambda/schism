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

    override fun writeByte(byte: Byte) {
        segment.set(JAVA_BYTE, countWritten, byte)
        countWritten += 1
    }

    override fun writeShort(value: Short, byteOrder: ByteOrder) {
        segment.set(JAVA_SHORT.withOrder(byteOrder).withBitAlignment(8), countWritten, value)
        countWritten += 2
    }

    override fun writeInt(value: Int, byteOrder: ByteOrder) {
        segment.set(JAVA_INT.withOrder(byteOrder).withBitAlignment(8), countWritten, value)
        countWritten += 4
    }

    override fun writeLong(value: Long, byteOrder: ByteOrder) {
        segment.set(JAVA_LONG.withOrder(byteOrder).withBitAlignment(8), countWritten, value)
        countWritten += 8
    }

    override fun writeZeros(byteCount: Long) {
        segment.asSlice(countWritten, byteCount).fill(0)
        countWritten += byteCount
    }

    override fun skip(byteCount: Long) {
        checkFromIndexSize(countWritten, byteCount, segment.byteSize())
        countWritten += byteCount
    }

    override fun writeBytes(bytes: MemorySegment) {
        segment.dropFirst(countWritten).copyFrom(bytes)
        countWritten += bytes.byteSize()
    }

    override fun writeBytes(bytes: ByteBuffer) {
        writeBytes(bytes.asMemorySegment())
        bytes.position(bytes.limit())
    }

    override fun writeBytes(bytes: ByteArray, offset: Int, size: Int) {
        writeBytes(bytes.asMemorySegment().asSlice(offset.toLong(), size.toLong()))
    }
}
