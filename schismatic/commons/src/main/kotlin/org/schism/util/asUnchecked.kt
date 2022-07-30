package org.schism.util

@Suppress("NOTHING_TO_INLINE")
public inline fun <T> Any?.asUnchecked(): T {
    return (@Suppress("UNCHECKED_CAST") (this as T))
}
