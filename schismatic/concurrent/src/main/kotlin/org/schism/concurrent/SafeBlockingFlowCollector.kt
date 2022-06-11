package org.schism.concurrent

import java.lang.Thread.currentThread

internal class SafeBlockingFlowCollector<in T>(private val onEach: (T) -> Unit) : BlockingFlowCollector<T> {
    private val collectionThread = currentThread()

    override fun emit(item: T) {
        check(currentThread() === collectionThread)
        onEach(item)
    }
}
