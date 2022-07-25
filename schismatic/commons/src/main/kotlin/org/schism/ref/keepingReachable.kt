package org.schism.ref

import java.lang.ref.Reference.reachabilityFence
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
public inline fun <R> keepingReachable(value: Any?, block: () -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    try {
        return block()
    } finally {
        reachabilityFence(value)
    }
}
