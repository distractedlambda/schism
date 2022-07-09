package org.schism.usb

import org.schism.ffi.CInt
import org.schism.memory.Memory
import org.schism.memory.NativeAddress
import org.schism.memory.allocateHeapMemory
import org.schism.memory.nativeMemory
import org.schism.memory.nextUByte

public data class UsbDescriptor(val descriptorType: UByte, val contents: Memory) {
    public companion object
}

internal fun parseExtraDescriptors(extra: NativeAddress, extraLength: CInt): List<UsbDescriptor> {
    return buildList {
        nativeMemory(extra, extraLength.toLong()).decoder().run {
            while (hasRemaining()) {
                val length = nextUByte()
                val descriptorType = nextUByte()
                val contents = allocateHeapMemory(length.toInt()).also(::nextBytes).asReadOnly()
                add(UsbDescriptor(descriptorType, contents))
            }
        }
    }
}
