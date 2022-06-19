@file:Suppress("NOTHING_TO_INLINE", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package org.schism.math

import kotlin.internal.InlineOnly

@InlineOnly inline fun Long.toIntExact(): Int {
    return Math.toIntExact(this)
}

@InlineOnly inline fun Int.negateExact(): Int {
    return Math.negateExact(this)
}

@InlineOnly inline fun Long.negateExact(): Long {
    return Math.negateExact(this)
}

@InlineOnly inline fun Int.incExact(): Int {
    return Math.incrementExact(this)
}

@InlineOnly inline fun Long.incExact(): Long {
    return Math.incrementExact(this)
}

@InlineOnly inline fun Int.decExact(): Int {
    return Math.decrementExact(this)
}

@InlineOnly inline fun Long.decExact(): Long {
    return Math.decrementExact(this)
}

@InlineOnly inline infix fun Int.plusExact(rhs: Int): Int {
    return Math.addExact(this, rhs)
}

@InlineOnly inline infix fun Long.plusExact(rhs: Long): Long {
    return Math.addExact(this, rhs)
}

@InlineOnly inline infix fun Int.minusExact(rhs: Int): Int {
    return Math.subtractExact(this, rhs)
}

@InlineOnly inline infix fun Long.minusExact(rhs: Long): Long {
    return Math.subtractExact(this, rhs)
}

@InlineOnly inline infix fun Int.timesExact(rhs: Int): Int {
    return Math.multiplyExact(this, rhs)
}

@InlineOnly inline infix fun Long.timesExact(rhs: Long): Long {
    return Math.multiplyExact(this, rhs)
}
