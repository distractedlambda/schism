package org.schism.coroutines

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind.EXACTLY_ONCE
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
public suspend inline fun <R> inVirtualThread(crossinline block: () -> R): R {
    contract {
        callsInPlace(block, EXACTLY_ONCE)
    }

    return suspendCancellableCoroutine { continuation ->
        val thread = Thread.ofVirtual().unstarted {
            continuation.resumeWith(runCatching(block))
        }

        continuation.invokeOnCancellation {
            thread.interrupt()
        }

        thread.start()
    }
}
