package org.schism.util

fun interface SuspendingAutocloseable {
    suspend fun close()
}

suspend inline fun <T : SuspendingAutocloseable?, R> T.use(block: (T) -> R): R {
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

suspend fun SuspendingAutocloseable?.closeFinally(cause: Throwable?) {
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
