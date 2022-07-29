package org.schism.foreign

import org.schism.math.toIntExact

@JvmInline public value class CPtrDiffT internal constructor(internal val value: Long) {
    internal constructor(value: Int) : this(value.toLong())

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

    public fun toIntExact(): Int = when (ADDRESS_TYPE) {
        IntOrLong.INT -> toInt()
        IntOrLong.LONG -> value.toIntExact()
    }

    public fun toLong(): Long {
        return value
    }

    public fun toULong(): ULong {
        return value.toULong()
    }

    public fun toFloat(): Float = when (ADDRESS_TYPE) {
        IntOrLong.INT -> toInt().toFloat()
        IntOrLong.LONG -> value.toFloat()
    }

    public fun toDouble(): Double = when (ADDRESS_TYPE) {
        IntOrLong.INT -> toInt().toDouble()
        IntOrLong.LONG -> value.toDouble()
    }

    public operator fun compareTo(other: Byte): Int = when (ADDRESS_TYPE) {
        IntOrLong.INT -> toInt().compareTo(other)
        IntOrLong.LONG -> value.compareTo(other)
    }

    public operator fun compareTo(other: Short): Int = when (ADDRESS_TYPE) {
        IntOrLong.INT -> toInt().compareTo(other)
        IntOrLong.LONG -> value.compareTo(other)
    }

    public operator fun compareTo(other: Int): Int = when (ADDRESS_TYPE) {
        IntOrLong.INT -> toInt().compareTo(other)
        IntOrLong.LONG -> value.compareTo(other)
    }

    public operator fun compareTo(other: Long): Int = when (ADDRESS_TYPE) {
        IntOrLong.INT -> toInt().compareTo(other)
        IntOrLong.LONG -> value.compareTo(other)
    }

    public operator fun compareTo(other: CPtrDiffT): Int = when (ADDRESS_TYPE) {
        IntOrLong.INT -> toInt().compareTo(other.toInt())
        IntOrLong.LONG -> value.compareTo(other.value)
    }

    public operator fun compareTo(other: Float): Int = when (ADDRESS_TYPE) {
        IntOrLong.INT -> toInt().compareTo(other)
        IntOrLong.LONG -> value.compareTo(other)
    }

    public operator fun compareTo(other: Double): Int = when (ADDRESS_TYPE) {
        IntOrLong.INT -> toInt().compareTo(other)
        IntOrLong.LONG -> value.compareTo(other)
    }

    public operator fun unaryPlus(): CPtrDiffT {
        return this
    }

    public operator fun unaryMinus(): CPtrDiffT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CPtrDiffT(-toInt())
        IntOrLong.LONG -> CPtrDiffT(-value)
    }

    public fun inv(): CPtrDiffT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CPtrDiffT(toInt().inv())
        IntOrLong.LONG -> CPtrDiffT(value.inv())
    }

    public operator fun plus(other: Byte): CPtrDiffT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CPtrDiffT(toInt() + other)
        IntOrLong.LONG -> CPtrDiffT(value + other)
    }

    public operator fun plus(other: Short): CPtrDiffT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CPtrDiffT(toInt() + other)
        IntOrLong.LONG -> CPtrDiffT(value + other)
    }

    public operator fun plus(other: Int): CPtrDiffT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CPtrDiffT(toInt() + other)
        IntOrLong.LONG -> CPtrDiffT(value + other)
    }

    public operator fun plus(other: Long): Long {
        return value + other
    }

    public operator fun plus(other: CPtrDiffT): CPtrDiffT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CPtrDiffT(toInt() + other.toInt())
        IntOrLong.LONG -> CPtrDiffT(value + other.value)
    }

    public operator fun plus(other: Float): Float {
        return toFloat() + other
    }

    public operator fun plus(other: Double): Double {
        return toDouble() + other
    }

    public operator fun minus(other: Byte): CPtrDiffT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CPtrDiffT(toInt() - other)
        IntOrLong.LONG -> CPtrDiffT(value - other)
    }

    public operator fun minus(other: Short): CPtrDiffT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CPtrDiffT(toInt() - other)
        IntOrLong.LONG -> CPtrDiffT(value - other)
    }

    public operator fun minus(other: Int): CPtrDiffT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CPtrDiffT(toInt() - other)
        IntOrLong.LONG -> CPtrDiffT(value - other)
    }

    public operator fun minus(other: Long): Long {
        return value - other
    }

    public operator fun minus(other: CPtrDiffT): CPtrDiffT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CPtrDiffT(toInt() - other.toInt())
        IntOrLong.LONG -> CPtrDiffT(value - other.value)
    }

    public operator fun minus(other: Float): Float {
        return toFloat() - other
    }

    public operator fun minus(other: Double): Double {
        return toDouble() - other
    }

    public operator fun times(other: Byte): CPtrDiffT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CPtrDiffT(toInt() * other)
        IntOrLong.LONG -> CPtrDiffT(value * other)
    }

    public operator fun times(other: Short): CPtrDiffT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CPtrDiffT(toInt() * other)
        IntOrLong.LONG -> CPtrDiffT(value * other)
    }

    public operator fun times(other: Int): CPtrDiffT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CPtrDiffT(toInt() * other)
        IntOrLong.LONG -> CPtrDiffT(value * other)
    }

    public operator fun times(other: Long): Long {
        return value * other
    }

    public operator fun times(other: CPtrDiffT): CPtrDiffT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CPtrDiffT(toInt() * other.toInt())
        IntOrLong.LONG -> CPtrDiffT(value * other.value)
    }

    public operator fun times(other: Float): Float {
        return toFloat() * other
    }

    public operator fun times(other: Double): Double {
        return toDouble() * other
    }

    public operator fun div(other: Byte): CPtrDiffT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CPtrDiffT(toInt() / other)
        IntOrLong.LONG -> CPtrDiffT(value / other)
    }

    public operator fun div(other: Short): CPtrDiffT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CPtrDiffT(toInt() / other)
        IntOrLong.LONG -> CPtrDiffT(value / other)
    }

    public operator fun div(other: Int): CPtrDiffT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CPtrDiffT(toInt() / other)
        IntOrLong.LONG -> CPtrDiffT(value / other)
    }

    public operator fun div(other: Long): Long {
        return value / other
    }

    public operator fun div(other: CPtrDiffT): CPtrDiffT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CPtrDiffT(toInt() / other.toInt())
        IntOrLong.LONG -> CPtrDiffT(value / other.value)
    }

    public operator fun div(other: Float): Float {
        return toFloat() / other
    }

    public operator fun div(other: Double): Double {
        return toDouble() / other
    }

    public operator fun rem(other: Byte): CPtrDiffT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CPtrDiffT(toInt() % other)
        IntOrLong.LONG -> CPtrDiffT(value % other)
    }

    public operator fun rem(other: Short): CPtrDiffT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CPtrDiffT(toInt() % other)
        IntOrLong.LONG -> CPtrDiffT(value % other)
    }

    public operator fun rem(other: Int): CPtrDiffT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CPtrDiffT(toInt() % other)
        IntOrLong.LONG -> CPtrDiffT(value % other)
    }

    public operator fun rem(other: Long): Long {
        return value % other
    }

    public operator fun rem(other: CPtrDiffT): CPtrDiffT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CPtrDiffT(toInt() % other.toInt())
        IntOrLong.LONG -> CPtrDiffT(value % other.value)
    }

    public operator fun rem(other: Float): Float {
        return toFloat() % other
    }

    public operator fun rem(other: Double): Double {
        return toDouble() % other
    }

    public operator fun inc(): CPtrDiffT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CPtrDiffT(toInt().inc())
        IntOrLong.LONG -> CPtrDiffT(value.inc())
    }

    public operator fun dec(): CPtrDiffT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CPtrDiffT(toInt().dec())
        IntOrLong.LONG -> CPtrDiffT(value.dec())
    }

    public infix fun and(other: CPtrDiffT): CPtrDiffT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CPtrDiffT(toInt() and other.toInt())
        IntOrLong.LONG -> CPtrDiffT(value and other.value)
    }

    public infix fun or(other: CPtrDiffT): CPtrDiffT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CPtrDiffT(toInt() or other.toInt())
        IntOrLong.LONG -> CPtrDiffT(value or other.value)
    }

    public infix fun xor(other: CPtrDiffT): CPtrDiffT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CPtrDiffT(toInt() xor other.toInt())
        IntOrLong.LONG -> CPtrDiffT(value xor other.value)
    }

    public infix fun shl(count: Int): CPtrDiffT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CPtrDiffT(toInt() shl count)
        IntOrLong.LONG -> CPtrDiffT(value shl count)
    }

    public infix fun shr(count: Int): CPtrDiffT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CPtrDiffT(toInt() shr count)
        IntOrLong.LONG -> CPtrDiffT(value shr count)
    }

    public infix fun ushr(count: Int): CPtrDiffT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CPtrDiffT(toInt() ushr count)
        IntOrLong.LONG -> CPtrDiffT(value ushr count)
    }

    override fun toString(): String = when (C_LONG_TYPE) {
        IntOrLong.INT -> toInt().toString()
        IntOrLong.LONG -> value.toString()
    }

    public companion object {
        public val MIN_VALUE: CPtrDiffT = when (ADDRESS_TYPE) {
            IntOrLong.INT -> CPtrDiffT(Int.MIN_VALUE)
            IntOrLong.LONG -> CPtrDiffT(Int.MAX_VALUE)
        }

        public val MAX_VALUE: CPtrDiffT = when (ADDRESS_TYPE) {
            IntOrLong.INT -> CPtrDiffT(Int.MAX_VALUE)
            IntOrLong.LONG -> CPtrDiffT(Long.MAX_VALUE)
        }

        public val SIZE_BITS: Int = when (ADDRESS_TYPE) {
            IntOrLong.INT -> 32
            IntOrLong.LONG -> 64
        }

        public val SIZE_BYTES: Int = when (ADDRESS_TYPE) {
            IntOrLong.INT -> 4
            IntOrLong.LONG -> 8
        }
    }
}

public fun Byte.toCPtrDiffT(): CPtrDiffT {
    return CPtrDiffT(toLong())
}

public fun UByte.toCPtrDiffT(): CPtrDiffT {
    return CPtrDiffT(toLong())
}

public fun Short.toCPtrDiffT(): CPtrDiffT {
    return CPtrDiffT(toLong())
}

public fun UShort.toCPtrDiffT(): CPtrDiffT {
    return CPtrDiffT(toLong())
}

public fun Int.toCPtrDiffT(): CPtrDiffT {
    return CPtrDiffT(this)
}

public fun UInt.toCPtrDiffT(): CPtrDiffT = when (ADDRESS_TYPE) {
    IntOrLong.INT -> CPtrDiffT(toInt())
    IntOrLong.LONG -> CPtrDiffT(toLong())
}

public fun Long.toCPtrDiffT(): CPtrDiffT = when (ADDRESS_TYPE) {
    IntOrLong.INT -> CPtrDiffT(toInt())
    IntOrLong.LONG -> CPtrDiffT(this)
}

public fun ULong.toCPtrDiffT(): CPtrDiffT = when (ADDRESS_TYPE) {
    IntOrLong.INT -> CPtrDiffT(toInt())
    IntOrLong.LONG -> CPtrDiffT(toLong())
}

public fun Float.toCPtrDiffT(): CPtrDiffT = when (ADDRESS_TYPE) {
    IntOrLong.INT -> CPtrDiffT(toInt())
    IntOrLong.LONG -> CPtrDiffT(toLong())
}

public fun Double.toCPtrDiffT(): CPtrDiffT = when (ADDRESS_TYPE) {
    IntOrLong.INT -> CPtrDiffT(toInt())
    IntOrLong.LONG -> CPtrDiffT(toLong())
}

public operator fun Byte.plus(other: CPtrDiffT): CPtrDiffT = when (ADDRESS_TYPE) {
    IntOrLong.INT -> CPtrDiffT(this + other.toInt())
    IntOrLong.LONG -> CPtrDiffT(this + other.value)
}

public operator fun Short.plus(other: CPtrDiffT): CPtrDiffT = when (ADDRESS_TYPE) {
    IntOrLong.INT -> CPtrDiffT(this + other.toInt())
    IntOrLong.LONG -> CPtrDiffT(this + other.value)
}

public operator fun Int.plus(other: CPtrDiffT): CPtrDiffT = when (ADDRESS_TYPE) {
    IntOrLong.INT -> CPtrDiffT(this + other.toInt())
    IntOrLong.LONG -> CPtrDiffT(this + other.value)
}

public operator fun Long.plus(other: CPtrDiffT): Long {
    return this + other.value
}

public operator fun Float.plus(other: CPtrDiffT): Float {
    return this + other.toFloat()
}

public operator fun Double.plus(other: CPtrDiffT): Double {
    return this + other.toDouble()
}

public operator fun Byte.minus(other: CPtrDiffT): CPtrDiffT = when (ADDRESS_TYPE) {
    IntOrLong.INT -> CPtrDiffT(this - other.toInt())
    IntOrLong.LONG -> CPtrDiffT(this - other.value)
}

public operator fun Short.minus(other: CPtrDiffT): CPtrDiffT = when (ADDRESS_TYPE) {
    IntOrLong.INT -> CPtrDiffT(this - other.toInt())
    IntOrLong.LONG -> CPtrDiffT(this - other.value)
}

public operator fun Int.minus(other: CPtrDiffT): CPtrDiffT = when (ADDRESS_TYPE) {
    IntOrLong.INT -> CPtrDiffT(this - other.toInt())
    IntOrLong.LONG -> CPtrDiffT(this - other.value)
}

public operator fun Long.minus(other: CPtrDiffT): Long {
    return this - other.value
}

public operator fun Float.minus(other: CPtrDiffT): Float {
    return this - other.toFloat()
}

public operator fun Double.minus(other: CPtrDiffT): Double {
    return this - other.toDouble()
}

public operator fun Byte.times(other: CPtrDiffT): CPtrDiffT = when (ADDRESS_TYPE) {
    IntOrLong.INT -> CPtrDiffT(this * other.toInt())
    IntOrLong.LONG -> CPtrDiffT(this * other.value)
}

public operator fun Short.times(other: CPtrDiffT): CPtrDiffT = when (ADDRESS_TYPE) {
    IntOrLong.INT -> CPtrDiffT(this * other.toInt())
    IntOrLong.LONG -> CPtrDiffT(this * other.value)
}

public operator fun Int.times(other: CPtrDiffT): CPtrDiffT = when (ADDRESS_TYPE) {
    IntOrLong.INT -> CPtrDiffT(this * other.toInt())
    IntOrLong.LONG -> CPtrDiffT(this * other.value)
}

public operator fun Long.times(other: CPtrDiffT): Long {
    return this * other.value
}

public operator fun Float.times(other: CPtrDiffT): Float {
    return this * other.toFloat()
}

public operator fun Double.times(other: CPtrDiffT): Double {
    return this * other.toDouble()
}

public operator fun Byte.div(other: CPtrDiffT): CPtrDiffT = when (ADDRESS_TYPE) {
    IntOrLong.INT -> CPtrDiffT(this / other.toInt())
    IntOrLong.LONG -> CPtrDiffT(this / other.value)
}

public operator fun Short.div(other: CPtrDiffT): CPtrDiffT = when (ADDRESS_TYPE) {
    IntOrLong.INT -> CPtrDiffT(this / other.toInt())
    IntOrLong.LONG -> CPtrDiffT(this / other.value)
}

public operator fun Int.div(other: CPtrDiffT): CPtrDiffT = when (ADDRESS_TYPE) {
    IntOrLong.INT -> CPtrDiffT(this / other.toInt())
    IntOrLong.LONG -> CPtrDiffT(this / other.value)
}

public operator fun Long.div(other: CPtrDiffT): Long {
    return this / other.value
}

public operator fun Float.div(other: CPtrDiffT): Float {
    return this / other.toFloat()
}

public operator fun Double.div(other: CPtrDiffT): Double {
    return this / other.toDouble()
}

public operator fun Byte.rem(other: CPtrDiffT): CPtrDiffT = when (ADDRESS_TYPE) {
    IntOrLong.INT -> CPtrDiffT(this % other.toInt())
    IntOrLong.LONG -> CPtrDiffT(this % other.value)
}

public operator fun Short.rem(other: CPtrDiffT): CPtrDiffT = when (ADDRESS_TYPE) {
    IntOrLong.INT -> CPtrDiffT(this % other.toInt())
    IntOrLong.LONG -> CPtrDiffT(this % other.value)
}

public operator fun Int.rem(other: CPtrDiffT): CPtrDiffT = when (ADDRESS_TYPE) {
    IntOrLong.INT -> CPtrDiffT(this % other.toInt())
    IntOrLong.LONG -> CPtrDiffT(this % other.value)
}

public operator fun Long.rem(other: CPtrDiffT): Long {
    return this % other.value
}

public operator fun Float.rem(other: CPtrDiffT): Float {
    return this % other.toFloat()
}

public operator fun Double.rem(other: CPtrDiffT): Double {
    return this % other.toDouble()
}

public operator fun Byte.compareTo(other: CPtrDiffT): Int = when (ADDRESS_TYPE) {
    IntOrLong.INT -> compareTo(other.toInt())
    IntOrLong.LONG -> compareTo(other.value)
}

public operator fun Short.compareTo(other: CPtrDiffT): Int = when (ADDRESS_TYPE) {
    IntOrLong.INT -> compareTo(other.toInt())
    IntOrLong.LONG -> compareTo(other.value)
}

public operator fun Int.compareTo(other: CPtrDiffT): Int = when (ADDRESS_TYPE) {
    IntOrLong.INT -> compareTo(other.toInt())
    IntOrLong.LONG -> compareTo(other.value)
}

public operator fun Long.compareTo(other: CPtrDiffT): Int = when (ADDRESS_TYPE) {
    IntOrLong.INT -> compareTo(other.toInt())
    IntOrLong.LONG -> compareTo(other.value)
}

public operator fun Float.compareTo(other: CPtrDiffT): Int = when (ADDRESS_TYPE) {
    IntOrLong.INT -> compareTo(other.toInt())
    IntOrLong.LONG -> compareTo(other.value)
}

public operator fun Double.compareTo(other: CPtrDiffT): Int = when (ADDRESS_TYPE) {
    IntOrLong.INT -> compareTo(other.toInt())
    IntOrLong.LONG -> compareTo(other.value)
}
