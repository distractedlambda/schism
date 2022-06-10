package org.schism.concurrent

public fun virtualThread(
    name: String? = null,
    start: Boolean = true,
    allowSetThreadLocals: Boolean = true,
    inheritInheritableThreadLocals: Boolean = true,
    uncaughtExceptionHandler: Thread.UncaughtExceptionHandler? = null,
    body: Runnable,
): Thread = Thread.ofVirtual().let { builder ->
    builder.allowSetThreadLocals(allowSetThreadLocals)
    builder.inheritInheritableThreadLocals(inheritInheritableThreadLocals)
    name?.let(builder::name)
    uncaughtExceptionHandler?.let(builder::uncaughtExceptionHandler)
    if (start) builder.start(body) else builder.unstarted(body)
}
