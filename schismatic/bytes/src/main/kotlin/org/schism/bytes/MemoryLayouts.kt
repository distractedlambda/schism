package org.schism.bytes

import java.lang.foreign.GroupLayout
import java.lang.foreign.MemoryLayout
import java.lang.foreign.MemoryLayout.paddingLayout
import java.lang.foreign.MemoryLayout.sequenceLayout
import java.lang.foreign.MemoryLayout.structLayout
import java.lang.foreign.SequenceLayout

public fun MemoryLayout.sequence(elementCount: Long): SequenceLayout {
    return sequenceLayout(elementCount, this)
}

public  fun cStructLayout(vararg elements: MemoryLayout): GroupLayout {
    var nextOffset = 0L
    var greatestAlignment = 1L
    val paddedElements = mutableListOf<MemoryLayout>()

    for (element in elements) {
        greatestAlignment = maxOf(greatestAlignment, element.byteAlignment())

        val alignmentPadding = nextOffset.forwardsAlignmentOffsetTo(element.byteAlignment())
        if (alignmentPadding != 0L) {
            paddedElements.add(paddingLayout(alignmentPadding * 8))
            nextOffset += alignmentPadding
        }

        paddedElements.add(element)
        nextOffset += element.byteSize()
    }

    val trailingPadding = nextOffset.forwardsAlignmentOffsetTo(greatestAlignment)
    if (trailingPadding != 0L) {
        paddedElements.add(paddingLayout(trailingPadding * 8))
    }

    return structLayout(*paddedElements.toTypedArray())
}
