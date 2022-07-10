package org.schism.coroutines

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

// FIXME: unit-test this dude
public class SharedLifetime {
    private val controlWord = atomic(0L)
    private val endFence = Job()

    public suspend fun end(): Boolean {
        var lastControlWord: Long
        var nextControlWord: Long

        var wasEnder = true

        do {
            lastControlWord = controlWord.value

            if (lastControlWord < 0L) {
                wasEnder = false
                break
            }

            nextControlWord = lastControlWord + Long.MIN_VALUE
        } while (!controlWord.compareAndSet(lastControlWord, nextControlWord))

        if (lastControlWord == 0L) {
            endFence.complete()
        } else {
            withContext(NonCancellable) {
                endFence.join()
            }
        }

        return wasEnder
    }

    @OptIn(ExperimentalContracts::class)
    public inline fun <R> withRetained(block: () -> R): R {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }

        retain()

        try {
            return block()
        } finally {
            release()
        }
    }

    @PublishedApi internal fun retain() {
        var lastControlWord: Long
        var nextControlWord: Long
        do {
            lastControlWord = controlWord.value
            check(lastControlWord != Long.MAX_VALUE) { "Retain count overflow" }
            check(lastControlWord != Long.MIN_VALUE) { "Lifetime has ended" }
            nextControlWord = lastControlWord + 1
        } while (!controlWord.compareAndSet(lastControlWord, nextControlWord))
    }

    @PublishedApi internal fun release() {
        if (controlWord.getAndDecrement() == Long.MIN_VALUE + 1L) {
            endFence.complete()
        }
    }
}
