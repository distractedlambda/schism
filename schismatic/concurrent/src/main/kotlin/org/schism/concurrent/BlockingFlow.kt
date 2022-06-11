package org.schism.concurrent

public interface BlockingFlow<out T> {
    public fun collect(collector: BlockingFlowCollector<T>)
}
