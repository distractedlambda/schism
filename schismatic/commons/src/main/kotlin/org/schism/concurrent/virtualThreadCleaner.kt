package org.schism.concurrent

import java.lang.ref.Cleaner

public fun virtualThreadCleaner(threadName: String? = null): Cleaner {
    return Cleaner.create {
        virtualThread(
            start = false,
            name = threadName,
            allowSetThreadLocals = false,
            inheritInheritableThreadLocals = false,
            block = it,
        )
    }
}
