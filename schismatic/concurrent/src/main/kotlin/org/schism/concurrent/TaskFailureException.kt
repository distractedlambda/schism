package org.schism.concurrent

public class TaskFailureException internal constructor(public val task: Task<*>, cause: Throwable) : Exception(cause)
