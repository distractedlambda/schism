package org.schism.concurrent

import java.lang.Thread.currentThread
import java.lang.invoke.MethodHandles
import java.util.concurrent.locks.LockSupport
import java.util.concurrent.locks.LockSupport.park
import java.util.concurrent.locks.LockSupport.unpark
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@Suppress("UNCHECKED_CAST")
internal class BlockingStateFlowImpl<T>(initialValue: T) : MutableBlockingStateFlow<T> {
    private val mutationAndSubscriptionLock = ReentrantLock()
    private var storedValue = initialValue
    private var subscriptions: Any? = null

    override var value: T
        get() = vhStoredValue.getVolatile(this) as T

        set(newValue) = mutationAndSubscriptionLock.withLockInterruptibly {
            if (newValue == storedValue) {
                return
            }

            storedValue = newValue

            when (val subscriptions = subscriptions) {
                null -> {}

                is Subscription<*> -> {
                    (subscriptions as Subscription<T>).publish(newValue)
                }

                is Collection<*> -> subscriptions.forEach {
                    (it as Subscription<T>).publish(newValue)
                }

                else -> error("unexpected subscription set representation")
            }
        }

    override fun collect(collector: BlockingFlowCollector<T>) {
        var item: T
        val subscription: Subscription<T>

        mutationAndSubscriptionLock.withLockInterruptibly {
            item = storedValue
            subscription = Subscription()

            when (val currentSubscriptions = subscriptions) {
                null -> {
                    subscriptions = subscription
                }

                is Subscription<*> -> {
                    subscriptions = hashSetOf(currentSubscriptions, subscription)
                }

                is MutableCollection<*> -> {
                    (currentSubscriptions as MutableCollection<Subscription<T>>).add(subscription)
                }

                else -> error("unexpected subscription set representation")
            }
        }

        try {
            while (true) {
                collector.emit(item)

                var nextValue: T

                do {
                    nextValue = subscription.next()
                } while (nextValue == item)

                item = nextValue
            }
        } finally {
            mutationAndSubscriptionLock.withLock {
                when (val currentSubscriptions = subscriptions) {
                    subscription -> {
                        subscriptions = null
                    }

                    is MutableCollection<*> -> {
                        (currentSubscriptions as MutableCollection<Subscription<T>>).remove(subscription)
                    }

                    else -> error("unexpected subscription set representation")
                }
            }
        }
    }

    private class Subscription<T> {
        private val thread: Thread = currentThread()

        @Volatile private var newValue: Any? = NoValue

        fun publish(value: T) {
            newValue = value
            unpark(thread)
        }

        fun next(): T {
            while (newValue === NoValue) {
                park()

                if (Thread.interrupted()) {
                    throw InterruptedException()
                }
            }

            return vhNewValue.getAndSet(this, NoValue) as T
        }

        private object NoValue

        private companion object {
            private val vhNewValue = MethodHandles.lookup().findVarHandle(
                Subscription::class.java,
                "newValue",
                Any::class.java,
            )

            init {
                LockSupport::class.java
            }
        }
    }

    private companion object {
        private val vhStoredValue = MethodHandles.lookup().findVarHandle(
            BlockingStateFlowImpl::class.java,
            "storedValue",
            Any::class.java,
        )
    }
}
