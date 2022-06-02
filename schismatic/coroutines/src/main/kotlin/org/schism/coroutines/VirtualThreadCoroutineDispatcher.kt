package org.schism.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Runnable
import java.lang.Thread.startVirtualThread
import kotlin.coroutines.CoroutineContext

public object VirtualThreadCoroutineDispatcher : CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        startVirtualThread(block)
    }

    @ExperimentalCoroutinesApi
    override fun limitedParallelism(parallelism: Int): Nothing {
        throw UnsupportedOperationException()
    }
}

@Suppress("unused")
public val Dispatchers.Virtual: VirtualThreadCoroutineDispatcher
    get() = VirtualThreadCoroutineDispatcher
