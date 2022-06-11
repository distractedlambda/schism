package org.schism.concurrent

import java.lang.Thread.currentThread

@TaskDsl public class TaskGroup @PublishedApi internal constructor() {
    public val owner: Thread = currentThread()
    internal val unjoinedTasks = hashSetOf<Task<*>>()

    public fun <R> task(body: TaskScope.() -> R): Task<R> {
        check(currentThread() === owner)

        return Task(this, body).also {
            unjoinedTasks.add(it)
            it.thread.start()
        }
    }

    @PublishedApi internal fun exit(cause: Throwable?) {
        check(currentThread() === owner)

        val coalescedCause = cause ?: kotlin.run {
            while (unjoinedTasks.isNotEmpty()) {
                try {
                    unjoinedTasks.first().join()
                } catch (exception: TaskFailureException) {
                    return@run exception.cause!!
                } catch (exception: InterruptedException) {
                    return@run exception
                }
            }

            return
        }

        unjoinedTasks.forEach {
            it.thread.interrupt()
        }

        while (unjoinedTasks.isNotEmpty()) {
            try {
                unjoinedTasks.first().join()
            } catch (_: InterruptedException) {
            } catch (exception: TaskFailureException) {
                coalescedCause.addSuppressed(exception.cause)
            }
        }

        throw coalescedCause
    }

    public companion object
}
