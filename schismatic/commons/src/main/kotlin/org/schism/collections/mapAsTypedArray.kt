package org.schism.collections

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
public inline fun <T, reified U> Collection<T>.mapAsTypedArray(transform: (T) -> U): Array<U> {
    contract {
        callsInPlace(transform, InvocationKind.UNKNOWN)
    }

    val iterator = iterator()

    return Array(size) {
        check(iterator.hasNext())
        transform(iterator.next())
    }
}
