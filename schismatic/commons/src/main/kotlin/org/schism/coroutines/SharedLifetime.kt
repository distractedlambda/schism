package org.schism.coroutines

import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import java.lang.invoke.MethodHandles
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

// FIXME: unit-test this dude
public class SharedLifetime {
    @Volatile private var controlWord: Long = 0L
    private val endFence = Job()

    public suspend fun end(): Boolean {
        var lastControlWord: Long
        var nextControlWord: Long

        var wasEnder = true

        do {
            lastControlWord = controlWord

            if (lastControlWord < 0L) {
                wasEnder = false
                break
            }

            nextControlWord = controlWord + Long.MIN_VALUE
        } while (!VH_CONTROL_WORD.weakCompareAndSet(this, lastControlWord, nextControlWord))

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
            lastControlWord = controlWord
            check(lastControlWord != Long.MAX_VALUE) { "Retain count overflow" }
            check(lastControlWord != Long.MIN_VALUE) { "Lifetime has ended" }
            nextControlWord = lastControlWord + 1
        } while (!VH_CONTROL_WORD.weakCompareAndSet(this, lastControlWord, nextControlWord))
    }

    @PublishedApi internal fun release() {
        if ((VH_CONTROL_WORD.getAndAdd(this, -1L) as Long) == Long.MIN_VALUE + 1L) {
            endFence.complete()
        }
    }

    private companion object {
        private val VH_CONTROL_WORD = MethodHandles.lookup().findVarHandle(
            SharedLifetime::class.java, "controlWord",
            Long::class.java,
        )
    }
}
