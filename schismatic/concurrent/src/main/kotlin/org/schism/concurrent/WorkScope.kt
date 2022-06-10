package org.schism.concurrent

public sealed interface WorkScope {
    public val parentScope: WorkScope?

    public val isActive: Boolean

    public val isCompleted: Boolean

    public fun launch(body: WorkScope.() -> Unit): WorkScope

    public fun childScope(): WorkScope

    public fun cancel(cause: Throwable?)

    public fun join()

    public fun invokeOnCompletion(action: (cause: Throwable?) -> Unit)
}
