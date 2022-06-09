package org.schism.concurrent

import java.lang.Thread.currentThread

public interface BlockingFlow<out T> {
    public fun collect(collector: BlockingFlowCollector<T>)
}

public sealed interface BlockingFlowCollector<in T> {
    public fun emit(item: T)
}

public inline fun <T> BlockingFlow<T>.collect(crossinline onEach: (T) -> Unit) {
    collect(SafeBlockingFlowCollector(onEach))
}

public inline fun <T> blockingFlow(crossinline body: BlockingFlowCollector<T>.() -> Unit): BlockingFlow<T> =
    object : BlockingFlow<T> {
        override fun collect(collector: BlockingFlowCollector<T>) {
            collector.body()
        }
    }

@PublishedApi internal abstract class SafeBlockingFlowCollector<T> : BlockingFlowCollector<T> {
    private val collectionThread = currentThread()

    abstract fun emitUnchecked(item: T)

    final override fun emit(item: T) {
        check(currentThread() === collectionThread)
        emitUnchecked(item)
    }
}

@PublishedApi internal inline fun <T> SafeBlockingFlowCollector(
    crossinline emit: (T) -> Unit,
): SafeBlockingFlowCollector<T> = object : SafeBlockingFlowCollector<T>() {
    override fun emitUnchecked(item: T) {
        emit(item)
    }
}
