package org.schism.coroutines

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.mutate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class) @JvmName("updateListMutating")
public inline fun <E> MutableStateFlow<PersistentList<E>>.updateMutating(
    block: MutableList<E>.() -> Unit,
) {
    contract {
        callsInPlace(block, InvocationKind.AT_LEAST_ONCE)
    }

    update {
        it.mutate(block)
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun <E> MutableStateFlow<PersistentList<E>>.getAndUpdateMutating(
    block: MutableList<E>.() -> Unit,
): PersistentList<E> {
    contract {
        callsInPlace(block, InvocationKind.AT_LEAST_ONCE)
    }

    return getAndUpdate {
        it.mutate(block)
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun <E> MutableStateFlow<PersistentList<E>>.updateAndGetMutating(
    block: MutableList<E>.() -> Unit,
): PersistentList<E> {
    contract {
        callsInPlace(block, InvocationKind.AT_LEAST_ONCE)
    }

    return updateAndGet {
        it.mutate(block)
    }
}

@OptIn(ExperimentalContracts::class) @JvmName("updateSetMutating")
public inline fun <E> MutableStateFlow<PersistentSet<E>>.updateMutating(
    block: MutableSet<E>.() -> Unit,
) {
    contract {
        callsInPlace(block, InvocationKind.AT_LEAST_ONCE)
    }

    update {
        it.mutate(block)
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun <E> MutableStateFlow<PersistentSet<E>>.getAndUpdateMutating(
    block: MutableSet<E>.() -> Unit,
): PersistentSet<E> {
    contract {
        callsInPlace(block, InvocationKind.AT_LEAST_ONCE)
    }

    return getAndUpdate {
        it.mutate(block)
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun <E> MutableStateFlow<PersistentSet<E>>.updateAndGetMutating(
    block: MutableSet<E>.() -> Unit,
): PersistentSet<E> {
    contract {
        callsInPlace(block, InvocationKind.AT_LEAST_ONCE)
    }

    return updateAndGet {
        it.mutate(block)
    }
}

@OptIn(ExperimentalContracts::class) @JvmName("updateMapMutating")
public inline fun <K, V> MutableStateFlow<PersistentMap<K, V>>.updateMutating(
    block: MutableMap<K, V>.() -> Unit,
) {
    contract {
        callsInPlace(block, InvocationKind.AT_LEAST_ONCE)
    }

    update {
        it.mutate(block)
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun <K, V> MutableStateFlow<PersistentMap<K, V>>.getAndUpdateMutating(
    block: MutableMap<K, V>.() -> Unit,
): PersistentMap<K, V> {
    contract {
        callsInPlace(block, InvocationKind.AT_LEAST_ONCE)
    }

    return getAndUpdate {
        it.mutate(block)
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun <K, V> MutableStateFlow<PersistentMap<K, V>>.updateAndGetMutating(
    block: MutableMap<K, V>.() -> Unit,
): PersistentMap<K, V> {
    contract {
        callsInPlace(block, InvocationKind.AT_LEAST_ONCE)
    }

    return updateAndGet {
        it.mutate(block)
    }
}
