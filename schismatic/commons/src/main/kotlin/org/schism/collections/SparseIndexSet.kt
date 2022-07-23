package org.schism.collections

import org.schism.math.incSaturating
import java.util.TreeMap

public class SparseIndexSet private constructor(private val rangesLastsByFirsts: TreeMap<Long, Long>): MutableIndexSet {
    public constructor() : this(TreeMap())

    override val ascendingIndices: Sequence<Long> get() = sequence {
        for ((first, last) in rangesLastsByFirsts) {
            for (element in first .. last) {
                yield(element)
            }
        }
    }

    override val descendingIndices: Sequence<Long> get() = sequence {
        for ((first, last) in rangesLastsByFirsts.descendingMap()) {
            for (element in last downTo first) {
                yield(element)
            }
        }
    }

    override val ascendingRanges: Sequence<LongRange> get() = sequence {
        for ((first, last) in rangesLastsByFirsts) {
            yield(first .. last)
        }
    }

    override val descendingRanges: Sequence<LongRange> get() = sequence {
        for ((first, last) in rangesLastsByFirsts.descendingMap()) {
            yield(first .. last)
        }
    }

    override fun contains(index: Long): Boolean {
        val (_, last) = rangesLastsByFirsts.floorEntry(index) ?: return false
        return index <= last
    }

    override fun containsAll(indices: LongRange): Boolean {
        val (_, last) = rangesLastsByFirsts.floorEntry(indices.first) ?: return false
        return indices.last <= last
    }

    override fun add(index: Long) {
        val (floorFirst, floorLast) = rangesLastsByFirsts.floorEntry(index) ?: run {
            rangesLastsByFirsts[index] = index
            return
        }

        if (index <= floorLast) {
            return
        }

        val newFirst = if (index == floorLast + 1) floorFirst else index
        val newLast = if (index != Long.MAX_VALUE) rangesLastsByFirsts.remove(index + 1) ?: index else index
        rangesLastsByFirsts[newFirst] = newLast
    }

    override fun addAll(indices: LongRange) {
        if (indices.isEmpty()) {
            return
        }

        if (indices.first == indices.last) {
            add(indices.first)
            return
        }

        val firstRange = rangesLastsByFirsts.floorEntry(indices.first)
        val lastRange = rangesLastsByFirsts.floorEntry(indices.last)

        val newFirst = when {
            firstRange != null && indices.first <= firstRange.value.incSaturating() -> firstRange.key
            else -> indices.first
        }

        val newLast = when {
            lastRange != null && indices.last <= lastRange.value -> lastRange.value
            indices.last != Long.MAX_VALUE -> rangesLastsByFirsts.remove(indices.last + 1) ?: indices.last
            else -> Long.MAX_VALUE
        }

        if (newLast - newFirst > 1) {
            rangesLastsByFirsts.subMap(newFirst, false, newLast, false).clear()
        }

        rangesLastsByFirsts[newFirst] = newLast
    }

    override fun remove(index: Long) {
        val (floorFirst, floorLast) = rangesLastsByFirsts.floorEntry(index) ?: return

        if (floorLast < index) {
            return
        }

        rangesLastsByFirsts[floorFirst] = index

        if (index != floorLast) {
            rangesLastsByFirsts[index] = floorLast
        }
    }

    override fun removeAll(indices: LongRange) {
        if (indices.isEmpty()) {
            return
        }

        if (indices.first == indices.last) {
            remove(indices.first)
            return
        }

        rangesLastsByFirsts.floorEntry(indices.first)?.let { (firstRangeFirst, firstRangeLast) ->
            if (firstRangeLast < indices.first) {
                return@let
            }

            if (firstRangeFirst == indices.first) {
                rangesLastsByFirsts.remove(firstRangeFirst)
            } else {
                rangesLastsByFirsts[firstRangeFirst] = indices.first
            }

            if (firstRangeLast == indices.last) {
                return
            }

            if (firstRangeLast > indices.last) {
                rangesLastsByFirsts[indices.last + 1] = firstRangeLast
                return
            }
        }

        rangesLastsByFirsts.floorEntry(indices.last)?.let { (_, lastRangeLast) ->
            if (lastRangeLast > indices.last) {
                rangesLastsByFirsts[indices.last + 1] = lastRangeLast
            }
        }

        rangesLastsByFirsts.subMap(indices.first, true, indices.last, true).clear()
    }

    override fun clear() {
        rangesLastsByFirsts.clear()
    }

    override fun toSparseIndexSet(): SparseIndexSet {
        return SparseIndexSet(TreeMap(rangesLastsByFirsts))
    }

    override fun equals(other: Any?): Boolean {
        return when {
            other !is IndexSet -> false

            other is SparseIndexSet -> {
                rangesLastsByFirsts == other.rangesLastsByFirsts
            }

            else -> {
                val thisIterator = rangesLastsByFirsts.iterator()
                val otherIterator = other.ascendingRanges.iterator()

                while (thisIterator.hasNext()) {
                    if (!otherIterator.hasNext()) {
                        return false
                    }

                    val thisRange = thisIterator.next()
                    val otherRange = otherIterator.next()

                    if (thisRange.key != otherRange.first || thisRange.value != otherRange.last) {
                        return false
                    }
                }

                true
            }
        }
    }

    override fun hashCode(): Int {
        var hash = 0

        for ((first, last) in rangesLastsByFirsts) {
            hash = 31 * (31 * hash + first.hashCode()) + last.hashCode()
        }

        return hash
    }

    override fun toString(): String {
        val iterator = rangesLastsByFirsts.iterator()

        if (!iterator.hasNext()) {
            return "[]"
        }

        return buildString {
            append('[')

            appendRange(iterator.next())

            while (iterator.hasNext()) {
                append(", ")
                appendRange(iterator.next())
            }

            append(']')
        }
    }
}

private fun StringBuilder.appendRange(range: Map.Entry<Long, Long>) {
    val (first, last) = range

    append(first)

    if (first != last) {
        append("..")
        append(last)
    }
}
