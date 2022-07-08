package org.schism.coroutines

public fun interface SuspendingAutocloseable {
    public suspend fun close()
}

public suspend inline fun <T : SuspendingAutocloseable?, R> T.use(block: (T) -> R): R {
    var cause: Throwable? = null

    try {
        return block(this)
    } catch (exception: Throwable) {
        cause = exception
        throw exception
    } finally {
        closeFinally(cause)
    }
}

@PublishedApi internal suspend fun SuspendingAutocloseable?.closeFinally(cause: Throwable?) {
    if (this == null) {
        return
    }

    try {
        close()
    } catch (exception: Throwable) {
        if (cause != null) {
            cause.addSuppressed(exception)
        } else {
            throw exception
        }
    }
}
