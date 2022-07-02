package org.schism.memory

import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker
import java.lang.foreign.MemoryAddress
import java.lang.foreign.ValueLayout

public fun malloc(size: Long): NativeAddress {
    require(size > 0)

    val address = NativeAddress((NATIVE_MALLOC(size) as MemoryAddress).toRawLongValue())

    if (address.isNULL()) {
        throw OutOfMemoryError()
    }

    return address
}

public fun free(address: NativeAddress) {
    NATIVE_FREE(address.toMemoryAddress())
}

private val NATIVE_MALLOC = Linker.nativeLinker().downcallHandle(
    Linker.nativeLinker().defaultLookup().lookup("malloc").orElseThrow(),
    FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.JAVA_LONG), // FIXME: handle 32-bit?
)

private val NATIVE_FREE = Linker.nativeLinker().downcallHandle(
    Linker.nativeLinker().defaultLookup().lookup("free").orElseThrow(),
    FunctionDescriptor.ofVoid(ValueLayout.ADDRESS),
)
