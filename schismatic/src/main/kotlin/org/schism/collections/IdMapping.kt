package org.schism.collections

import java.util.WeakHashMap

class IdMapping<E : Any> {
    private var nextId = 0L
    private val objectsToIds = WeakHashMap<E, Long>()

    operator fun get(value: E): Long = synchronized(objectsToIds) {
        return objectsToIds.getOrPut(value) { nextId++ }
    }
}
