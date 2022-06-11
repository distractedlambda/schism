package org.schism.concurrent

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
public inline fun <R> taskGroup(body: TaskGroup.() -> R): R {
    contract {
        callsInPlace(body, InvocationKind.EXACTLY_ONCE)
    }

    val group = TaskGroup()
    var exitCause: Throwable? = null

    try {
        return group.body()
    } catch (exception: Throwable) {
        exitCause = exception
    } finally {
        group.exit(exitCause)
    }

    throw AssertionError()
}
