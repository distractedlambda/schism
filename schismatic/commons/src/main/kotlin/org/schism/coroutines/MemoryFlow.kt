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
import org.schism.foreign.allocateHeapSegment
import org.schism.foreign.emptyHeapSegment
import org.schism.foreign.heapCopyOf
import org.schism.math.plusExact
import org.schism.math.toIntExact
import org.schism.util.Token
import org.schism.util.loop
import java.lang.foreign.MemorySegment
import java.util.BitSet
import java.util.Objects.checkFromIndexSize

public interface MemoryFlow : Flow<MemorySegment> {
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

    public operator fun plus(other: MemoryFlow): MemoryFlow {
        val components = buildList {
            unpackComponents(this@buildList)
            other.unpackComponents(this@buildList)
        }

        return concatenateComponents(components, size plusExact other.size)
    }
}

public fun MemoryFlow(size: Long, load: suspend () -> MemorySegment): MemoryFlow {
    return when (size) {
        0L -> EmptyMemoryFlow
        else -> DeferredMemoryFlow(size, load)
    }
}

public fun MemoryFlow(size: Long, pageSize: Long, loadPage: suspend (pageIndex: Long) -> MemorySegment): MemoryFlow {
    return when (size) {
        0L -> EmptyMemoryFlow
        pageSize -> DeferredMemoryFlow(size) { loadPage(0) }
        else -> PageDeferredMemoryFlow(size, pageSize, loadPage)
    }
}

public fun emptyMemoryFlow(): MemoryFlow {
    return EmptyMemoryFlow
}

public fun memoryFlowOf(segment: MemorySegment): MemoryFlow {
    return ImmediateMemoryFlow(segment.asReadOnly())
}

public fun concatenate(vararg flows: MemoryFlow): MemoryFlow {
    return when (flows.size) {
        0 -> EmptyMemoryFlow
        1 -> flows.single()
        else -> {
            var size = 0L

            val components = buildList {
                flows.forEach {
                    size = size plusExact it.size
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
            size = size plusExact it.size
            it.unpackComponents(this@buildList)
        }
    }

    return concatenateComponents(components, size)
}

private fun concatenateComponents(components: List<MemoryFlow>, size: Long): MemoryFlow {
    return when (components.size) {
        0 -> EmptyMemoryFlow
        1 -> components.single()
        else -> ConcatenatedMemoryFlow(components, size)
    }
}

private class PageDeferredMemoryFlow(
    override val size: Long,
    private val pageSize: Long,
    private val loadPage: suspend (pageIndex: Long) -> MemorySegment,
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

        return when {
            pages[pageIndex].compareAndSet(null, newPage) -> newPage
            else -> pages[pageIndex].value!!
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

                when (firstPageIndex) {
                    lastPageIndex -> {
                        getOrCreatePage(firstPageIndex).slice(firstPageOffset, size)
                    }

                    else -> {
                        val components = MutableList(lastPageIndex - firstPageIndex + 1) {
                            getOrCreatePage(firstPageIndex + it)
                        }

                        components[0] = components
                            .first()
                            .slice(firstPageOffset)

                        components[components.lastIndex] = components
                            .last()
                            .slice(size = size - ((pages.size - 1) * pageSize) + firstPageOffset)

                        ConcatenatedMemoryFlow(components, size)
                    }
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
    override suspend fun collect(collector: FlowCollector<MemorySegment>) {
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
                    val mutableVersion = allocateHeapSegment(size)
                    val initFlags = BitSet(pages.size)
                    var uninitializedCount = pages.size

                    while (uninitializedCount != 0) {
                        val (pageIndex, data) = receive()

                        mutableVersion.asSlice(pageIndex * pageSize, pageSize).copyFrom(data)

                        if (!initFlags[pageIndex]) {
                            initFlags.set(pageIndex)
                            uninitializedCount--
                        }
                    }

                    send(heapCopyOf(mutableVersion).asReadOnly())

                    loop {
                        val (pageIndex, data) = receive()
                        mutableVersion.asSlice(pageIndex * pageSize, pageSize).copyFrom(data)
                        send(heapCopyOf(mutableVersion).asReadOnly())
                    }
                }
            }

            collector.emitAll(nextVersion)
        }
    }

    private data class PageUpdate(val pageIndex: Int, val data: MemorySegment)
}

private class DeferredMemoryFlow(override val size: Long, private val load: suspend () -> MemorySegment) : MemoryFlow {
    init {
        require(size > 0)
    }

    private val state = MutableStateFlow<Any>(EMPTY)

    override fun invalidate(offset: Long, size: Long) {
        checkFromIndexSize(offset, size, this.size)
        if (size != 0L) state.value = EMPTY
    }

    override suspend fun collect(collector: FlowCollector<MemorySegment>) {
        state.collect { lastState ->
            when {
                lastState === EMPTY -> {
                    if (state.compareAndSet(EMPTY, LOADING)) {
                        try {
                            val loaded = load()
                            check(loaded.byteSize() == size)
                            state.value = loaded.asReadOnly()
                        } catch (exception: Throwable) {
                            state.value = EMPTY
                            throw exception
                        }
                    }
                }

                lastState is MemorySegment -> {
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

private data class ConcatenatedMemoryFlow(
    private val components: List<MemoryFlow>,
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
                    else -> ConcatenatedMemoryFlow(newComponents, size)
                }
            }
        }
    }

    override fun unpackComponents(to: MutableList<MemoryFlow>) {
        to.addAll(components)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun collect(collector: FlowCollector<MemorySegment>) {
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
                    val mutableVersion = allocateHeapSegment(size)
                    val initFlags = BitSet(components.size)
                    var uninitializedCount = components.size

                    while (uninitializedCount != 0) {
                        val (componentIndex, byteOffset, data) = receive()

                        mutableVersion.asSlice(byteOffset, data.byteSize()).copyFrom(data)

                        if (!initFlags[componentIndex]) {
                            initFlags.set(componentIndex)
                            uninitializedCount--
                        }
                    }

                    send(heapCopyOf(mutableVersion).asReadOnly())

                    loop {
                        val (_, byteOffset, data) = receive()
                        mutableVersion.asSlice(byteOffset, data.byteSize()).copyFrom(data)
                        send(heapCopyOf(mutableVersion).asReadOnly())
                    }
                }
            }

            collector.emitAll(nextVersion)
        }
    }

    private data class ComponentUpdate(val componentIndex: Int, val byteOffset: Long, val data: MemorySegment)
}

private data class SlicedMemoryFlow(
    private val source: MemoryFlow,
    private val sourceOffset: Long,
    override val size: Long,
) : MemoryFlow {
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

    override suspend fun collect(collector: FlowCollector<MemorySegment>) {
        source.collect {
            collector.emit(it.asSlice(sourceOffset, size))
        }
    }
}

private data class ImmediateMemoryFlow(private val memory: MemorySegment) : MemoryFlow {
    override val size: Long get() {
        return memory.byteSize()
    }

    override fun slice(offset: Long, size: Long): MemoryFlow {
        checkFromIndexSize(offset, size, memory.byteSize())
        return when (size) {
            memory.byteSize() -> this
            else -> ImmediateMemoryFlow(memory.asSlice(offset, size))
        }
    }

    override fun unpackComponents(to: MutableList<MemoryFlow>) {
        if (memory.byteSize() != 0L) {
            to.add(this)
        }
    }

    override suspend fun collect(collector: FlowCollector<MemorySegment>) {
        collector.emit(memory)
        awaitCancellation()
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

    override fun plus(other: MemoryFlow): MemoryFlow {
        return other
    }

    override suspend fun collect(collector: FlowCollector<MemorySegment>) {
        collector.emit(emptyHeapSegment())
        awaitCancellation()
    }
}
