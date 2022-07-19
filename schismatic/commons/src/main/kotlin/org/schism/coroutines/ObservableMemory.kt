package org.schism.coroutines

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import org.schism.math.timesExact
import org.schism.math.toIntExact
import org.schism.memory.Memory
import org.schism.memory.allocateHeapMemory
import org.schism.util.Token
import java.util.WeakHashMap

public class ObservableMemory(private val source: Source, private val pageSize: Long) {
    private val pageNodes = WeakHashMap<Long, MutableStateFlow<Any>>()

    public fun invalidate(offset: Long, size: Long) {
        TODO()
    }

    public fun observeAsFlow(offset: Long, size: Long): Flow<Memory> {
        require(offset >= 0)
        require(size > 0)
        require(Long.MAX_VALUE - offset >= size)

        val firstPage = offset / pageSize
        val lastPage = (offset + size - 1) / pageSize
        val firstPageStart = offset % pageSize

        return if (firstPage == lastPage) {
            PageFlow(firstPage).map { page ->
                page.slice(firstPageStart, size)
            }
        } else {
            val componentPages = Array((lastPage - firstPage + 1).toIntExact()) {
                PageFlow(it.toLong())
            }

            combine(*componentPages) { pages ->
                TODO()
            }
        }
    }

    public fun interface Source {
        public suspend fun read(offset: Long, destination: Memory)
    }

    private inner class PageFlow(private val pageIndex: Long) : Flow<Memory> {
        override suspend fun collect(collector: FlowCollector<Memory>) {
            val node = synchronized(pageNodes) {
                pageNodes.getOrPut(pageIndex) {
                    MutableStateFlow(EMPTY)
                }
            }

            node.collect { state ->
                when {
                    state === EMPTY -> {
                        if (node.compareAndSet(EMPTY, PAGE_LOADING)) {
                            try {
                                val page = allocateHeapMemory(pageSize.toIntExact())
                                source.read(pageIndex timesExact pageSize, page)
                                node.value = page.asReadOnly()
                            } catch (exception: Throwable) {
                                node.value = EMPTY
                                throw exception
                            }
                        }
                    }

                    state is Memory -> {
                        collector.emit(state)
                    }
                }
            }
        }
    }

    private companion object {
        private val EMPTY = Token("EMPTY")
        private val PAGE_LOADING = Token("PAGE_LOADING")
    }
}
