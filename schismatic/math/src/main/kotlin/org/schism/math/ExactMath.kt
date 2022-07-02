package org.schism.math

public fun Long.toIntExact(): Int {
    return Math.toIntExact(this)
}

public fun Int.negateExact(): Int {
    return Math.negateExact(this)
}

public fun Long.negateExact(): Long {
    return Math.negateExact(this)
}

public fun Int.incExact(): Int {
    return Math.incrementExact(this)
}

public fun Long.incExact(): Long {
    return Math.incrementExact(this)
}

public fun Int.decExact(): Int {
    return Math.decrementExact(this)
}

public fun Long.decExact(): Long {
    return Math.decrementExact(this)
}

public infix fun Int.plusExact(rhs: Int): Int {
    return Math.addExact(this, rhs)
}

public infix fun Long.plusExact(rhs: Long): Long {
    return Math.addExact(this, rhs)
}

public infix fun Int.minusExact(rhs: Int): Int {
    return Math.subtractExact(this, rhs)
}

public infix fun Long.minusExact(rhs: Long): Long {
    return Math.subtractExact(this, rhs)
}

public infix fun Int.timesExact(rhs: Int): Int {
    return Math.multiplyExact(this, rhs)
}

public infix fun Long.timesExact(rhs: Long): Long {
    return Math.multiplyExact(this, rhs)
}
