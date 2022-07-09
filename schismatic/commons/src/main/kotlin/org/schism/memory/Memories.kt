package org.schism.memory

import org.schism.math.timesExact
import org.schism.ref.registerCleanup
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

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
    return NativeMemory(startAddress, size, NativeMemory.READABLE or NativeMemory.WRITABLE).apply {
        registerCleanup(cleanup)
    }
}

public fun allocateNativeMemory(size: Long): Memory {
    val startAddress = malloc(size)
    return nativeMemory(startAddress, size) { free(startAddress) }
}

@OptIn(ExperimentalContracts::class)
public inline fun <R> withNativeMemory(size: Long, block: (Memory) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    val startAddress = malloc(size)
    try {
        return block(nativeMemory(startAddress, size))
    } finally {
        free(startAddress)
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun <R> withNativeBytes(size: Long, block: (MemoryByteArrayView) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return withNativeMemory(size) {
        block(it.bytes)
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun <R> withNativeUBytes(size: Long, block: (MemoryUByteArrayView) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return withNativeMemory(size) {
        block(it.ubytes)
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun <R> withNativeChars(size: Long, block: (MemoryCharArrayView) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return withNativeMemory(size timesExact 2) {
        block(it.chars)
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun <R> withNativeShorts(size: Long, block: (MemoryShortArrayView) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return withNativeMemory(size timesExact 2) {
        block(it.shorts)
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun <R> withNativeUShorts(size: Long, block: (MemoryUShortArrayView) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return withNativeMemory(size timesExact 2) {
        block(it.ushorts)
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun <R> withNativeInts(size: Long, block: (MemoryIntArrayView) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return withNativeMemory(size timesExact 4) {
        block(it.ints)
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun <R> withNativeUInts(size: Long, block: (MemoryUIntArrayView) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return withNativeMemory(size timesExact 4) {
        block(it.uints)
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun <R> withNativeLongs(size: Long, block: (MemoryLongArrayView) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return withNativeMemory(size timesExact 8) {
        block(it.longs)
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun <R> withNativeULongs(size: Long, block: (MemoryULongArrayView) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return withNativeMemory(size timesExact 8) {
        block(it.ulongs)
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun <R> withNativeFloats(size: Long, block: (MemoryFloatArrayView) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return withNativeMemory(size timesExact 4) {
        block(it.floats)
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun <R> withNativeDoubles(size: Long, block: (MemoryDoubleArrayView) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return withNativeMemory(size timesExact 8) {
        block(it.doubles)
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun <R> withNativePointers(size: Long, block: (MemoryPointerArrayView) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return withNativeMemory(size timesExact NativeAddress.BYTE_SIZE) {
        block(it.pointers)
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun <R> withNativeByte(block: (MemoryByteView) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return withNativeMemory(1) {
        block(it.byte)
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun <R> withNativeUByte(block: (MemoryUByteView) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return withNativeMemory(1) {
        block(it.ubyte)
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun <R> withNativeChar(block: (MemoryCharView) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return withNativeMemory(2) {
        block(it.char)
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun <R> withNativeShort(block: (MemoryShortView) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return withNativeMemory(2) {
        block(it.short)
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun <R> withNativeUShort(block: (MemoryUShortView) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return withNativeMemory(2) {
        block(it.ushort)
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun <R> withNativeInt(block: (MemoryIntView) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return withNativeMemory(4) {
        block(it.int)
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun <R> withNativeUInt(block: (MemoryUIntView) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return withNativeMemory(4) {
        block(it.uint)
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun <R> withNativeLong(block: (MemoryLongView) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return withNativeMemory(8) {
        block(it.long)
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun <R> withNativeULong(block: (MemoryULongView) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return withNativeMemory(8) {
        block(it.ulong)
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun <R> withNativeFloat(block: (MemoryFloatView) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return withNativeMemory(4) {
        block(it.float)
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun <R> withNativeDouble(block: (MemoryDoubleView) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return withNativeMemory(8) {
        block(it.double)
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun <R> withNativePointer(block: (MemoryPointerView) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return withNativeMemory(NativeAddress.BYTE_SIZE.toLong()) {
        block(it.pointer)
    }
}