package org.schism.concurrent

import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReadWriteLock
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
public inline fun <R> Lock.withLockInterruptibly(block: () -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    lockInterruptibly()

    try {
        return block()
    } finally {
        unlock()
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun <R> ReadWriteLock.readInterruptibly(block: () -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return readLock().withLockInterruptibly(block)
}

@OptIn(ExperimentalContracts::class)
public inline fun <R> ReadWriteLock.writeInterruptibly(block: () -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return writeLock().withLockInterruptibly(block)
}
