package org.schism.memory

import java.lang.ref.Cleaner

public fun heapMemory(array: ByteArray, offset: Int = 0, size: Int = array.size - offset): Memory {
    return HeapMemory(array, offset, size, HeapMemory.READABLE or HeapMemory.WRITABLE)
}

public fun allocateHeapMemory(size: Int): Memory {
    return heapMemory(ByteArray(size))
}

public fun nativeMemory(startAddress: NativeAddress, size: Long, attachment: Any? = null): Memory {
    return NativeMemory(startAddress, size, NativeMemory.READABLE or NativeMemory.WRITABLE, attachment)
}

public fun nativeMemory(startAddress: NativeAddress, size: Long, cleanup: Runnable): Memory {
    return NativeMemory(startAddress, size, NativeMemory.READABLE or NativeMemory.WRITABLE).also {
        CLEANER.register(it, cleanup)
    }
}

public fun allocateNativeMemory(size: Long): Memory {
    val startAddress = malloc(size)
    return nativeMemory(startAddress, size) { free(startAddress) }
}

public inline fun <R> withAllocatedNativeMemory(size: Long, block: (Memory) -> R): R {
    val startAddress = malloc(size)
    try {
        return block(nativeMemory(startAddress, size))
    } finally {
        free(startAddress)
    }
}

private val CLEANER = Cleaner.create()
