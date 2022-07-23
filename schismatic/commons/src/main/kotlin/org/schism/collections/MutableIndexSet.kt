package org.schism.collections

public interface MutableIndexSet : IndexSet {
    public fun clear()

    public fun add(index: Long)

    public fun addAll(indices: LongRange)

    public fun addAll(indices: IndexSet) {
        for (range in indices.ascendingRanges) {
            addAll(range)
        }
    }

    public fun remove(index: Long)

    public fun removeAll(indices: LongRange)

    public fun removeAll(indices: IndexSet) {
        for (range in indices.ascendingRanges) {
            removeAll(range)
        }
    }
}
