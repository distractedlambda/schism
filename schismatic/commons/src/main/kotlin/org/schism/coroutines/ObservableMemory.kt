package org.schism.coroutines

import kotlinx.atomicfu.atomicArrayOfNulls
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.schism.math.timesExact
import org.schism.math.toIntExact
import org.schism.memory.Memory
import org.schism.memory.allocateHeapMemory
import org.schism.memory.emptyHeapMemory
import org.schism.util.Token
import java.util.Objects.checkFromIndexSize

public class ObservableMemory(public val source: Source, public val sourceSize: Long, public val pageSize: Int) {
    init {
        require(sourceSize > 0)
        require(pageSize in 1 .. sourceSize)
        require(sourceSize % pageSize == 0L)
        require(sourceSize / pageSize <= Int.MAX_VALUE)
    }

    private val pageFlows = atomicArrayOfNulls<PageFlow>((sourceSize / pageSize).toIntExact())

    public fun invalidate(offset: Long, size: Long) {
        checkFromIndexSize(offset, size, sourceSize)

        if (size == 0L) {
            return
        }

        val firstPage = (offset / pageSize).toIntExact()
        val lastPage = ((offset + size - 1) / pageSize).toIntExact()

        for (pageIndex in firstPage .. lastPage) {
            pageFlows[pageIndex].value?.invalidate()
        }
    }

    public fun observeAsFlow(offset: Long, size: Long): Flow<Memory> {
        checkFromIndexSize(offset, size, sourceSize)

        if (size == 0L) {
            return flowOf(emptyHeapMemory())
        }

        val firstPage = (offset / pageSize).toIntExact()
        val lastPage = ((offset + size - 1) / pageSize).toIntExact()
        val firstPageOffset = offset % pageSize

        return if (firstPage == lastPage) {
            pageFlow(firstPage).map { page ->
                page.slice(firstPageOffset, size)
            }
        } else {
            val componentPages = Array(lastPage - firstPage + 1) {
                pageFlow(firstPage + it)
            }

            combine(*componentPages) { pages ->
                val combined = allocateHeapMemory(size.toIntExact())

                combined.encoder().apply {
                    putBytes(pages[0].slice(offset = firstPageOffset))

                    for (i in 1 until pages.lastIndex) {
                        putBytes(pages[i])
                    }

                    putBytes(pages.last().slice(size = size - ((pages.size - 1) * pageSize) + firstPageOffset))
                }

                combined.asReadOnly()
            }
        }
    }

    private fun pageFlow(pageIndex: Int) : PageFlow {
        pageFlows[pageIndex].value?.let { return it }

        val newFlow = PageFlow(pageIndex)

        return if (pageFlows[pageIndex].compareAndSet(null, newFlow)) {
            newFlow
        } else {
            pageFlows[pageIndex].value!!
        }
    }

    public fun interface Source {
        public suspend fun read(offset: Long, destination: Memory)
    }

    private inner class PageFlow(private val pageIndex: Int) : Flow<Memory> {
        private val state = MutableStateFlow<Any>(EMPTY)

        fun invalidate() {
            state.value = EMPTY
        }

        override suspend fun collect(collector: FlowCollector<Memory>) {
            state.collect { lastState ->
                when {
                    lastState === EMPTY -> {
                        if (state.compareAndSet(EMPTY, PAGE_LOADING)) {
                            try {
                                val page = allocateHeapMemory(pageSize)
                                source.read(pageIndex.toLong() timesExact pageSize, page)
                                state.value = page.asReadOnly()
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
    }
}

private val EMPTY = Token("EMPTY")

private val PAGE_LOADING = Token("PAGE_LOADING")
