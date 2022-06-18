package org.schism.coroutines

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flow
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

fun <E : Any> Flow<Collection<E>>.additionsAndRemovals(): Flow<AddedOrRemoved<E>> = flow {
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

data class AddedOrRemoved<out E>(val element: E, val kind: Kind) {
    @OptIn(ExperimentalContracts::class)
    inline fun onAdded(body: (element: E) -> Unit): AddedOrRemoved<E> {
        contract {
            callsInPlace(body, InvocationKind.AT_MOST_ONCE)
        }

        if (kind == Kind.Added) {
            body(element)
        }

        return this
    }

    @OptIn(ExperimentalContracts::class)
    inline fun onRemoved(body: (element: E) -> Unit): AddedOrRemoved<E> {
        contract {
            callsInPlace(body, InvocationKind.AT_MOST_ONCE)
        }

        if (kind == Kind.Removed) {
            body(element)
        }

        return this
    }

    @OptIn(ExperimentalContracts::class)
    inline fun <R> fold(onAdded: (element: E) -> R, onRemoved: (element: E) -> R): R {
        contract {
            callsInPlace(onAdded, InvocationKind.AT_MOST_ONCE)
            callsInPlace(onRemoved, InvocationKind.AT_MOST_ONCE)
        }

        return when (kind) {
            Kind.Added -> onAdded(element)
            Kind.Removed -> onRemoved(element)
        }
    }

    enum class Kind {
        Added,
        Removed;

        companion object
    }

    companion object {
        fun <E> added(element: E): AddedOrRemoved<E> {
            return AddedOrRemoved(element, Kind.Added)
        }

        fun <E> removed(element: E): AddedOrRemoved<E> {
            return AddedOrRemoved(element, Kind.Removed)
        }
    }
}
