package org.schism.collections

import org.schism.math.incSaturating
import java.util.TreeMap

public class SparseIndexSet : MutableIndexSet {
    private val rangesLastsByFirsts = TreeMap<Long, Long>()

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
}
