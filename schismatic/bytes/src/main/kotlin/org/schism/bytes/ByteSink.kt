package org.schism.bytes

import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout
import java.nio.Buffer

public interface ByteSink {
    public fun write(byte: Byte)

    public fun write(bytes: MemorySegment) {
        for (i in 0 until bytes.byteSize()) {
            write(bytes[ValueLayout.JAVA_BYTE, i])
        }
    }

    public fun write(bytes: ByteArray, offset: Int = 0, size: Int = bytes.size - offset) {
        write(MemorySegment.ofArray(bytes).asSlice(offset.toLong(), size.toLong()))
    }

    public fun write(bytes: Buffer) {
        write(MemorySegment.ofBuffer(bytes))
        bytes.position(bytes.limit())
    }
}
