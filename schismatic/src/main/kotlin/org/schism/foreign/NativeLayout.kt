package org.schism.foreign

import java.lang.foreign.MemoryLayout

data class NativeLayout(val size: Long, val alignment: Long) {
    init {
        require(size >= 0L)
        requireValidAlignment(alignment)
    }

    val stride = size.alignForwardsTo(alignment)

    fun legalizeForC(): NativeLayout {
        return when (size) {
            0L -> NativeLayout(size = alignment, alignment = alignment)
            stride -> this
            else -> NativeLayout(size = stride, alignment = alignment)
        }
    }
}

fun MemoryLayout.toNativeLayout(): NativeLayout {
    return NativeLayout(byteSize(), byteAlignment())
}
