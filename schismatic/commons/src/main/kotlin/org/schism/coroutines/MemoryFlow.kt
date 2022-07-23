package org.schism.coroutines

import kotlinx.atomicfu.atomicArrayOfNulls
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.launch
import org.schism.math.toIntExact
import org.schism.memory.Memory
import org.schism.memory.allocateHeapMemory
import org.schism.memory.emptyHeapMemory
import org.schism.memory.heapCopyOf
import org.schism.memory.memcpy
import org.schism.util.Token
import org.schism.util.loop
import java.util.BitSet
import java.util.Objects.checkFromIndexSize

public interface MemoryFlow : Flow<Memory> {
    public val size: Long

    public fun invalidate(offset: Long = 0, size: Long = this.size - offset) {
        checkFromIndexSize(offset, size, this.size)
    }

    public fun slice(offset: Long = 0, size: Long = this.size - offset): MemoryFlow {
        checkFromIndexSize(offset, size, this.size)
        return when (size) {
            0L -> EmptyMemoryFlow
            this.size -> this
            else -> SlicedMemoryFlow(this, offset, size)
        }
    }

    public fun unpackComponents(to: MutableList<MemoryFlow>) {
        to.add(this)
    }
}

public fun MemoryFlow(size: Long, load: suspend () -> Memory): MemoryFlow {
    require(size >= 0)
    return if (size == 0L) {
        EmptyMemoryFlow
    } else {
        DeferredMemoryFlow(size, load)
    }
}

private fun concatenateComponents(components: List<MemoryFlow>, size: Long): MemoryFlow {
    return when (components.size) {
        0 -> EmptyMemoryFlow
        1 -> components.single()
        else -> ConcatenatedMemoryFlow(components.toTypedArray(), size)
    }
}

public fun concatenate(vararg flows: MemoryFlow): MemoryFlow {
    return when (flows.size) {
        0 -> EmptyMemoryFlow
        1 -> flows.single()
        else -> {
            var size = 0L

            val components = buildList {
                flows.forEach {
                    size += it.size
                    it.unpackComponents(this@buildList)
                }
            }

            return concatenateComponents(components, size)
        }
    }
}

public fun concatenate(flows: Iterable<MemoryFlow>): MemoryFlow {
    var size = 0L

    val components = buildList {
        flows.forEach {
            size += it.size
            it.unpackComponents(this@buildList)
        }
    }

    return concatenateComponents(components, size)
}

private class PageDeferredMemoryFlow(
    override val size: Long,
    private val pageSize: Long,
    private val loadPage: suspend (pageIndex: Long) -> Memory,
) : MemoryFlow {
    init {
        require(size > 0)
        require(pageSize in 1 .. size)
        require(size % pageSize == 0L)
        require(size / pageSize <= Int.MAX_VALUE)
    }

    private val pages = atomicArrayOfNulls<MemoryFlow>((size / pageSize).toIntExact())

    private fun getOrCreatePage(pageIndex: Int) : MemoryFlow {
        pages[pageIndex].value?.let { return it }

        val newPage = DeferredMemoryFlow(pageSize) {
            loadPage(pageIndex.toLong())
        }

        return if (pages[pageIndex].compareAndSet(null, newPage)) {
            newPage
        } else {
            pages[pageIndex].value!!
        }
    }

    override fun invalidate(offset: Long, size: Long) {
        checkFromIndexSize(offset, size, this.size)

        if (size == 0L) {
            return
        }

        val firstPageIndex = (offset / pageSize).toIntExact()
        val lastPageIndex = ((offset + size - 1) / pageSize).toIntExact()

        for (pageIndex in firstPageIndex .. lastPageIndex) {
            pages[pageIndex].value?.invalidate()
        }
    }

    override fun slice(offset: Long, size: Long): MemoryFlow {
        checkFromIndexSize(offset, size, this.size)

        return when (size) {
            0L -> EmptyMemoryFlow
            this.size -> this
            else -> {
                val firstPageIndex = (offset / pageSize).toIntExact()
                val lastPageIndex = ((offset + size - 1) / pageSize).toIntExact()
                val firstPageOffset = offset % pageSize

                if (firstPageIndex == lastPageIndex) {
                    getOrCreatePage(firstPageIndex).slice(firstPageOffset, size)
                } else {
                    val componentPages = Array(lastPageIndex - firstPageIndex + 1) {
                        getOrCreatePage(firstPageIndex + it)
                    }

                    componentPages[0] = componentPages
                        .first()
                        .slice(firstPageOffset)

                    componentPages[componentPages.lastIndex] = componentPages
                        .last()
                        .slice(size = size - ((pages.size - 1) * pageSize) + firstPageOffset)

                    ConcatenatedMemoryFlow(componentPages, size)
                }
            }
        }
    }

    override fun unpackComponents(to: MutableList<MemoryFlow>) {
        for (i in 0 until pages.size) {
            to.add(getOrCreatePage(i))
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun collect(collector: FlowCollector<Memory>) {
        coroutineScope {
            val nextVersion = produce(capacity = Channel.CONFLATED) {
                val pageUpdates = produce {
                    for (pageIndex in 0 until pages.size) {
                        val page = getOrCreatePage(pageIndex)
                        launch {
                            page.collect {
                                send(PageUpdate(pageIndex, it))
                            }
                        }
                    }
                }

                pageUpdates.consume {
                    val mutableVersion = allocateHeapMemory(size.toIntExact()) // FIXME: support larger memories
                    val initFlags = BitSet(pages.size)
                    var uninitializedCount = pages.size

                    while (uninitializedCount != 0) {
                        val (pageIndex, data) = receive()

                        memcpy(mutableVersion.slice(pageIndex * pageSize, pageSize), data)

                        if (!initFlags[pageIndex]) {
                            initFlags.set(pageIndex)
                            uninitializedCount--
                        }
                    }

                    send(heapCopyOf(mutableVersion).asReadOnly())

                    loop {
                        val (pageIndex, data) = receive()
                        memcpy(mutableVersion.slice(pageIndex * pageSize, pageSize), data)
                        send(heapCopyOf(mutableVersion).asReadOnly())
                    }
                }
            }

            collector.emitAll(nextVersion)
        }
    }

    private data class PageUpdate(val pageIndex: Int, val data: Memory)
}

private class DeferredMemoryFlow(override val size: Long, private val load: suspend () -> Memory) : MemoryFlow {
    init {
        require(size > 0)
    }

    private val state = MutableStateFlow<Any>(EMPTY)

    override fun invalidate(offset: Long, size: Long) {
        checkFromIndexSize(offset, size, this.size)
        if (size != 0L) state.value = EMPTY
    }

    override suspend fun collect(collector: FlowCollector<Memory>) {
        state.collect { lastState ->
            when {
                lastState === EMPTY -> {
                    if (state.compareAndSet(EMPTY, LOADING)) {
                        try {
                            val loaded = load()
                            check(loaded.size == size)
                            state.value = loaded.asReadOnly()
                        } catch (exception: Throwable) {
                            state.value = EMPTY
                            throw exception
                        }
                    }
                }

                lastState is Memory -> {
                    collector.emit(lastState)
                }
            }
        }
    }

    private companion object {
        private val EMPTY = Token("EMPTY")

        private val LOADING = Token("LOADING")
    }
}

private class ConcatenatedMemoryFlow(
    private val components: Array<out MemoryFlow>,
    override val size: Long,
) : MemoryFlow {
    init {
        require(components.size > 1)
        require(size > 0)
    }

    override fun invalidate(offset: Long, size: Long) {
        checkFromIndexSize(offset, size, this.size)

        var remainingOffset = offset
        var remainingSize = size

        for (component in components) {
            if (remainingSize == 0L) {
                return
            }

            val componentSize = component.size

            if (remainingOffset < componentSize) {
                val invalidatedSize = minOf(remainingSize, componentSize - remainingOffset)
                component.invalidate(remainingOffset, invalidatedSize)
                remainingOffset = 0
                remainingSize -= invalidatedSize
            }
        }
    }

    override fun slice(offset: Long, size: Long): MemoryFlow {
        checkFromIndexSize(offset, size, this.size)

        return when (size) {
            0L -> EmptyMemoryFlow
            this.size -> this
            else -> {
                val newComponents = buildList {
                    var remainingOffset = offset
                    var remainingSize = size

                    for (component in components) {
                        val componentSize = component.size

                        if (remainingOffset < componentSize) {
                            val sliceSize = minOf(remainingSize, componentSize - remainingOffset)
                            add(component.slice(remainingOffset, sliceSize))
                            remainingOffset = 0
                            remainingSize -= sliceSize
                        }

                        if (remainingSize == 0L) {
                            return@buildList
                        }
                    }
                }

                when (newComponents.size) {
                    1 -> newComponents.single()
                    else -> ConcatenatedMemoryFlow(newComponents.toTypedArray(), size)
                }
            }
        }
    }

    override fun unpackComponents(to: MutableList<MemoryFlow>) {
        to.addAll(components)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun collect(collector: FlowCollector<Memory>) {
        coroutineScope {
            val nextVersion = produce(capacity = Channel.CONFLATED) {
                val componentUpdates = produce {
                    var nextOffset = 0L

                    components.forEachIndexed { componentIndex, component ->
                        val offset = nextOffset

                        launch {
                            component.collect {
                                send(ComponentUpdate(componentIndex, offset, it))
                            }
                        }

                        nextOffset += component.size
                    }
                }

                componentUpdates.consume {
                    val mutableVersion = allocateHeapMemory(size.toIntExact()) // FIXME: support larger memories
                    val initFlags = BitSet(components.size)
                    var uninitializedCount = components.size

                    while (uninitializedCount != 0) {
                        val (componentIndex, byteOffset, data) = receive()

                        memcpy(mutableVersion.slice(byteOffset, data.size), data)

                        if (!initFlags[componentIndex]) {
                            initFlags.set(componentIndex)
                            uninitializedCount--
                        }
                    }

                    send(heapCopyOf(mutableVersion).asReadOnly())

                    loop {
                        val (_, byteOffset, data) = receive()
                        memcpy(mutableVersion.slice(byteOffset, data.size), data)
                        send(heapCopyOf(mutableVersion).asReadOnly())
                    }
                }
            }

            collector.emitAll(nextVersion)
        }
    }

    private data class ComponentUpdate(val componentIndex: Int, val byteOffset: Long, val data: Memory)
}

private class SlicedMemoryFlow(val source: MemoryFlow, val sourceOffset: Long, override val size: Long) : MemoryFlow {
    init {
        require(size > 0)
    }

    override fun invalidate(offset: Long, size: Long) {
        checkFromIndexSize(offset, size, this.size)
        source.invalidate(sourceOffset + offset, size)
    }

    override fun slice(offset: Long, size: Long): MemoryFlow {
        checkFromIndexSize(offset, size, this.size)
        return when (size) {
            0L -> EmptyMemoryFlow
            this.size -> this
            else -> SlicedMemoryFlow(source, sourceOffset + offset, size)
        }
    }

    override suspend fun collect(collector: FlowCollector<Memory>) {
        source.collect {
            collector.emit(it.slice(sourceOffset, size))
        }
    }
}

private object EmptyMemoryFlow : MemoryFlow {
    override val size: Long get() {
        return 0
    }

    override fun slice(offset: Long, size: Long): MemoryFlow {
        checkFromIndexSize(offset, size, 0)
        return EmptyMemoryFlow
    }

    override fun unpackComponents(to: MutableList<MemoryFlow>) {
        return
    }

    override suspend fun collect(collector: FlowCollector<Memory>) {
        collector.emit(emptyHeapMemory())
        awaitCancellation()
    }
}
