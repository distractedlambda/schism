package org.schism.math

public infix fun Int.ceilDiv(rhs: Int): Int {
    return Math.ceilDiv(this, rhs)
}

public infix fun Long.ceilDiv(rhs: Int): Long {
    return Math.ceilDiv(this, rhs)
}

public infix fun Long.ceilDiv(rhs: Long): Long {
    return Math.ceilDiv(this, rhs)
}

public infix fun Int.ceilDivExact(rhs: Int): Int {
    return Math.ceilDivExact(this, rhs)
}

public infix fun Long.ceilDivExact(rhs: Long): Long {
    return Math.ceilDivExact(this, rhs)
}
