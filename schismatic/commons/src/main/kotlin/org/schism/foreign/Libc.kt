package org.schism.foreign

import java.lang.foreign.Addressable
import java.lang.foreign.Linker
import java.lang.foreign.MemoryAddress

private interface Libc {
    fun malloc(size: @CSSizeT ULong): MemoryAddress

    fun free(memory: Addressable)
}

private val libc = linkNativeLibrary<Libc>(Linker.nativeLinker().defaultLookup())

public fun malloc(size: Long): MemoryAddress {
    require(size > 0)
    return libc.malloc(size.toULong()).nonNULLOrElse {
        throw OutOfMemoryError("malloc() returned NULL")
    }
}

public fun free(address: MemoryAddress) {
    if (address.isNotNULL()) {
        libc.free(address)
    }
}
