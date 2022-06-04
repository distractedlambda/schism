package org.schism.bytes

import org.schism.coroutines.inVirtualThread
import java.io.OutputStream
import java.lang.Thread.onSpinWait
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout.JAVA_BYTE
import java.nio.ByteBuffer
import java.nio.channels.WritableByteChannel

public fun OutputStream.asByteSink(): ByteSink = object : ByteSink {
    override suspend fun write(byte: Byte) = inVirtualThread {
        this@asByteSink.write(byte.toInt())
    }

    override suspend fun write(bytes: ByteArray, offset: Int, size: Int) = inVirtualThread {
        this@asByteSink.write(bytes, offset, size)
    }

    override suspend fun write(bytes: MemorySegment) = inVirtualThread {
        for (i in 0 until bytes.byteSize()) {
            this@asByteSink.write(bytes[JAVA_BYTE, i].toInt())
        }
    }

    override suspend fun write(bytes: ByteBuffer): Unit = inVirtualThread {
        if (bytes.hasArray()) {
            this@asByteSink.write(bytes.array(), bytes.arrayOffset(), bytes.remaining())
        } else {
            val segment = MemorySegment.ofBuffer(bytes)
            for (i in 0 until segment.byteSize()) {
                this@asByteSink.write(segment[JAVA_BYTE, i].toInt())
            }
        }

        bytes.position(bytes.limit())
    }
}

public fun WritableByteChannel.asByteSink(): ByteSink = object : ByteSink {
    override suspend fun write(byte: Byte) = inVirtualThread {
        val buffer = ByteBuffer.allocate(1)
        buffer.put(0, byte)
        while (this@asByteSink.write(buffer) != 1) {
            onSpinWait()
        }
    }

    override suspend fun write(bytes: ByteArray, offset: Int, size: Int) = inVirtualThread {
        val buffer = ByteBuffer.wrap(bytes, offset, size)
        this@asByteSink.write(buffer)
        while (buffer.hasRemaining()) {
            onSpinWait()
            this@asByteSink.write(buffer)
        }
    }
}
