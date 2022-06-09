package org.schism.concurrent

public inline fun <T, U> BlockingFlow<T>.transform(
    crossinline body: BlockingFlowCollector<U>.(T) -> Unit,
): BlockingFlow<U> = blockingFlow { collect { body(it) } }

public inline fun <T, U> BlockingFlow<T>.map(crossinline op: (T) -> U): BlockingFlow<U> =
    transform { emit(op(it)) }

public inline fun <T, U> BlockingFlow<T>.mapNotNull(crossinline op: (T) -> U?): BlockingFlow<U> =
    transform { op(it)?.let(::emit) }

public inline fun <T> BlockingFlow<T>.filter(crossinline predicate: (T) -> Boolean): BlockingFlow<T> =
    transform { if (predicate(it)) emit(it) }

public fun <T> BlockingFlow<BlockingFlow<T>>.flatten(): BlockingFlow<T> =
    transform { it.collect(this@transform) }

public inline fun <T, U> BlockingFlow<T>.flatMap(crossinline op: (T) -> BlockingFlow<U>): BlockingFlow<U> =
    transform { op(it).collect(this@transform) }

public fun <U> BlockingFlow<*>.filterIsInstance(cls: Class<U>): BlockingFlow<U> =
    transform { if (cls.isInstance(it)) emit(@Suppress("UNCHECKED_CAST") (it as U)) }

public inline fun <reified U> BlockingFlow<*>.filterIsInstance(): BlockingFlow<U> =
    filterIsInstance(U::class.java)
