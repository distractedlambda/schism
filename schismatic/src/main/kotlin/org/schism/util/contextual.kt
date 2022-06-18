@file:Suppress("NOTHING_TO_INLINE", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package org.schism.util

import kotlin.internal.InlineOnly

context (T) @InlineOnly
inline fun <T> contextual(): T = this@T
