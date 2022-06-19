package org.schism.collections

import org.schism.foreign.BufferDecoder
import org.schism.foreign.NativeBuffer
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout.JAVA_BYTE
import java.nio.ByteBuffer

@JvmInline value class ImmutableByteArray private constructor(private inline val array: ByteArray) {
    constructor(contents: ByteArray, offset: Int = 0, length: Int = contents.size - offset)
        : this(contents.copyOfRange(offset, offset + length))

    constructor(contents: ByteBuffer)
        : this(ByteArray(contents.remaining()).also { contents.get(it) })

    constructor(contents: MemorySegment)
        : this(contents.toArray(JAVA_BYTE))

    constructor(contents: NativeBuffer)
        : this(contents.keepAlive { contents.asDanglingSegment().toArray(JAVA_BYTE) })

    val size get() = array.size

    operator fun get(index: Int): Byte {
        return array[index]
    }

    fun decoder(startIndex: Int = 0, endIndex: Int = size): BufferDecoder {
        return array.decoder(startIndex, endIndex)
    }
}
