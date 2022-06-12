@file:Suppress("NOTHING_TO_INLINE")

package org.schism.foreign

import java.lang.Math.multiplyExact

@JvmInline value class Index(inline val value: Long) {
    inline fun toByteOffset(stride: Int): ByteOffset {
        return multiplyExact(value, stride).byteOffset
    }

    inline fun toByteOffset(stride: Long): ByteOffset {
        return multiplyExact(value, stride).byteOffset
    }
}

inline val Long.index: Index get() = Index(this)
