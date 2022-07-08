package org.schism.ref

import java.lang.ref.Cleaner

public val SharedCleaner: Cleaner = Cleaner.create(Thread.ofPlatform().name("SharedCleaner").factory())

public fun Any.registerCleanup(action: Runnable) {
    SharedCleaner.register(this, action)
}
