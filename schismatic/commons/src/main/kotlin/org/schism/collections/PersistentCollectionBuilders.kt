package org.schism.collections

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentHashMapOf
import kotlinx.collections.immutable.persistentHashSetOf
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun <E> buildPersistentList(@BuilderInference block: MutableList<E>.() -> Unit): PersistentList<E> {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return persistentListOf<E>().mutate(block)
}

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun <E> buildPersistentSet(@BuilderInference block: MutableSet<E>.() -> Unit): PersistentSet<E> {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return persistentSetOf<E>().mutate(block)
}

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun <E> buildPersistentHashSet(@BuilderInference block: MutableSet<E>.() -> Unit): PersistentSet<E> {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return persistentHashSetOf<E>().mutate(block)
}

@OptIn(ExperimentalTypeInference::class, ExperimentalContracts::class)
public inline fun <K, V> buildPersistentMap(@BuilderInference block: MutableMap<K, V>.() -> Unit): PersistentMap<K, V> {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return persistentMapOf<K, V>().mutate(block)
}

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun <K, V> buildPersistentHashMap(@BuilderInference block: MutableMap<K, V>.() -> Unit): PersistentMap<K, V> {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return persistentHashMapOf<K, V>().mutate(block)
}
