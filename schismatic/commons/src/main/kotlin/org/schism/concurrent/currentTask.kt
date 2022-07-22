package org.schism.concurrent

public fun currentTask(): ThreadTask<*>? {
    return CURRENT_TASK.get()
}

internal fun setCurrentTask(task: ThreadTask<*>?) {
    CURRENT_TASK.set(task)
}

private val CURRENT_TASK = ThreadLocal<ThreadTask<*>?>()
