package org.schism.collections

public interface IndexSet {
    public val ascendingIndices: Sequence<Long>

    public val descendingIndices: Sequence<Long>

    public val ascendingRanges: Sequence<LongRange>

    public val descendingRanges: Sequence<LongRange>

    public operator fun contains(index: Long): Boolean

    public fun containsAll(indices: LongRange): Boolean

    public operator fun plus(index: Long): IndexSet {
        return toMutableIndexSet().apply {
            add(index)
        }
    }

    public operator fun plus(indices: LongRange): IndexSet {
        return toMutableIndexSet().apply {
            addAll(indices)
        }
    }

    public operator fun plus(indices: IndexSet): IndexSet {
        return toMutableIndexSet().apply {
            addAll(indices)
        }
    }

    public operator fun minus(index: Long): IndexSet {
        return toMutableIndexSet().apply {
            remove(index)
        }
    }

    public operator fun minus(indices: LongRange): IndexSet {
        return toMutableIndexSet().apply {
            removeAll(indices)
        }
    }

    public operator fun minus(indices: IndexSet): IndexSet {
        return toMutableIndexSet().apply {
            removeAll(indices)
        }
    }

    public fun toIndexSet(): IndexSet {
        return toMutableIndexSet()
    }

    public fun toMutableIndexSet(): MutableIndexSet {
        return toSparseIndexSet()
    }

    public fun toSparseIndexSet(): SparseIndexSet {
        return SparseIndexSet().apply {
            for (range in ascendingRanges) {
                addAll(range)
            }
        }
    }
}
