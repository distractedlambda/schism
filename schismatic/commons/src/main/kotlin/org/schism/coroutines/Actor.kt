package org.schism.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

public class Actor(scope: CoroutineScope): Job by Job(scope.coroutineContext[Job]) {
    private val mutex = Mutex()
    private val coroutineContext = scope.coroutineContext + this

    public suspend fun within(block: suspend CoroutineScope.() -> Unit) {
        mutex.withLock {
            withContext(coroutineContext, block)
        }
    }

    public companion object
}
