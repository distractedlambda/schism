package org.schism.util

public fun unreachable(): Nothing {
    throw IllegalStateException("unreachable() called")
}
