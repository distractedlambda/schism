package org.schism.coroutines

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

public class Actor(scope: CoroutineScope) {
    private val workItems = Channel(Channel.UNLIMITED, onUndeliveredElement = WorkItem<*>::cancel)

    @OptIn(ExperimentalContracts::class)
    public suspend fun <T> within(block: suspend CoroutineScope.() -> T): T {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }

        return suspendCancellableCoroutine { continuation ->
            workItems.trySend(WorkItem(continuation, block)).onFailure { cause ->
                continuation.cancel(cause)
            }
        }
    }

    init {
        scope.launch {
            workItems.consumeEach {
                // FIXME: Is there a risk that [coroutineScope] will poll cancellation early?
                coroutineScope(it)
            }
        }
    }

    private class WorkItem<T>(
        private val continuation: CancellableContinuation<T>,
        private val work: suspend CoroutineScope.() -> T,
    ) : suspend CoroutineScope.() -> Unit {
        override suspend fun invoke(scope: CoroutineScope) {
            if (continuation.isActive) {
                continuation.resumeWith(scope.runCatching { work() })
            }
        }

        fun cancel() {
            continuation.cancel()
        }
    }
}

public fun CoroutineScope.Actor(): Actor {
    return Actor(this)
}
