package org.schism.concurrent

import java.lang.Thread.currentThread
import java.lang.invoke.MethodHandles
import java.util.concurrent.locks.LockSupport
import java.util.concurrent.locks.LockSupport.park
import java.util.concurrent.locks.LockSupport.unpark
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

public sealed interface Observable<T> {
    public val value: T

    public fun subscribe(): Subscription<T>

    public interface Subscription<T> : AutoCloseable {
        public val ownerThread: Thread

        public fun next(): T
    }
}

public sealed interface MutableObservable<T> : Observable<T> {
    public override var value: T
}

public fun <T> MutableObservable(initialValue: T): MutableObservable<T> = ObservableImpl(initialValue)

@Suppress("UNCHECKED_CAST")
internal class ObservableImpl<T>(initialValue: T) : MutableObservable<T> {
    private val mutationAndSubscriptionLock = ReentrantLock()
    private var storedValue = initialValue
    private var subscriptions: Any? = null

    override var value: T
        get() = STORED_VALUE.getVolatile(this) as T

        set(newValue) = mutationAndSubscriptionLock.withLockInterruptibly {
            if (newValue == storedValue) {
                return
            }

            storedValue = newValue

            when (val subscriptions = subscriptions) {
                null -> {}

                is SubscriptionImpl<*> -> {
                    (subscriptions as SubscriptionImpl<T>).publish(newValue)
                }

                is Collection<*> -> subscriptions.forEach {
                    (it as SubscriptionImpl<T>).publish(newValue)
                }

                else -> error("Unexpected subscription set representation")
            }
        }

    override fun subscribe(): Observable.Subscription<T> = mutationAndSubscriptionLock.withLockInterruptibly {
        val subscription = SubscriptionImpl(this)

        when (val currentSubscriptions = subscriptions) {
            null -> {
                subscriptions = subscription
            }

            is SubscriptionImpl<*> -> {
                subscriptions = hashSetOf(currentSubscriptions, subscription)
            }

            is MutableCollection<*> -> {
                (currentSubscriptions as MutableCollection<SubscriptionImpl<T>>).add(subscription)
            }

            else -> error("Unexpected subscription set representation")
        }

        return subscription
    }

    private class SubscriptionImpl<T>(private val observable: ObservableImpl<T>) : Observable.Subscription<T> {
        override val ownerThread: Thread = currentThread()

        private var closed = false

        @Volatile private var newValue: Any? = observable.storedValue

        fun publish(value: T) {
            newValue = value
            unpark(ownerThread)
        }

        override fun next(): T {
            check(currentThread() === ownerThread)
            check(!closed)

            while (newValue === NoValue) {
                park()
                if (Thread.interrupted()) throw InterruptedException()
            }

            return NEW_VALUE.getAndSet(this, NoValue) as T
        }

        override fun close() {
            check(currentThread() === ownerThread)

            if (closed) return
            closed = true

            observable.mutationAndSubscriptionLock.withLock {
                when (val subscriptions = observable.subscriptions) {
                    this -> {
                        observable.subscriptions = null
                    }

                    is MutableCollection<*> -> {
                        (subscriptions as MutableCollection<SubscriptionImpl<T>>).remove(this)
                    }

                    else -> error("Unexpected subscription set representation")
                }
            }
        }

        private object NoValue

        private companion object {
            private val NEW_VALUE = MethodHandles.lookup().findVarHandle(
                SubscriptionImpl::class.java,
                "newValue",
                Any::class.java,
            )

            init {
                LockSupport::class.java
            }
        }
    }

    private companion object {
        private val STORED_VALUE = MethodHandles.lookup().findVarHandle(
            ObservableImpl::class.java,
            "storedValue",
            Any::class.java,
        )
    }
}
