package org.schism.ffi

import org.schism.math.timesExact
import org.schism.memory.Memory
import org.schism.memory.NativeAddress
import org.schism.memory.allocateNativeMemory
import org.schism.memory.nativeMemory
import org.schism.memory.requireAlignedTo
import org.schism.memory.withNativeMemory
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

public fun <S : Struct> StructType<S>.wrapArray(memory: Memory): StructArray<S> {
    memory.requireAlignedTo(this.alignment)
    return StructArray(this, memory.slice(size = memory.size / this.size * this.size))
}

public fun <S : Struct> StructType<S>.wrapArray(startAddress: NativeAddress, size: Long): StructArray<S> {
    return wrapArray(nativeMemory(startAddress, size timesExact this.size))
}

public fun <S : Struct> allocateNativeStructArray(elementType: StructType<S>, size: Long): StructArray<S> {
    return elementType.wrapArray(allocateNativeMemory(size timesExact elementType.size))
}

@OptIn(ExperimentalContracts::class)
public inline fun <S : Struct, R> withNativeStructArray(
    elementType: StructType<S>,
    size: Long,
    block: (StructArray<S>) -> R,
): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return withNativeMemory(size timesExact elementType.size) {
        block(elementType.wrapArray(it))
    }
}

public class StructArray<S : Struct> internal constructor(
    public val elementType: StructType<S>,
    public val memory: Memory,
) {
    public val startAddress: NativeAddress get() {
        return memory.startAddress
    }

    public val isNative: Boolean get() {
        return memory.isNative
    }

    public val isReadable: Boolean get() {
        return memory.isReadable
    }

    public val isWritable: Boolean get() {
        return memory.isWritable
    }

    public val size: Long get() {
        return memory.size / elementType.size
    }

    public val indices: LongRange get() {
        return 0 until size
    }

    public operator fun get(index: Long): S {
        return elementType.wrap(memory.slice(index timesExact elementType.size, elementType.size))
    }
}
