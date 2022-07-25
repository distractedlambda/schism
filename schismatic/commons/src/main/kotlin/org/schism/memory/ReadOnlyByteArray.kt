package org.schism.memory

import java.lang.foreign.MemoryAddress
import java.lang.foreign.MemorySegment
import java.lang.foreign.MemorySession
import java.util.Objects
import java.util.Objects.checkFromToIndex

@JvmInline public value class ReadOnlyByteArray(private val mutableArray: ByteArray) {
    public val size: Int get() {
        return mutableArray.size
    }

    public operator fun get(index: Int): Byte {
        return mutableArray[index]
    }

    public fun copyInto(destination: ByteArray, destinationOffset: Int = 0, startIndex: Int = 0, endIndex: Int = size) {
        mutableArray.copyInto(destination, destinationOffset, startIndex, endIndex)
    }

    public fun copyInto(destination: NativeAddress, startIndex: Int = 0, endIndex: Int = size) {
        checkFromToIndex(startIndex, endIndex, size)
    }

    public operator fun iterator(): ByteIterator {
        return mutableArray.iterator()
    }
}
