package org.schism.concurrent

import kotlin.experimental.ExperimentalTypeInference

@OptIn(ExperimentalTypeInference::class)
public fun <T> blockingFlow(@BuilderInference body: BlockingFlowCollector<T>.() -> Unit): BlockingFlow<T> =
    object : BlockingFlow<T> {
        override fun collect(collector: BlockingFlowCollector<T>) {
            collector.body()
        }
    }

public fun <T> BlockingFlow<T>.collect(onEach: (T) -> Unit): Unit =
    collect(SafeBlockingFlowCollector(onEach))

public fun <T, U> BlockingFlow<T>.transform(body: BlockingFlowCollector<U>.(T) -> Unit): BlockingFlow<U> =
    blockingFlow { collect { body(it) } }

public fun <T, U> BlockingFlow<T>.map(op: (T) -> U): BlockingFlow<U> =
    transform { emit(op(it)) }

public fun <T, U> BlockingFlow<T>.mapNotNull(op: (T) -> U?): BlockingFlow<U> =
    transform { op(it)?.let(::emit) }

public fun <T> BlockingFlow<T>.filter(predicate: (T) -> Boolean): BlockingFlow<T> =
    transform { if (predicate(it)) emit(it) }

public fun <T> BlockingFlow<BlockingFlow<T>>.flatten(): BlockingFlow<T> =
    transform { it.collect(this@transform) }

public fun <T, U> BlockingFlow<T>.flatMap(op: (T) -> BlockingFlow<U>): BlockingFlow<U> =
    transform { op(it).collect(this@transform) }

public fun <U> BlockingFlow<*>.filterIsInstance(cls: Class<U>): BlockingFlow<U> =
    transform { if (cls.isInstance(it)) emit(@Suppress("UNCHECKED_CAST") (it as U)) }

public inline fun <reified U> BlockingFlow<*>.filterIsInstance(): BlockingFlow<U> =
    filterIsInstance(U::class.java)
