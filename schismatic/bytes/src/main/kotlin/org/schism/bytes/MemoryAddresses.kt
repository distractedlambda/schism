package org.schism.bytes

import java.lang.Math.negateExact
import java.lang.Math.subtractExact
import java.lang.foreign.MemoryAddress
import java.lang.foreign.MemorySegment
import java.lang.foreign.MemorySession

public operator fun MemoryAddress.plus(offset: Long): MemoryAddress {
    return addOffset(offset)
}

public operator fun MemoryAddress.minus(offset: Long): MemoryAddress {
    return addOffset(negateExact(offset))
}

public operator fun MemoryAddress.minus(other: MemoryAddress): Long {
    return subtractExact(toRawLongValue(), other.toRawLongValue())
}

public fun MemoryAddress.requireAlignedTo(alignment: Long) {
    toRawLongValue().requireAlignedTo(alignment)
}

public fun MemoryAddress.asSegment(byteSize: Long, session: MemorySession = globalMemorySession()): MemorySegment {
    return MemorySegment.ofAddress(this, byteSize, session)
}

public fun MemoryAddress.copyFrom(source: MemorySegment) {
    asSegment(source.byteSize()).copyFrom(source)
}

public fun MemoryAddress.copyFrom(source: MemoryAddress, byteCount: Long) {
    asSegment(byteCount).copyFrom(source.asSegment(byteCount))
}
