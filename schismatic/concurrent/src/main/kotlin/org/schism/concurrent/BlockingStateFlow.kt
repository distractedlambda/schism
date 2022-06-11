package org.schism.concurrent

public sealed interface BlockingStateFlow<out T> : BlockingFlow<T> {
    public val value: T
}
