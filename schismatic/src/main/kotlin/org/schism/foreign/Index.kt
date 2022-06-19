@file:Suppress("NOTHING_TO_INLINE", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package org.schism.foreign

import java.lang.Math.multiplyExact
import kotlin.internal.InlineOnly

@JvmInline value class Index(inline val value: Long) {
    @InlineOnly inline fun toByteOffset(stride: Int): ByteOffset {
        return multiplyExact(value, stride).byteOffset
    }

    @InlineOnly inline fun toByteOffset(stride: Long): ByteOffset {
        return multiplyExact(value, stride).byteOffset
    }
}

@InlineOnly inline val Long.index: Index
    get() = Index(this)
