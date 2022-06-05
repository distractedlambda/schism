package org.schism.bytes

import java.lang.foreign.MemorySegment
import java.nio.ByteBuffer
import java.nio.ByteOrder

public interface MeteredByteSink : ByteSink {
    public val countWritten: Long
}

public fun ByteSink.metered(): MeteredByteSink = object : MeteredByteSink {
    override var countWritten: Long = 0L
        private set

    private fun accumulateWritten(count: Long) {
        countWritten = Math.addExact(countWritten, count)
    }

    override suspend fun writeByte(byte: Byte) {
        this@metered.writeByte(byte)
        accumulateWritten(1)
    }

    override suspend fun writeShort(value: Short, byteOrder: ByteOrder) {
        this@metered.writeShort(value, byteOrder)
        accumulateWritten(2)
    }

    override suspend fun writeInt(value: Int, byteOrder: ByteOrder) {
        this@metered.writeInt(value, byteOrder)
        accumulateWritten(4)
    }

    override suspend fun writeLong(value: Long, byteOrder: ByteOrder) {
        this@metered.writeLong(value, byteOrder)
        accumulateWritten(8)
    }

    override suspend fun skip(byteCount: Long) {
        this@metered.skip(byteCount)
        accumulateWritten(byteCount)
    }

    override suspend fun writeZeros(byteCount: Long) {
        this@metered.writeZeros(byteCount)
        accumulateWritten(byteCount)
    }

    override suspend fun writeBytes(bytes: MemorySegment) {
        this@metered.writeBytes(bytes)
        accumulateWritten(bytes.byteSize())
    }

    override suspend fun writeBytes(bytes: ByteBuffer) {
        val sourceRemaining = bytes.remaining()
        this@metered.writeBytes(bytes)
        accumulateWritten(sourceRemaining.toLong())
    }

    override suspend fun writeBytes(bytes: ByteArray, offset: Int, size: Int) {
        this@metered.writeBytes(bytes, offset, size)
        accumulateWritten(size.toLong())
    }
}
