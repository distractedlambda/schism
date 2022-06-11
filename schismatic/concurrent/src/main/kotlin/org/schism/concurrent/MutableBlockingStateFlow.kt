package org.schism.concurrent

public sealed interface MutableBlockingStateFlow<T> : BlockingStateFlow<T> {
    public override var value: T
}
