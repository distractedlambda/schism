package org.schism.cousb

import java.lang.foreign.MemorySession
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
internal inline fun <T> MemorySession.withAlive(crossinline block: () -> T): T {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    val result: T
    whileAliveGuaranteed { result = block() }
    return result
}

@OptIn(ExperimentalContracts::class)
internal inline fun MemorySession.whileAliveGuaranteed(crossinline block: () -> Unit) {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    whileAlive { block() }
}
