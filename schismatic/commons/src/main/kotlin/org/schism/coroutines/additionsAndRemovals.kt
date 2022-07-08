package org.schism.coroutines

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flow
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

public fun <E : Any> Flow<Collection<E>>.additionsAndRemovals(): Flow<AddedOrRemoved<E>> = flow {
    val currentElements = mutableSetOf<E>()

    conflate().collect {
        val newElements = it.toMutableSet()

        currentElements.iterator().run {
            while (hasNext()) {
                val element = next()
                if (!newElements.remove(element)) {
                    remove()
                    emit(AddedOrRemoved.removed(element))
                }
            }
        }

        newElements.forEach { element ->
            currentElements.add(element)
            emit(AddedOrRemoved.added(element))
        }
    }
}

public data class AddedOrRemoved<out E>(val element: E, val kind: Kind) {
    @OptIn(ExperimentalContracts::class)
    public inline fun onAdded(body: (element: E) -> Unit): AddedOrRemoved<E> {
        contract {
            callsInPlace(body, InvocationKind.AT_MOST_ONCE)
        }

        if (kind == Kind.Added) {
            body(element)
        }

        return this
    }

    @OptIn(ExperimentalContracts::class)
    public inline fun onRemoved(body: (element: E) -> Unit): AddedOrRemoved<E> {
        contract {
            callsInPlace(body, InvocationKind.AT_MOST_ONCE)
        }

        if (kind == Kind.Removed) {
            body(element)
        }

        return this
    }

    @OptIn(ExperimentalContracts::class)
    public inline fun <R> fold(onAdded: (element: E) -> R, onRemoved: (element: E) -> R): R {
        contract {
            callsInPlace(onAdded, InvocationKind.AT_MOST_ONCE)
            callsInPlace(onRemoved, InvocationKind.AT_MOST_ONCE)
        }

        return when (kind) {
            Kind.Added -> onAdded(element)
            Kind.Removed -> onRemoved(element)
        }
    }

    public enum class Kind {
        Added,
        Removed;

        public companion object
    }

    public companion object {
        public fun <E> added(element: E): AddedOrRemoved<E> {
            return AddedOrRemoved(element, Kind.Added)
        }

        public fun <E> removed(element: E): AddedOrRemoved<E> {
            return AddedOrRemoved(element, Kind.Removed)
        }
    }
}
