package org.schism.concurrent

import java.lang.Math.incrementExact
import java.lang.Thread.currentThread
import java.lang.invoke.MethodHandles
import java.util.concurrent.locks.LockSupport
import java.util.concurrent.locks.LockSupport.park
import java.util.concurrent.locks.LockSupport.unpark

internal class WorkScopeImpl private constructor(
    override val parentScope: WorkScope?,
    @Volatile private var state: State,
) : WorkScope {
    constructor(): this(null, initialState)

    override val isActive: Boolean
        get() = state is ActiveState

    override val isCompleted: Boolean
        get() = state is CompletedState

    override fun childScope(): WorkScope {
        while (true) when (val lastState = state) {
            is ActiveState -> {
                val child = WorkScopeImpl(this, initialState)

                val newState = ActiveState(
                    onCompletion = lastState.onCompletion,
                    retainCount = incrementExact(lastState.retainCount),
                    onCancellation = { cause ->
                        lastState.onCancellation(cause)
                        child.cancel(cause)
                    },
                )

                if (vhState.compareAndSet(this, lastState, newState)) {
                    child.invokeOnCompletion { release() }
                    return child
                }
            }

            is CancellingState -> {
                return WorkScopeImpl(this, CompletedState(lastState.cause))
            }

            is CompletedState -> {
                return WorkScopeImpl(this, lastState)
            }
        }
    }

    override fun launch(body: WorkScope.() -> Unit): WorkScope {
        while (true) when (val lastState = state) {
            is ActiveState -> {
                val child = WorkScopeImpl(this, initialLaunchState)

                val thread = virtualThread(start = false) {
                    // FIXME: set thread-local scope
                    var exitCause: Throwable? = null
                    try {
                        child.body()
                    } catch (_: InterruptedException) {
                    } catch (exception: Throwable) {
                        exitCause = exception
                    } finally {
                        while (true) when (val lastChildState = child.state) {
                            is ActiveState -> {

                            }
                        }
                    }
                }
            }
        }
    }

    override fun cancel(cause: Throwable?) {
        val effectiveCause = cause ?: WorkScopeCancellationException("WorkScope was canceled")

        while (true) when (val lastState = state) {
            is ActiveState -> when (lastState.retainCount) {
                0L -> {
                    val newState = CompletedState(effectiveCause)
                    if (vhState.compareAndSet(this, lastState, newState)) {
                        lastState.onCancellation(effectiveCause)
                        lastState.onCompletion(effectiveCause)
                        return
                    }
                }

                else -> {
                    val newState = CancellingState(effectiveCause, lastState.onCompletion, lastState.retainCount)
                    if (vhState.compareAndSet(this, lastState, newState)) {
                        lastState.onCancellation(effectiveCause)
                        return
                    }
                }
            }

            is CancellingState -> {
                lastState.cause.addSuppressed(effectiveCause)
                return
            }

            is CompletedState -> {
                return
            }
        }
    }

    override fun invokeOnCompletion(action: (cause: Throwable?) -> Unit) {
        while (true) when (val lastState = state) {
            is IncompleteState -> {
                val newState = lastState.copy(
                    onCompletion = { cause ->
                        lastState.onCompletion(cause)
                        try {
                            action(cause)
                        } catch (exception: Throwable) {
                            exception.printStackTrace()
                        }
                    },
                )

                if (vhState.compareAndSet(this, state, newState)) {
                    return
                }
            }

            is CompletedState -> {
                try {
                    action(lastState.cause)
                } catch (exception: Throwable) {
                    exception.printStackTrace()
                }

                return
            }
        }
    }

    override fun join() {
        while (true) when (val lastState = state) {
            is IncompleteState -> {
                val thread = currentThread()

                val newState = lastState.copy(
                    onCompletion = { cause ->
                        lastState.onCompletion(cause)
                        unpark(thread)
                    },
                )

                if (vhState.compareAndSet(this, state, newState)) {
                    park()
                    checkInterruption()
                }
            }

            is CompletedState -> {
                return
            }
        }
    }

    private fun release() {
        while (true) {
            val lastState = state
            when {
                lastState is CancellingState && lastState.retainCount == 1L -> {
                    val newState = CompletedState(lastState.cause)
                    if (vhState.compareAndSet(this, lastState, newState)) {
                        lastState.onCompletion(lastState.cause)
                        return
                    }
                }

                lastState is IncompleteState -> {
                    val newState = lastState.copy(retainCount = lastState.retainCount.dec())
                    if (vhState.compareAndSet(this, lastState, newState)) {
                        return
                    }
                }

                else -> error("illegal state for release")
            }
        }
    }

    private sealed class State

    private sealed class IncompleteState(
        val onCompletion: (Throwable?) -> Unit,
        val retainCount: Long,
    ) : State() {
        abstract fun copy(
            onCompletion: (Throwable?) -> Unit = this.onCompletion,
            retainCount: Long = this.retainCount,
        ): IncompleteState
    }

    private class ActiveState(
        val onCancellation: (Throwable) -> Unit,
        onCompletion: (Throwable?) -> Unit,
        retainCount: Long,
    ) : IncompleteState(onCompletion, retainCount) {
        override fun copy(onCompletion: (Throwable?) -> Unit, retainCount: Long): IncompleteState =
            ActiveState(onCancellation, onCompletion, retainCount)
    }

    private class CancellingState(
        val cause: Throwable,
        onCompletion: (Throwable?) -> Unit,
        retainCount: Long,
    ) : IncompleteState(onCompletion, retainCount) {
        override fun copy(onCompletion: (Throwable?) -> Unit, retainCount: Long): IncompleteState =
            CancellingState(cause, onCompletion, retainCount)
    }

    private class CompletedState(val cause: Throwable?) : State()

    private companion object {
        private val vhState = MethodHandles.lookup().findVarHandle(
            WorkScopeImpl::class.java,
            "state",
            State::class.java,
        )

        private val initialState = ActiveState(onCancellation = {}, onCompletion = {}, retainCount = 0)
        private val initialLaunchState = ActiveState(onCancellation = {}, onCompletion = {}, retainCount = 1)

        init {
            LockSupport::class.java
        }
    }
}
