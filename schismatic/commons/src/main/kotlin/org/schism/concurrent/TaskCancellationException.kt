package org.schism.concurrent

public class TaskCancellationException internal constructor(
    public val task: ThreadTask,
    message: String? = null,
    cause: Throwable? = null,
) : Exception(message, cause)
