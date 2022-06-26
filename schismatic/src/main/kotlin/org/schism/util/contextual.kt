@file:Suppress("NOTHING_TO_INLINE", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package org.schism.util

import kotlin.internal.InlineOnly

@InlineOnly
inline fun <T> T.contextual(): T = this@contextual
