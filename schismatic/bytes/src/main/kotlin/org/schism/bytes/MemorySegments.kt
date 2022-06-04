package org.schism.bytes

import java.lang.Math.multiplyExact
import java.lang.foreign.MemoryAddress
import java.lang.foreign.MemoryLayout
import java.lang.foreign.MemorySegment
import java.lang.foreign.MemorySession
import java.lang.foreign.SequenceLayout
import java.util.Objects.checkIndex

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
