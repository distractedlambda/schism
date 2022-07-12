package org.schism.util

import java.util.WeakHashMap

public class IdMapping<E : Any> {
    private var nextId = 0L
    private val objectsToIds = WeakHashMap<E, Long>()

    public operator fun get(value: E): Long = synchronized(objectsToIds) {
        return objectsToIds.getOrPut(value) { nextId++ }
    }
}
