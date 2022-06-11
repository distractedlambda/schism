package org.schism.concurrent

import java.lang.Thread.currentThread
import java.lang.invoke.MethodHandles
import java.util.concurrent.locks.LockSupport
import java.util.concurrent.locks.LockSupport.park
import java.util.concurrent.locks.LockSupport.unpark

public class Task<out R> internal constructor(public val group: TaskGroup, body: TaskScope.() -> R) {
    @Volatile private var state: Any? = InitialState

    internal val thread = virtualThread(start = false) {
        val resultState = try {
            TaskScope.body()
        } catch (exception: Throwable) {
            FailedState(exception)
        }

        if (vhState.getAndSet(this, resultState) === JoinRequestedState) {
            unpark(group.owner)
        }
    }

    public fun join(): R {
        check(currentThread() === group.owner)

        while (true) {
            val lastState = state
            when {
                lastState === InitialState -> {
                    if (vhState.compareAndSet(this, lastState, JoinRequestedState)) {
                        park()
                        checkInterruption()
                    }
                }

                lastState === JoinRequestedState -> {
                    park()
                    checkInterruption()
                }

                lastState is FailedState -> {
                    group.unjoinedTasks.remove(this)
                    throw TaskFailureException(this, lastState.exception)
                }

                else -> {
                    group.unjoinedTasks.remove(this)
                    return (@Suppress("UNCHECKED_CAST") (lastState as R))
                }
            }
        }
    }

    private object InitialState

    private object JoinRequestedState

    private class FailedState(val exception: Throwable)

    public companion object {
        private val vhState = MethodHandles.lookup().findVarHandle(
            Task::class.java,
            "state",
            Any::class.java,
        )

        init {
            LockSupport::class.java
        }
    }
}
