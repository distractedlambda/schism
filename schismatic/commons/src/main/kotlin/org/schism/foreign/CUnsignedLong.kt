package org.schism.foreign

@JvmInline public value class CUnsignedLong internal constructor(internal val value: ULong) {
    internal constructor(value: UInt) : this(value.toULong())

    public fun toByte(): Byte {
        return value.toByte()
    }

    public fun toUByte(): UByte {
        return value.toUByte()
    }

    public fun toShort(): Short {
        return value.toShort()
    }

    public fun toUShort(): UShort {
        return value.toUShort()
    }

    public fun toInt(): Int {
        return value.toInt()
    }

    public fun toUInt(): UInt {
        return value.toUInt()
    }

    public fun toLong(): Long {
        return value.toLong()
    }

    public fun toULong(): ULong {
        return value
    }

    public fun toFloat(): Float = when (C_LONG_TYPE) {
        IntOrLong.INT -> toUInt().toFloat()
        IntOrLong.LONG -> value.toFloat()
    }

    public fun toDouble(): Double = when (C_LONG_TYPE) {
        IntOrLong.INT -> toUInt().toDouble()
        IntOrLong.LONG -> value.toDouble()
    }

    public operator fun compareTo(other: UByte): Int = when (C_LONG_TYPE) {
        IntOrLong.INT -> toUInt().compareTo(other)
        IntOrLong.LONG -> value.compareTo(other)
    }

    public operator fun compareTo(other: UShort): Int = when (C_LONG_TYPE) {
        IntOrLong.INT -> toUInt().compareTo(other)
        IntOrLong.LONG -> value.compareTo(other)
    }

    public operator fun compareTo(other: UInt): Int = when (C_LONG_TYPE) {
        IntOrLong.INT -> toUInt().compareTo(other)
        IntOrLong.LONG -> value.compareTo(other)
    }

    public operator fun compareTo(other: ULong): Int = when (C_LONG_TYPE) {
        IntOrLong.INT -> toUInt().compareTo(other)
        IntOrLong.LONG -> value.compareTo(other)
    }

    public operator fun compareTo(other: CUnsignedLong): Int = when (C_LONG_TYPE) {
        IntOrLong.INT -> toUInt().compareTo(other.toUInt())
        IntOrLong.LONG -> value.compareTo(other.value)
    }

    public operator fun unaryPlus(): CUnsignedLong {
        return this
    }

    public fun inv(): CUnsignedLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CUnsignedLong(toUInt().inv())
        IntOrLong.LONG -> CUnsignedLong(value.inv())
    }

    public operator fun plus(other: UByte): CUnsignedLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CUnsignedLong(toUInt() + other)
        IntOrLong.LONG -> CUnsignedLong(value + other)
    }

    public operator fun plus(other: UShort): CUnsignedLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CUnsignedLong(toUInt() + other)
        IntOrLong.LONG -> CUnsignedLong(value + other)
    }

    public operator fun plus(other: UInt): CUnsignedLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CUnsignedLong(toUInt() + other)
        IntOrLong.LONG -> CUnsignedLong(value + other)
    }

    public operator fun plus(other: ULong): ULong {
        return value + other
    }

    public operator fun plus(other: CUnsignedLong): CUnsignedLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CUnsignedLong(toUInt() + other.toUInt())
        IntOrLong.LONG -> CUnsignedLong(value + other.value)
    }

    public operator fun plus(other: Float): Float {
        return toFloat() + other
    }

    public operator fun plus(other: Double): Double {
        return toDouble() + other
    }

    public operator fun minus(other: UByte): CUnsignedLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CUnsignedLong(toUInt() - other)
        IntOrLong.LONG -> CUnsignedLong(value - other)
    }

    public operator fun minus(other: UShort): CUnsignedLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CUnsignedLong(toUInt() - other)
        IntOrLong.LONG -> CUnsignedLong(value - other)
    }

    public operator fun minus(other: UInt): CUnsignedLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CUnsignedLong(toUInt() - other)
        IntOrLong.LONG -> CUnsignedLong(value - other)
    }

    public operator fun minus(other: ULong): ULong {
        return value - other
    }

    public operator fun minus(other: CUnsignedLong): CUnsignedLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CUnsignedLong(toUInt() - other.toUInt())
        IntOrLong.LONG -> CUnsignedLong(value - other.value)
    }

    public operator fun minus(other: Float): Float {
        return toFloat() - other
    }

    public operator fun minus(other: Double): Double {
        return toDouble() - other
    }

    public operator fun times(other: UByte): CUnsignedLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CUnsignedLong(toUInt() * other)
        IntOrLong.LONG -> CUnsignedLong(value * other)
    }

    public operator fun times(other: UShort): CUnsignedLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CUnsignedLong(toUInt() * other)
        IntOrLong.LONG -> CUnsignedLong(value * other)
    }

    public operator fun times(other: UInt): CUnsignedLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CUnsignedLong(toUInt() * other)
        IntOrLong.LONG -> CUnsignedLong(value * other)
    }

    public operator fun times(other: ULong): ULong {
        return value * other
    }

    public operator fun times(other: CUnsignedLong): CUnsignedLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CUnsignedLong(toUInt() * other.toUInt())
        IntOrLong.LONG -> CUnsignedLong(value * other.value)
    }

    public operator fun times(other: Float): Float {
        return toFloat() * other
    }

    public operator fun times(other: Double): Double {
        return toDouble() * other
    }

    public operator fun div(other: UByte): CUnsignedLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CUnsignedLong(toUInt() / other)
        IntOrLong.LONG -> CUnsignedLong(value / other)
    }

    public operator fun div(other: UShort): CUnsignedLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CUnsignedLong(toUInt() / other)
        IntOrLong.LONG -> CUnsignedLong(value / other)
    }

    public operator fun div(other: UInt): CUnsignedLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CUnsignedLong(toUInt() / other)
        IntOrLong.LONG -> CUnsignedLong(value / other)
    }

    public operator fun div(other: ULong): ULong {
        return value / other
    }

    public operator fun div(other: CUnsignedLong): CUnsignedLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CUnsignedLong(toUInt() / other.toUInt())
        IntOrLong.LONG -> CUnsignedLong(value / other.value)
    }

    public operator fun div(other: Float): Float {
        return toFloat() / other
    }

    public operator fun div(other: Double): Double {
        return toDouble() / other
    }

    public operator fun rem(other: UByte): CUnsignedLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CUnsignedLong(toUInt() % other)
        IntOrLong.LONG -> CUnsignedLong(value % other)
    }

    public operator fun rem(other: UShort): CUnsignedLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CUnsignedLong(toUInt() % other)
        IntOrLong.LONG -> CUnsignedLong(value % other)
    }

    public operator fun rem(other: UInt): CUnsignedLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CUnsignedLong(toUInt() % other)
        IntOrLong.LONG -> CUnsignedLong(value % other)
    }

    public operator fun rem(other: ULong): ULong {
        return value % other
    }

    public operator fun rem(other: CUnsignedLong): CUnsignedLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CUnsignedLong(toUInt() % other.toUInt())
        IntOrLong.LONG -> CUnsignedLong(value % other.value)
    }

    public operator fun rem(other: Float): Float {
        return toFloat() % other
    }

    public operator fun rem(other: Double): Double {
        return toDouble() % other
    }

    public operator fun inc(): CUnsignedLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CUnsignedLong(toUInt().inc())
        IntOrLong.LONG -> CUnsignedLong(value.inc())
    }

    public operator fun dec(): CUnsignedLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CUnsignedLong(toUInt().dec())
        IntOrLong.LONG -> CUnsignedLong(value.dec())
    }

    public infix fun and(other: CUnsignedLong): CUnsignedLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CUnsignedLong(toUInt() and other.toUInt())
        IntOrLong.LONG -> CUnsignedLong(value and other.value)
    }

    public infix fun or(other: CUnsignedLong): CUnsignedLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CUnsignedLong(toUInt() or other.toUInt())
        IntOrLong.LONG -> CUnsignedLong(value or other.value)
    }

    public infix fun xor(other: CUnsignedLong): CUnsignedLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CUnsignedLong(toUInt() xor other.toUInt())
        IntOrLong.LONG -> CUnsignedLong(value xor other.value)
    }

    public infix fun shl(count: Int): CUnsignedLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CUnsignedLong(toUInt() shl count)
        IntOrLong.LONG -> CUnsignedLong(value shl count)
    }

    public infix fun shr(count: Int): CUnsignedLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CUnsignedLong(toUInt() shr count)
        IntOrLong.LONG -> CUnsignedLong(value shr count)
    }

    override fun toString(): String = when (C_LONG_TYPE) {
        IntOrLong.INT -> toUInt().toString()
        IntOrLong.LONG -> value.toString()
    }

    public companion object {
        public val MIN_VALUE: CUnsignedLong = when (C_LONG_TYPE) {
            IntOrLong.INT -> CUnsignedLong(UInt.MIN_VALUE)
            IntOrLong.LONG -> CUnsignedLong(ULong.MAX_VALUE)
        }

        public val MAX_VALUE: CUnsignedLong = when (C_LONG_TYPE) {
            IntOrLong.INT -> CUnsignedLong(UInt.MAX_VALUE)
            IntOrLong.LONG -> CUnsignedLong(ULong.MAX_VALUE)
        }

        public val SIZE_BITS: Int = when (C_LONG_TYPE) {
            IntOrLong.INT -> 32
            IntOrLong.LONG -> 64
        }

        public val SIZE_BYTES: Int = when (C_LONG_TYPE) {
            IntOrLong.INT -> 4
            IntOrLong.LONG -> 8
        }
    }
}

public fun Byte.toCUnsignedLong(): CUnsignedLong = when (C_LONG_TYPE) {
    IntOrLong.INT -> CUnsignedLong(toUInt())
    IntOrLong.LONG -> CUnsignedLong(toULong())
}

public fun UByte.toCUnsignedLong(): CUnsignedLong {
    return CUnsignedLong(toULong())
}

public fun Short.toCUnsignedLong(): CUnsignedLong = when (C_LONG_TYPE) {
    IntOrLong.INT -> CUnsignedLong(toUInt())
    IntOrLong.LONG -> CUnsignedLong(toULong())
}

public fun UShort.toCUnsignedLong(): CUnsignedLong {
    return CUnsignedLong(toULong())
}

public fun Int.toCUnsignedLong(): CUnsignedLong = when (C_LONG_TYPE) {
    IntOrLong.INT -> CUnsignedLong(toUInt())
    IntOrLong.LONG -> CUnsignedLong(toULong())
}

public fun UInt.toCUnsignedLong(): CUnsignedLong {
    return CUnsignedLong(this)
}

public fun Long.toCUnsignedLong(): CUnsignedLong = when (C_LONG_TYPE) {
    IntOrLong.INT -> CUnsignedLong(toUInt())
    IntOrLong.LONG -> CUnsignedLong(toULong())
}

public fun ULong.toCUnsignedLong(): CUnsignedLong = when (C_LONG_TYPE) {
    IntOrLong.INT -> CUnsignedLong(toUInt())
    IntOrLong.LONG -> CUnsignedLong(this)
}

public fun Float.toCUnsignedLong(): CUnsignedLong = when (C_LONG_TYPE) {
    IntOrLong.INT -> CUnsignedLong(toUInt())
    IntOrLong.LONG -> CUnsignedLong(toULong())
}

public fun Double.toCUnsignedLong(): CUnsignedLong = when (C_LONG_TYPE) {
    IntOrLong.INT -> CUnsignedLong(toUInt())
    IntOrLong.LONG -> CUnsignedLong(toULong())
}

public operator fun UByte.plus(other: CUnsignedLong): CUnsignedLong = when (C_LONG_TYPE) {
    IntOrLong.INT -> CUnsignedLong(this + other.toUInt())
    IntOrLong.LONG -> CUnsignedLong(this + other.value)
}

public operator fun UShort.plus(other: CUnsignedLong): CUnsignedLong = when (C_LONG_TYPE) {
    IntOrLong.INT -> CUnsignedLong(this + other.toUInt())
    IntOrLong.LONG -> CUnsignedLong(this + other.value)
}

public operator fun UInt.plus(other: CUnsignedLong): CUnsignedLong = when (C_LONG_TYPE) {
    IntOrLong.INT -> CUnsignedLong(this + other.toUInt())
    IntOrLong.LONG -> CUnsignedLong(this + other.value)
}

public operator fun ULong.plus(other: CUnsignedLong): ULong {
    return this + other.value
}

public operator fun Float.plus(other: CUnsignedLong): Float {
    return this + other.toFloat()
}

public operator fun Double.plus(other: CUnsignedLong): Double {
    return this + other.toDouble()
}

public operator fun UByte.minus(other: CUnsignedLong): CUnsignedLong = when (C_LONG_TYPE) {
    IntOrLong.INT -> CUnsignedLong(this - other.toUInt())
    IntOrLong.LONG -> CUnsignedLong(this - other.value)
}

public operator fun UShort.minus(other: CUnsignedLong): CUnsignedLong = when (C_LONG_TYPE) {
    IntOrLong.INT -> CUnsignedLong(this - other.toUInt())
    IntOrLong.LONG -> CUnsignedLong(this - other.value)
}

public operator fun UInt.minus(other: CUnsignedLong): CUnsignedLong = when (C_LONG_TYPE) {
    IntOrLong.INT -> CUnsignedLong(this - other.toUInt())
    IntOrLong.LONG -> CUnsignedLong(this - other.value)
}

public operator fun ULong.minus(other: CUnsignedLong): ULong {
    return this - other.value
}

public operator fun Float.minus(other: CUnsignedLong): Float {
    return this - other.toFloat()
}

public operator fun Double.minus(other: CUnsignedLong): Double {
    return this - other.toDouble()
}

public operator fun UByte.times(other: CUnsignedLong): CUnsignedLong = when (C_LONG_TYPE) {
    IntOrLong.INT -> CUnsignedLong(this * other.toUInt())
    IntOrLong.LONG -> CUnsignedLong(this * other.value)
}

public operator fun UShort.times(other: CUnsignedLong): CUnsignedLong = when (C_LONG_TYPE) {
    IntOrLong.INT -> CUnsignedLong(this * other.toUInt())
    IntOrLong.LONG -> CUnsignedLong(this * other.value)
}

public operator fun UInt.times(other: CUnsignedLong): CUnsignedLong = when (C_LONG_TYPE) {
    IntOrLong.INT -> CUnsignedLong(this * other.toUInt())
    IntOrLong.LONG -> CUnsignedLong(this * other.value)
}

public operator fun ULong.times(other: CUnsignedLong): ULong {
    return this * other.value
}

public operator fun Float.times(other: CUnsignedLong): Float {
    return this * other.toFloat()
}

public operator fun Double.times(other: CUnsignedLong): Double {
    return this * other.toDouble()
}

public operator fun UByte.div(other: CUnsignedLong): CUnsignedLong = when (C_LONG_TYPE) {
    IntOrLong.INT -> CUnsignedLong(this / other.toUInt())
    IntOrLong.LONG -> CUnsignedLong(this / other.value)
}

public operator fun UShort.div(other: CUnsignedLong): CUnsignedLong = when (C_LONG_TYPE) {
    IntOrLong.INT -> CUnsignedLong(this / other.toUInt())
    IntOrLong.LONG -> CUnsignedLong(this / other.value)
}

public operator fun UInt.div(other: CUnsignedLong): CUnsignedLong = when (C_LONG_TYPE) {
    IntOrLong.INT -> CUnsignedLong(this / other.toUInt())
    IntOrLong.LONG -> CUnsignedLong(this / other.value)
}

public operator fun ULong.div(other: CUnsignedLong): ULong {
    return this / other.value
}

public operator fun Float.div(other: CUnsignedLong): Float {
    return this / other.toFloat()
}

public operator fun Double.div(other: CUnsignedLong): Double {
    return this / other.toDouble()
}

public operator fun UByte.rem(other: CUnsignedLong): CUnsignedLong = when (C_LONG_TYPE) {
    IntOrLong.INT -> CUnsignedLong(this % other.toUInt())
    IntOrLong.LONG -> CUnsignedLong(this % other.value)
}

public operator fun UShort.rem(other: CUnsignedLong): CUnsignedLong = when (C_LONG_TYPE) {
    IntOrLong.INT -> CUnsignedLong(this % other.toUInt())
    IntOrLong.LONG -> CUnsignedLong(this % other.value)
}

public operator fun UInt.rem(other: CUnsignedLong): CUnsignedLong = when (C_LONG_TYPE) {
    IntOrLong.INT -> CUnsignedLong(this % other.toUInt())
    IntOrLong.LONG -> CUnsignedLong(this % other.value)
}

public operator fun ULong.rem(other: CUnsignedLong): ULong {
    return this % other.value
}

public operator fun Float.rem(other: CUnsignedLong): Float {
    return this % other.toFloat()
}

public operator fun Double.rem(other: CUnsignedLong): Double {
    return this % other.toDouble()
}

public operator fun UByte.compareTo(other: CUnsignedLong): Int = when (C_LONG_TYPE) {
    IntOrLong.INT -> compareTo(other.toUInt())
    IntOrLong.LONG -> compareTo(other.value)
}

public operator fun UShort.compareTo(other: CUnsignedLong): Int = when (C_LONG_TYPE) {
    IntOrLong.INT -> compareTo(other.toUInt())
    IntOrLong.LONG -> compareTo(other.value)
}

public operator fun UInt.compareTo(other: CUnsignedLong): Int = when (C_LONG_TYPE) {
    IntOrLong.INT -> compareTo(other.toUInt())
    IntOrLong.LONG -> compareTo(other.value)
}

public operator fun ULong.compareTo(other: CUnsignedLong): Int = when (C_LONG_TYPE) {
    IntOrLong.INT -> compareTo(other.toUInt())
    IntOrLong.LONG -> compareTo(other.value)
}
