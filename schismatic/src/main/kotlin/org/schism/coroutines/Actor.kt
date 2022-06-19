package org.schism.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlin.Result.Companion.failure
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.Continuation
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

context (CoroutineScope) open class Actor {
    private val workItems = Channel<WorkItem<*>>(Channel.UNLIMITED)

    init {
        @OptIn(ExperimentalCoroutinesApi::class)
        launch(start = CoroutineStart.ATOMIC) {
            var cause: Throwable? = null
            try {
                for (workItem in workItems) {
                    currentCoroutineContext().ensureActive()
                    coroutineScope(workItem)
                }
            } catch (exception: Throwable) {
                cause = exception
                throw exception
            } finally {
                workItems.close(cause)
                val closedResult = failure<Nothing>(ClosedException(cause))
                while (true) {
                    val workItem = workItems.tryReceive().getOrNull() ?: break
                    workItem.continuation.resumeWith(closedResult)
                }
            }
        }
    }

    @OptIn(ExperimentalContracts::class)
    protected suspend fun <R> isolated(block: suspend CoroutineScope.() -> R): R {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }

        return suspendCoroutine { continuation ->
            workItems.trySend(WorkItem(block, continuation)).onFailure { cause ->
                continuation.resumeWithException(ClosedException(cause))
            }
        }
    }

    private class WorkItem<R>(
        val block: suspend CoroutineScope.() -> R,
        val continuation: Continuation<R>,
    ) : suspend CoroutineScope.() -> Unit {
        override suspend fun invoke(scope: CoroutineScope) {
            continuation.resumeWith(scope.runCatching { block() })
        }
    }

    class ClosedException(cause: Throwable?) : Exception(cause)

    companion object
}
