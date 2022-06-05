package org.schism.bytes

import java.lang.Math.multiplyExact
import java.lang.foreign.MemoryAddress
import java.lang.foreign.MemoryLayout
import java.lang.foreign.MemorySegment
import java.lang.foreign.MemorySession
import java.lang.foreign.SequenceLayout
import java.nio.Buffer
import java.util.Objects.checkIndex

public fun MemorySegment.dropFirst(byteCount: Long): MemorySegment {
    checkIndex(byteCount, byteSize())
    return asSlice(byteCount)
}

public fun MemorySegment.dropLast(byteCount: Long): MemorySegment {
    checkIndex(byteCount, byteSize())
    return asSlice(0, byteSize() - byteCount)
}

public fun MemorySegment.takeFirst(byteCount: Long): MemorySegment {
    checkIndex(byteCount, byteSize())
    return asSlice(0, byteCount)
}

public fun MemorySegment.takeLast(byteCount: Long): MemorySegment {
    checkIndex(byteCount, byteSize())
    return asSlice(byteSize() - byteCount)
}

public fun MemorySegment.dropFirstAtMost(byteCount: Long): MemorySegment {
    require(byteCount >= 0)
    return asSlice(minOf(byteCount, byteSize()))
}

public fun MemorySegment.dropLastAtMost(byteCount: Long): MemorySegment {
    require(byteCount >= 0)
    return asSlice(0, maxOf(byteSize() - byteCount, 0))
}

public fun MemorySegment.takeFirstAtMost(byteCount: Long): MemorySegment {
    require(byteCount >= 0)
    return asSlice(0, minOf(byteCount, byteSize()))
}

public fun MemorySegment.takeLastAtMost(byteCount: Long): MemorySegment {
    require(byteCount >= 0)
    return asSlice(maxOf(byteSize() - byteCount, 0))
}

public fun Buffer.asMemorySegment(): MemorySegment {
    return MemorySegment.ofBuffer(this)
}

public fun ByteArray.asMemorySegment(): MemorySegment {
    return MemorySegment.ofArray(this)
}

public fun CharArray.asMemorySegment(): MemorySegment {
    return MemorySegment.ofArray(this)
}

public fun ShortArray.asMemorySegment(): MemorySegment {
    return MemorySegment.ofArray(this)
}

public fun IntArray.asMemorySegment(): MemorySegment {
    return MemorySegment.ofArray(this)
}

public fun LongArray.asMemorySegment(): MemorySegment {
    return MemorySegment.ofArray(this)
}

public fun FloatArray.asMemorySegment(): MemorySegment {
    return MemorySegment.ofArray(this)
}

public fun DoubleArray.asMemorySegment(): MemorySegment {
    return MemorySegment.ofArray(this)
}

@ExperimentalUnsignedTypes
public fun UByteArray.asMemorySegment(): MemorySegment {
    return asByteArray().asMemorySegment()
}

@ExperimentalUnsignedTypes
public fun UShortArray.asMemorySegment(): MemorySegment {
    return asShortArray().asMemorySegment()
}

@ExperimentalUnsignedTypes
public fun UIntArray.asMemorySegment(): MemorySegment {
    return asIntArray().asMemorySegment()
}

@ExperimentalUnsignedTypes
public fun ULongArray.asMemorySegment(): MemorySegment {
    return asLongArray().asMemorySegment()
}

public fun MemoryLayout.segment(
    address: MemoryAddress,
    session: MemorySession = globalMemorySession(),
): MemorySegment {
    address.requireAlignedTo(byteAlignment())
    return MemorySegment.ofAddress(address, byteSize(), session)
}

public fun SequenceLayout.elementSegment(
    baseAddress: MemoryAddress,
    index: Long,
    session: MemorySession = globalMemorySession(),
): MemorySegment {
    checkIndex(index, elementCount())
    baseAddress.requireAlignedTo(elementLayout().byteAlignment())
    return elementLayout().segment(baseAddress + multiplyExact(elementLayout().byteSize(), index), session)
}

public fun SequenceLayout.elementSegmentList(
    baseAddress: MemoryAddress,
    session: MemorySession = globalMemorySession(),
): List<MemorySegment> {
    require(elementCount() <= Int.MAX_VALUE) {
        "element count (${elementCount()}) does not fit in Int"
    }

    baseAddress.requireAlignedTo(elementLayout().byteAlignment())

    return object : AbstractList<MemorySegment>() {
        override val size: Int get() =
            elementCount().toInt()

        override fun get(index: Int): MemorySegment =
            elementSegment(baseAddress, index.toLong(), session)
    }
}
