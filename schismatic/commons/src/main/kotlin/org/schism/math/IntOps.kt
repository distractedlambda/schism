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

public fun Int.incSaturating(): Int {
    return if (this == Int.MAX_VALUE) this else this.inc()
}

public fun Int.decSaturating(): Int {
    return if (this == Int.MIN_VALUE) this else this.dec()
}

public fun Long.incSaturating(): Long {
    return if (this == Long.MAX_VALUE) this else this.inc()
}

public fun Long.decSaturating(): Long {
    return if (this == Long.MIN_VALUE) this else this.dec()
}
