package org.schism.collections

public interface MutableIndexSet : IndexSet {
    public fun add(index: Long)

    public fun addAll(indices: LongRange)

    public fun remove(index: Long)

    public fun removeAll(indices: LongRange)
}
