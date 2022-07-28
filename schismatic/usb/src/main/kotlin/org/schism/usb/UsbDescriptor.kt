package org.schism.usb

import org.schism.foreign.allocateHeapSegment
import org.schism.foreign.asMemorySegment
import org.schism.foreign.decoder
import java.lang.foreign.MemoryAddress
import java.lang.foreign.MemorySegment

public data class UsbDescriptor(val descriptorType: UByte, val contents: MemorySegment)

internal fun parseExtraDescriptors(extra: MemoryAddress, extraLength: Int): List<UsbDescriptor> {
    return buildList {
        extra.asMemorySegment(extraLength.toLong()).decoder().run {
            while (hasRemaining) {
                val length = nextUByte() - 2u
                val descriptorType = nextUByte()
                val contents = nextBytes(allocateHeapSegment(length.toLong())).asReadOnly()
                add(UsbDescriptor(descriptorType, contents))
            }
        }
    }
}
