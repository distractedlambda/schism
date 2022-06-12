package org.schism.foreign

import java.lang.Math.addExact

fun Long.isValidAlignment(): Boolean {
    return this > 0 && this.countOneBits() == 1
}

fun requireValidAlignment(alignment: Long) {
    require(alignment.isValidAlignment()) {
        "$alignment is not a valid alignment value"
    }
}

fun Long.isAlignedTo(alignment: Long): Boolean {
    requireValidAlignment(alignment)
    return this and (alignment - 1) == 0L
}

fun Long.requireAlignedTo(alignment: Long) {
    require(isAlignedTo(alignment)) {
        "$this is not $alignment-byte aligned"
    }
}

fun Long.forwardsAlignmentOffsetTo(alignment: Long): Long {
    requireValidAlignment(alignment)
    return (alignment - (this and (alignment - 1))) and (alignment - 1)
}

fun Long.alignForwardsTo(alignment: Long): Long {
    return addExact(this, forwardsAlignmentOffsetTo(alignment))
}
