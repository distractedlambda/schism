package org.schism.concurrent

public sealed interface BlockingFlowCollector<in T> {
    public fun emit(item: T)
}
