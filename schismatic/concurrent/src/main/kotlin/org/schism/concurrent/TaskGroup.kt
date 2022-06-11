package org.schism.concurrent

import java.lang.Thread.currentThread
import java.lang.invoke.MethodHandles
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.LockSupport
import java.util.concurrent.locks.LockSupport.park
import java.util.concurrent.locks.LockSupport.unpark
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@DslMarker
@Target(AnnotationTarget.CLASS)
public annotation class TaskGroupDsl

@TaskGroupDsl public class TaskGroup @PublishedApi internal constructor() {
    public val owner: Thread = currentThread()
    private val activeTasks = ConcurrentHashMap.newKeySet<Task<*>>()
    @Volatile private var incompleteTaskCount = 0

    public fun <R> task(body: TaskScope.() -> R): Task<R> {
        check(currentThread() === owner)

        return Task(this, body).also {
            activeTasks.add(it)
            vhIncompleteTaskCount.getAndAdd(this, 1)
            it.thread.start()
        }
    }

    @PublishedApi internal fun exit(cause: Throwable?) {
        check(currentThread() === owner)

        if (cause != null) {
            activeTasks.forEach { it.thread.interrupt() }
        }

        var wasInterrupted = false

        while (incompleteTaskCount != 0) {
            park()

            if (Thread.interrupted()) {
                wasInterrupted = true
            }
        }

        if (wasInterrupted && cause == null) {
            throw InterruptedException()
        }
    }

    public class Task<out R> internal constructor(public val group: TaskGroup, body: TaskScope.() -> R) {
        @Volatile private var state: Any? = InitialState

        internal val thread = virtualThread(start = false) {
            val resultState = try {
                TaskScope.body()
            } catch (exception: Throwable) {
                FailedState(exception)
            }

            group.activeTasks.remove(this)

            var shouldUnparkOwner = false

            if (vhState.getAndSet(this, resultState) === JoinRequestedState) {
                shouldUnparkOwner = true
            }

            if (vhIncompleteTaskCount.getAndAdd(group, -1) as Int == 1) {
                // FIXME have a way for the owner to indicate that it's waiting for the active count to hit 0
                shouldUnparkOwner = true
            }

            if (shouldUnparkOwner) {
                unpark(group.owner)
            }
        }

        public fun join(): Result<R> {
            check(currentThread() === group.owner)

            while (true) {
                val lastState = state
                when {
                    lastState === InitialState -> {
                        if (vhState.compareAndSet(this, lastState, JoinRequestedState)) {
                            park()
                        }
                    }

                    lastState is FailedState -> {
                        return failure(lastState.exception)
                    }

                    else -> {
                        return success(@Suppress("UNCHECKED_CAST") (lastState as R))
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
        }
    }

    @TaskGroupDsl public object TaskScope

    public companion object {
        private val vhIncompleteTaskCount = MethodHandles.lookup().findVarHandle(
            TaskGroup::class.java,
            "incompleteTaskCount",
            Int::class.java,
        )

        init {
            LockSupport::class.java
        }
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun <R> taskGroup(body: TaskGroup.() -> R): R {
    contract {
        callsInPlace(body, InvocationKind.EXACTLY_ONCE)
    }

    val scope = TaskGroup()
    var exitCause: Throwable? = null

    try {
        return scope.body()
    } catch (exception: Throwable) {
        exitCause = exception
        throw exception
    } finally {
        scope.exit(exitCause)
    }
}
