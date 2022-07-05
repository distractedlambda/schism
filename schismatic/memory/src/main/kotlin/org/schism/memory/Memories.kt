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

public inline fun <R> withNativeMemory(size: Long, block: (Memory) -> R): R {
    val startAddress = malloc(size)
    try {
        return block(nativeMemory(startAddress, size))
    } finally {
        free(startAddress)
    }
}

public inline fun <R> withNativeByte(block: (MemoryByteView) -> R): R {
    return withNativeMemory(1) {
        block(it.byte)
    }
}

public inline fun <R> withNativeUByte(block: (MemoryUByteView) -> R): R {
    return withNativeMemory(1) {
        block(it.ubyte)
    }
}

public inline fun <R> withNativeChar(block: (MemoryCharView) -> R): R {
    return withNativeMemory(2) {
        block(it.char)
    }
}

public inline fun <R> withNativeShort(block: (MemoryShortView) -> R): R {
    return withNativeMemory(2) {
        block(it.short)
    }
}

public inline fun <R> withNativeUShort(block: (MemoryUShortView) -> R): R {
    return withNativeMemory(2) {
        block(it.ushort)
    }
}

public inline fun <R> withNativeInt(block: (MemoryIntView) -> R): R {
    return withNativeMemory(4) {
        block(it.int)
    }
}

public inline fun <R> withNativeUInt(block: (MemoryUIntView) -> R): R {
    return withNativeMemory(4) {
        block(it.uint)
    }
}

public inline fun <R> withNativeLong(block: (MemoryLongView) -> R): R {
    return withNativeMemory(8) {
        block(it.long)
    }
}

public inline fun <R> withNativeULong(block: (MemoryULongView) -> R): R {
    return withNativeMemory(8) {
        block(it.ulong)
    }
}

public inline fun <R> withNativeFloat(block: (MemoryFloatView) -> R): R {
    return withNativeMemory(4) {
        block(it.float)
    }
}

public inline fun <R> withNativeDouble(block: (MemoryDoubleView) -> R): R {
    return withNativeMemory(8) {
        block(it.double)
    }
}

public inline fun <R> withNativePointer(block: (MemoryPointerView) -> R): R {
    return withNativeMemory(NativeAddress.BYTE_SIZE.toLong()) {
        block(it.pointer)
    }
}

private val CLEANER = Cleaner.create()
