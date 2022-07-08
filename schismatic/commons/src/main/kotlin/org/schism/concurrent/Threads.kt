package org.schism.concurrent

public fun platformThread(
    start: Boolean = true,
    isDaemon: Boolean = false,
    name: String? = null,
    priority: Int = -1,
    stackSize: Long = -1,
    group: ThreadGroup? = null,
    allowSetThreadLocals: Boolean = true,
    inheritInheritableThreadLocals: Boolean = true,
    uncaughtExceptionHandler: Thread.UncaughtExceptionHandler? = null,
    block: Runnable,
): Thread {
    val builder = Thread.ofPlatform()
    builder.daemon(isDaemon)
    name?.let(builder::name)
    if (priority != -1) builder.priority(priority)
    if (stackSize != -1L) builder.stackSize(stackSize)
    group?.let(builder::group)
    builder.allowSetThreadLocals(allowSetThreadLocals)
    builder.inheritInheritableThreadLocals(inheritInheritableThreadLocals)
    uncaughtExceptionHandler?.let(builder::uncaughtExceptionHandler)
    return if (start) builder.start(block) else builder.unstarted(block)
}

public fun virtualThread(
    start: Boolean = true,
    name: String? = null,
    allowSetThreadLocals: Boolean = true,
    inheritInheritableThreadLocals: Boolean = true,
    uncaughtExceptionHandler: Thread.UncaughtExceptionHandler? = null,
    block: Runnable,
): Thread {
    val builder = Thread.ofVirtual()
    name?.let(builder::name)
    builder.allowSetThreadLocals(allowSetThreadLocals)
    builder.inheritInheritableThreadLocals(inheritInheritableThreadLocals)
    uncaughtExceptionHandler?.let(builder::uncaughtExceptionHandler)
    return if (start) builder.start(block) else builder.unstarted(block)
}
