package org.schism.collections

import java.util.concurrent.ConcurrentMap
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
public inline fun <K : Any, V : Any> ConcurrentMap<K, V>.loop(key: K, block: (V?) -> Unit): Nothing {
    contract {
        callsInPlace(block, InvocationKind.AT_LEAST_ONCE)
    }

    while (true) {
        block(get(key))
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun <K : Any, V : Any> ConcurrentMap<K, V>.update(key: K, op: (V?) -> V?) {
    contract {
        callsInPlace(op, InvocationKind.AT_LEAST_ONCE)
    }

    loop(key) { oldValue ->
        if (tryCommitUpdate(key, oldValue, op(oldValue))) {
            return
        }
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun <K : Any, V : Any> ConcurrentMap<K, V>.getAndUpdate(key: K, op: (V?) -> V?): V? {
    contract {
        callsInPlace(op, InvocationKind.AT_LEAST_ONCE)
    }

    loop(key) { oldValue ->
        if (tryCommitUpdate(key, oldValue, op(oldValue))) {
            return oldValue
        }
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun <K : Any, V : Any, R : V?> ConcurrentMap<K, V>.updateAndGet(key: K, op: (V?) -> R): R {
    contract {
        callsInPlace(op, InvocationKind.AT_LEAST_ONCE)
    }

    loop(key) { oldValue ->
        val newValue = op(oldValue)
        if (tryCommitUpdate(key, oldValue, newValue)) {
            return newValue
        }
    }
}

@PublishedApi
internal fun <K : Any, V : Any> ConcurrentMap<K, V>.tryCommitUpdate(key: K, oldValue: V?, newValue: V?): Boolean {
    return when {
        oldValue == null -> when {
            newValue == null -> true
            else -> putIfAbsent(key, newValue) == null
        }

        else -> when {
            newValue == null -> remove(key, oldValue)
            else -> replace(key, oldValue, newValue)
        }
    }
}
