package org.schism.foreign

import java.lang.foreign.MemorySession
import java.lang.ref.Cleaner
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

public fun globalMemorySession(): MemorySession {
    return MemorySession.global()
}

public fun confinedMemorySession(cleaner: Cleaner? = null): MemorySession {
    return when (cleaner) {
        null -> MemorySession.openConfined()
        else -> MemorySession.openConfined(cleaner)
    }
}

public fun sharedMemorySession(cleaner: Cleaner? = null): MemorySession {
    return when (cleaner) {
        null -> MemorySession.openShared()
        else -> MemorySession.openShared(cleaner)
    }
}

public fun implicitMemorySession(): MemorySession {
    return MemorySession.openImplicit()
}

@OptIn(ExperimentalContracts::class)
public inline fun <R> withConfinedMemorySession(cleaner: Cleaner? = null, block: MemorySession.() -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return confinedMemorySession(cleaner).use(block)
}

@OptIn(ExperimentalContracts::class)
public inline fun <R> withSharedMemorySession(cleaner: Cleaner? = null, block: MemorySession.() -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return sharedMemorySession(cleaner).use(block)
}
