package org.schism.foreign

import org.schism.math.timesExact
import java.lang.foreign.MemoryAddress
import java.lang.foreign.MemorySegment
import java.lang.foreign.MemorySession
import java.lang.foreign.SegmentAllocator

public class StructArray<S : Struct> internal constructor(
    public val elementType: StructType<S>,
    public val segment: MemorySegment,
) : Iterable<S> {
    init {
        require(segment.byteSize() % elementType.layout.byteSize() == 0L)
        segment.requireAlignedFor(elementType.layout)
    }

    public val size: Long get() {
        return segment.byteSize() / elementType.layout.byteSize()
    }

    public val indices: LongRange get() {
        return 0 until size
    }

    public operator fun get(index: Long): S {
        return segment
            .asSlice(index timesExact elementType.layout.byteSize(), elementType.layout.byteSize())
            .asStruct(elementType)
    }

    override fun iterator(): Iterator<S> {
        return object : Iterator<S> {
            private var index = 0L

            override fun hasNext(): Boolean {
                return index < size
            }

            override fun next(): S {
                return get(index++)
            }
        }
    }
}

public fun <S : Struct> MemorySegment.asStructArray(elementType: StructType<S>): StructArray<S> {
    return StructArray(elementType, this)
}

public fun <S : Struct> MemoryAddress.asStructArray(
    elementType: StructType<S>,
    count: Long,
    session: MemorySession,
): StructArray<S> {
    return asMemorySegment(elementType.layout.byteSize() timesExact count, session).asStructArray(elementType)
}

public fun <S : Struct> SegmentAllocator.allocateArray(elementType: StructType<S>, count: Long): StructArray<S> {
    return allocateArray(elementType.layout, count).asStructArray(elementType)
}
