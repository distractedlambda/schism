package org.schism.concurrent

import kotlinx.atomicfu.atomic
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf
import java.lang.Thread.currentThread
import java.util.concurrent.locks.LockSupport
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success

internal class ThreadTask<T>(val thread: Thread = currentThread(), val parent: ThreadTask<*>? = currentTask()) {
    private val state = atomic<State<T>>(Active(persistentSetOf()))

    private fun awaitCompletion(): T {
        while (true) when (val lastState = state.value) {
            is Completing -> {
                LockSupport.park()
            }

            is Complete -> {
                return lastState.result.getOrThrow()
            }

            else -> {
                error("Unexpected state: $lastState")
            }
        }
    }

    fun complete(value: T): T {
        while (true) when (val lastState = state.value) {
            is Active -> {
                if (lastState.incompleteChildren.isEmpty()) {
                    if (state.compareAndSet(lastState, Complete(success(value)))) {
                        return value
                    }
                } else {
                    if (state.compareAndSet(lastState, Completing(lastState.incompleteChildren, success(value)))) {
                        LockSupport.park()
                        return awaitCompletion()
                    }
                }
            }

            else -> {
                return awaitCompletion()
            }
        }
    }

    fun completeExceptionally(cause: Throwable): T {
        while (true) when (val lastState = state.value) {
            is Active -> {
                if (lastState.incompleteChildren.isEmpty()) {
                    if (state.compareAndSet(lastState, Complete(failure(cause)))) {
                        throw cause
                    }
                } else {
                    if (state.compareAndSet(lastState, Completing(lastState.incompleteChildren, failure(cause)))) {
                        for (child in lastState.incompleteChildren) child.cancel(cause)
                        return awaitCompletion()
                    }
                }
            }

            is Completing -> {
                lastState.result.onSuccess {
                    if (state.compareAndSet(lastState, Completing(lastState.incompleteChildren, failure(cause)))) {
                        for (child in lastState.incompleteChildren) child.cancel(cause)
                        return awaitCompletion()
                    }
                }.onFailure {
                    it.addSuppressed(cause)
                    return awaitCompletion()
                }
            }

            is Complete -> {
                lastState.result.onSuccess {
                    error("Task was already completed")
                }.onFailure {
                    it.addSuppressed(cause)
                    throw it
                }
            }
        }
    }

    fun cancel(cause: Throwable? = null) {
        val exception = TaskCancellationException(this, "Task was canceled", cause)
        while (true) when (val lastState = state.value) {
            is Active -> {
                if (lastState.incompleteChildren.isEmpty()) {
                    if (state.compareAndSet(lastState, Complete(failure(exception)))) {
                        return
                    }
                } else {
                    if (state.compareAndSet(lastState, Completing(lastState.incompleteChildren, failure(exception)))) {
                        for (child in lastState.incompleteChildren) child.cancel(exception)
                        return
                    }
                }
            }

            is Completing -> {
                lastState.result.onSuccess {
                    if (state.compareAndSet(lastState, Completing(lastState.incompleteChildren, failure(exception)))) {
                        for (child in lastState.incompleteChildren) child.cancel(exception)
                        return
                    }
                }.onFailure {
                    cause?.let(it::addSuppressed)
                }
            }

            is Complete -> {
                lastState.result.onFailure {
                    cause?.let(it::addSuppressed)
                }
            }
        }
    }

    fun <R> launch(block: ThreadTask<R>.() -> R): ThreadTask<R> {
        TODO()
    }

    private sealed class State<out T>

    private data class Active(val incompleteChildren: PersistentSet<ThreadTask<*>>) : State<Nothing>()

    private data class Completing<out T>(
        val incompleteChildren: PersistentSet<ThreadTask<*>>,
        val result: Result<T>,
    ) : State<T>()

    private data class Complete<out T>(val result: Result<T>) : State<T>()
}
