package org.schism.ffi

import org.schism.math.toIntExact

public typealias CChar = Byte

public typealias CUnsignedChar = UByte

public typealias CShort = Short

public typealias CUnsignedShort = UShort

public typealias CInt = Int

public typealias CUnsignedInt = UInt

public typealias CFloat = Float

public typealias CDouble = Double

public typealias CIntPtrT = CPtrDiffT

public typealias CUIntPtrT = CSizeT

public typealias CSSizeT = CPtrDiffT

@JvmInline public value class CLong internal constructor(private val value: Long) {
    public fun toIntExact(): Int {
        return if (C_LONG_IS_4_BYTES) {
            value.toInt()
        } else {
            value.toIntExact()
        }
    }

    public fun toInt(): Int {
        return value.toInt()
    }

    public fun toLong(): Long {
        return value
    }

    public operator fun unaryPlus(): CLong {
        return this
    }

    public operator fun unaryMinus(): CLong {
        return CLong(if (C_LONG_IS_4_BYTES) {
            (-value.toInt()).toLong()
        } else {
            -value
        })
    }

    public operator fun plus(other: CLong): CLong {
        return CLong(if (C_LONG_IS_4_BYTES) {
            (value.toInt() + other.value.toInt()).toLong()
        } else {
            value + other.value
        })
    }

    public operator fun minus(other: CLong): CLong {
        return CLong(if (C_LONG_IS_4_BYTES) {
            (value.toInt() - other.value.toInt()).toLong()
        } else {
            value - other.value
        })
    }

    public operator fun times(other: CLong): CLong {
        return CLong(if (C_LONG_IS_4_BYTES) {
            (value.toInt() * other.value.toInt()).toLong()
        } else {
            value * other.value
        })
    }

    public operator fun div(other: CLong): CLong {
        return CLong(if (C_LONG_IS_4_BYTES) {
            (value.toInt() / other.value.toInt()).toLong()
        } else {
            value / other.value
        })
    }

    public operator fun rem(other: CLong): CLong {
        return CLong(if (C_LONG_IS_4_BYTES) {
            (value.toInt() % other.value.toInt()).toLong()
        } else {
            value % other.value
        })
    }

    public operator fun inc(): CLong {
        return CLong(if (C_LONG_IS_4_BYTES) {
            value.toInt().inc().toLong()
        } else {
            value.inc()
        })
    }

    public operator fun dec(): CLong {
        return CLong(if (C_LONG_IS_4_BYTES) {
            value.toInt().dec().toLong()
        } else {
            value.dec()
        })
    }

    public companion object {
        public val SIZE_BITS: Int

        public val SIZE_BYTES: Int

        public val MIN_VALUE: CLong

        public val MAX_VALUE: CLong

        init {
            if (C_LONG_IS_4_BYTES) {
                SIZE_BITS = 32
                SIZE_BYTES = 4
                MIN_VALUE = CLong(Int.MIN_VALUE.toLong())
                MAX_VALUE = CLong(Int.MAX_VALUE.toLong())
            } else {
                SIZE_BITS = 64
                SIZE_BYTES = 8
                MIN_VALUE = CLong(Long.MIN_VALUE)
                MAX_VALUE = CLong(Long.MAX_VALUE)
            }
        }
    }
}

public fun Int.toCLong(): CLong {
    return CLong(toLong())
}

public fun Long.toCLongExact(): CLong {
    return CLong(if (C_LONG_IS_4_BYTES) {
        toIntExact().toLong()
    } else {
        this
    })
}

public fun Long.toCLong(): CLong {
    return CLong(if (C_LONG_IS_4_BYTES) {
        toInt().toLong()
    } else {
        this
    })
}

@JvmInline public value class CUnsignedLong internal constructor(private val value: ULong) {
    public fun toUIntExact(): UInt {
        if (!C_LONG_IS_4_BYTES && value > UInt.MAX_VALUE) {
            throw ArithmeticException()
        } else {
            return value.toUInt()
        }
    }

    public fun toUInt(): UInt {
        return value.toUInt()
    }

    public fun toULong(): ULong {
        return value
    }

    public operator fun unaryPlus(): CUnsignedLong {
        return this
    }

    public operator fun plus(other: CUnsignedLong): CUnsignedLong {
        return CUnsignedLong(if (C_LONG_IS_4_BYTES) {
            (value.toUInt() + other.value.toUInt()).toULong()
        } else {
            value + other.value
        })
    }

    public operator fun minus(other: CUnsignedLong): CUnsignedLong {
        return CUnsignedLong(if (C_LONG_IS_4_BYTES) {
            (value.toUInt() - other.value.toUInt()).toULong()
        } else {
            value - other.value
        })
    }

    public operator fun times(other: CUnsignedLong): CUnsignedLong {
        return CUnsignedLong(if (C_LONG_IS_4_BYTES) {
            (value.toUInt() * other.value.toUInt()).toULong()
        } else {
            value * other.value
        })
    }

    public operator fun div(other: CUnsignedLong): CUnsignedLong {
        return CUnsignedLong(if (C_LONG_IS_4_BYTES) {
            (value.toUInt() / other.value.toUInt()).toULong()
        } else {
            value / other.value
        })
    }

    public operator fun rem(other: CUnsignedLong): CUnsignedLong {
        return CUnsignedLong(if (C_LONG_IS_4_BYTES) {
            (value.toUInt() % other.value.toUInt()).toULong()
        } else {
            value % other.value
        })
    }

    public operator fun inc(): CUnsignedLong {
        return CUnsignedLong(if (C_LONG_IS_4_BYTES) {
            value.toUInt().inc().toULong()
        } else {
            value.inc()
        })
    }

    public operator fun dec(): CUnsignedLong {
        return CUnsignedLong(if (C_LONG_IS_4_BYTES) {
            value.toUInt().dec().toULong()
        } else {
            value.dec()
        })
    }

    public companion object {
        public val SIZE_BITS: Int

        public val SIZE_BYTES: Int

        public val MIN_VALUE: CUnsignedLong = CUnsignedLong(0u)

        public val MAX_VALUE: CUnsignedLong

        init {
            if (C_LONG_IS_4_BYTES) {
                SIZE_BITS = 32
                SIZE_BYTES = 4
                MAX_VALUE = CUnsignedLong(UInt.MAX_VALUE.toULong())
            } else {
                SIZE_BITS = 64
                SIZE_BYTES = 8
                MAX_VALUE = CUnsignedLong(ULong.MAX_VALUE)
            }
        }
    }
}

public fun UInt.toCUnsignedLong(): CUnsignedLong {
    return CUnsignedLong(toULong())
}

public fun ULong.toCUnsignedLongExact(): CUnsignedLong {
    if (C_LONG_IS_4_BYTES && this > UInt.MAX_VALUE) {
        throw ArithmeticException()
    } else {
        return CUnsignedLong(this)
    }
}

public fun ULong.toCUnsignedLong(): CUnsignedLong {
    return CUnsignedLong(if (C_LONG_IS_4_BYTES) {
        toUInt().toULong()
    } else {
        this
    })
}

@JvmInline public value class CPtrDiffT internal constructor(private val value: Long) {
    public fun toIntExact(): Int {
        return if (ADDRESS_IS_4_BYTES) {
            value.toInt()
        } else {
            value.toIntExact()
        }
    }

    public fun toInt(): Int {
        return value.toInt()
    }

    public fun toLong(): Long {
        return value
    }

    public operator fun unaryPlus(): CPtrDiffT {
        return this
    }

    public operator fun unaryMinus(): CPtrDiffT {
        return CPtrDiffT(if (ADDRESS_IS_4_BYTES) {
            (-value.toInt()).toLong()
        } else {
            -value
        })
    }

    public operator fun plus(other: CPtrDiffT): CPtrDiffT {
        return CPtrDiffT(if (ADDRESS_IS_4_BYTES) {
            (value.toInt() + other.value.toInt()).toLong()
        } else {
            value + other.value
        })
    }

    public operator fun minus(other: CPtrDiffT): CPtrDiffT {
        return CPtrDiffT(if (ADDRESS_IS_4_BYTES) {
            (value.toInt() - other.value.toInt()).toLong()
        } else {
            value - other.value
        })
    }

    public operator fun times(other: CPtrDiffT): CPtrDiffT {
        return CPtrDiffT(if (ADDRESS_IS_4_BYTES) {
            (value.toInt() * other.value.toInt()).toLong()
        } else {
            value * other.value
        })
    }

    public operator fun div(other: CPtrDiffT): CPtrDiffT {
        return CPtrDiffT(if (ADDRESS_IS_4_BYTES) {
            (value.toInt() / other.value.toInt()).toLong()
        } else {
            value / other.value
        })
    }

    public operator fun rem(other: CPtrDiffT): CPtrDiffT {
        return CPtrDiffT(if (ADDRESS_IS_4_BYTES) {
            (value.toInt() % other.value.toInt()).toLong()
        } else {
            value % other.value
        })
    }

    public operator fun inc(): CPtrDiffT {
        return CPtrDiffT(if (ADDRESS_IS_4_BYTES) {
            value.toInt().inc().toLong()
        } else {
            value.inc()
        })
    }

    public operator fun dec(): CPtrDiffT {
        return CPtrDiffT(if (ADDRESS_IS_4_BYTES) {
            value.toInt().dec().toLong()
        } else {
            value.dec()
        })
    }

    public companion object {
        public val SIZE_BITS: Int

        public val SIZE_BYTES: Int

        public val MIN_VALUE: CPtrDiffT

        public val MAX_VALUE: CPtrDiffT

        init {
            if (ADDRESS_IS_4_BYTES) {
                SIZE_BITS = 32
                SIZE_BYTES = 4
                MIN_VALUE = CPtrDiffT(Int.MIN_VALUE.toLong())
                MAX_VALUE = CPtrDiffT(Int.MAX_VALUE.toLong())
            } else {
                SIZE_BITS = 64
                SIZE_BYTES = 8
                MIN_VALUE = CPtrDiffT(Long.MIN_VALUE)
                MAX_VALUE = CPtrDiffT(Long.MAX_VALUE)
            }
        }
    }
}

public fun Int.toCPtrdiffT(): CPtrDiffT {
    return CPtrDiffT(toLong())
}

public fun Long.toCPtrdiffTExact(): CPtrDiffT {
    return CPtrDiffT(if (ADDRESS_IS_4_BYTES) {
        toIntExact().toLong()
    } else {
        this
    })
}

public fun Long.toCPtrdiffT(): CPtrDiffT {
    return CPtrDiffT(if (ADDRESS_IS_4_BYTES) {
        toInt().toLong()
    } else {
        this
    })
}

@JvmInline public value class CSizeT internal constructor(private val value: ULong) {
    public fun toUIntExact(): UInt {
        if (!ADDRESS_IS_4_BYTES && value > UInt.MAX_VALUE) {
            throw ArithmeticException()
        } else {
            return value.toUInt()
        }
    }

    public fun toUInt(): UInt {
        return value.toUInt()
    }

    public fun toULong(): ULong {
        return value
    }

    public operator fun unaryPlus(): CSizeT {
        return this
    }

    public operator fun plus(other: CSizeT): CSizeT {
        return CSizeT(if (ADDRESS_IS_4_BYTES) {
            (value.toUInt() + other.value.toUInt()).toULong()
        } else {
            value + other.value
        })
    }

    public operator fun minus(other: CSizeT): CSizeT {
        return CSizeT(if (ADDRESS_IS_4_BYTES) {
            (value.toUInt() - other.value.toUInt()).toULong()
        } else {
            value - other.value
        })
    }

    public operator fun times(other: CSizeT): CSizeT {
        return CSizeT(if (ADDRESS_IS_4_BYTES) {
            (value.toUInt() * other.value.toUInt()).toULong()
        } else {
            value * other.value
        })
    }

    public operator fun div(other: CSizeT): CSizeT {
        return CSizeT(if (ADDRESS_IS_4_BYTES) {
            (value.toUInt() / other.value.toUInt()).toULong()
        } else {
            value / other.value
        })
    }

    public operator fun rem(other: CSizeT): CSizeT {
        return CSizeT(if (ADDRESS_IS_4_BYTES) {
            (value.toUInt() % other.value.toUInt()).toULong()
        } else {
            value % other.value
        })
    }

    public operator fun inc(): CSizeT {
        return CSizeT(if (ADDRESS_IS_4_BYTES) {
            value.toUInt().inc().toULong()
        } else {
            value.inc()
        })
    }

    public operator fun dec(): CSizeT {
        return CSizeT(if (ADDRESS_IS_4_BYTES) {
            value.toUInt().dec().toULong()
        } else {
            value.dec()
        })
    }

    public companion object {
        public val SIZE_BITS: Int

        public val SIZE_BYTES: Int

        public val MIN_VALUE: CSizeT = CSizeT(0u)

        public val MAX_VALUE: CSizeT

        init {
            if (ADDRESS_IS_4_BYTES) {
                SIZE_BITS = 32
                SIZE_BYTES = 4
                MAX_VALUE = CSizeT(UInt.MAX_VALUE.toULong())
            } else {
                SIZE_BITS = 64
                SIZE_BYTES = 8
                MAX_VALUE = CSizeT(ULong.MAX_VALUE)
            }
        }
    }
}

public fun UInt.toCSizeT(): CSizeT {
    return CSizeT(toULong())
}

public fun ULong.toCSizeTExact(): CSizeT {
    if (ADDRESS_IS_4_BYTES && this > UInt.MAX_VALUE) {
        throw ArithmeticException()
    } else {
        return CSizeT(this)
    }
}

public fun ULong.toCSizeT(): CSizeT {
    return CSizeT(if (ADDRESS_IS_4_BYTES) {
        toUInt().toULong()
    } else {
        this
    })
}
