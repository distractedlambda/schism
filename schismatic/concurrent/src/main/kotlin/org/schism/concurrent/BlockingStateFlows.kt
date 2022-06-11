package org.schism.concurrent

public fun <T> MutableBlockingStateFlow(initialValue: T): MutableBlockingStateFlow<T> =
    BlockingStateFlowImpl(initialValue)
