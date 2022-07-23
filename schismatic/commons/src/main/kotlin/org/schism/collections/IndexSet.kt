package org.schism.collections

public interface IndexSet {
    public operator fun contains(index: Long): Boolean

    public fun containsAll(indices: LongRange): Boolean
}
