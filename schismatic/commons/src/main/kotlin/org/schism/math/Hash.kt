package org.schism.math

public infix fun Int.foldHashCode(rhs: Int): Int {
    // FIXME: is this better?
    // val fullProduct = ((this xor rhs).toLong() and 0xffff_ffffL) * 0xb4e11cebL
    // return fullProduct.toInt() xor (fullProduct ushr 32).toInt()
    return 31 * this + rhs
}
