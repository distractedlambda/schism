package org.schism.foreign

import org.schism.math.toIntExact

@JvmInline public value class CLong internal constructor(internal val value: Long) {
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

    public fun toIntExact(): Int = when (C_LONG_TYPE) {
        IntOrLong.INT -> toInt()
        IntOrLong.LONG -> value.toIntExact()
    }

    public fun toLong(): Long {
        return value
    }

    public fun toULong(): ULong {
        return value.toULong()
    }

    public fun toFloat(): Float = when (C_LONG_TYPE) {
        IntOrLong.INT -> toInt().toFloat()
        IntOrLong.LONG -> value.toFloat()
    }

    public fun toDouble(): Double = when (C_LONG_TYPE) {
        IntOrLong.INT -> toInt().toDouble()
        IntOrLong.LONG -> value.toDouble()
    }

    public operator fun compareTo(other: Byte): Int = when (C_LONG_TYPE) {
        IntOrLong.INT -> toInt().compareTo(other)
        IntOrLong.LONG -> value.compareTo(other)
    }

    public operator fun compareTo(other: Short): Int = when (C_LONG_TYPE) {
        IntOrLong.INT -> toInt().compareTo(other)
        IntOrLong.LONG -> value.compareTo(other)
    }

    public operator fun compareTo(other: Int): Int = when (C_LONG_TYPE) {
        IntOrLong.INT -> toInt().compareTo(other)
        IntOrLong.LONG -> value.compareTo(other)
    }

    public operator fun compareTo(other: Long): Int = when (C_LONG_TYPE) {
        IntOrLong.INT -> toInt().compareTo(other)
        IntOrLong.LONG -> value.compareTo(other)
    }

    public operator fun compareTo(other: CLong): Int = when (C_LONG_TYPE) {
        IntOrLong.INT -> toInt().compareTo(other.toInt())
        IntOrLong.LONG -> value.compareTo(other.value)
    }

    public operator fun compareTo(other: Float): Int = when (C_LONG_TYPE) {
        IntOrLong.INT -> toInt().compareTo(other)
        IntOrLong.LONG -> value.compareTo(other)
    }

    public operator fun compareTo(other: Double): Int = when (C_LONG_TYPE) {
        IntOrLong.INT -> toInt().compareTo(other)
        IntOrLong.LONG -> value.compareTo(other)
    }

    public operator fun unaryPlus(): CLong {
        return this
    }

    public operator fun unaryMinus(): CLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CLong(-toInt())
        IntOrLong.LONG -> CLong(-value)
    }

    public fun inv(): CLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CLong(toInt().inv())
        IntOrLong.LONG -> CLong(value.inv())
    }

    public operator fun plus(other: Byte): CLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CLong(toInt() + other)
        IntOrLong.LONG -> CLong(value + other)
    }

    public operator fun plus(other: Short): CLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CLong(toInt() + other)
        IntOrLong.LONG -> CLong(value + other)
    }

    public operator fun plus(other: Int): CLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CLong(toInt() + other)
        IntOrLong.LONG -> CLong(value + other)
    }

    public operator fun plus(other: Long): Long {
        return value + other
    }

    public operator fun plus(other: CLong): CLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CLong(toInt() + other.toInt())
        IntOrLong.LONG -> CLong(value + other.value)
    }

    public operator fun plus(other: Float): Float {
        return toFloat() + other
    }

    public operator fun plus(other: Double): Double {
        return toDouble() + other
    }

    public operator fun minus(other: Byte): CLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CLong(toInt() - other)
        IntOrLong.LONG -> CLong(value - other)
    }

    public operator fun minus(other: Short): CLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CLong(toInt() - other)
        IntOrLong.LONG -> CLong(value - other)
    }

    public operator fun minus(other: Int): CLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CLong(toInt() - other)
        IntOrLong.LONG -> CLong(value - other)
    }

    public operator fun minus(other: Long): Long {
        return value - other
    }

    public operator fun minus(other: CLong): CLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CLong(toInt() - other.toInt())
        IntOrLong.LONG -> CLong(value - other.value)
    }

    public operator fun minus(other: Float): Float {
        return toFloat() - other
    }

    public operator fun minus(other: Double): Double {
        return toDouble() - other
    }

    public operator fun times(other: Byte): CLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CLong(toInt() * other)
        IntOrLong.LONG -> CLong(value * other)
    }

    public operator fun times(other: Short): CLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CLong(toInt() * other)
        IntOrLong.LONG -> CLong(value * other)
    }

    public operator fun times(other: Int): CLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CLong(toInt() * other)
        IntOrLong.LONG -> CLong(value * other)
    }

    public operator fun times(other: Long): Long {
        return value * other
    }

    public operator fun times(other: CLong): CLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CLong(toInt() * other.toInt())
        IntOrLong.LONG -> CLong(value * other.value)
    }

    public operator fun times(other: Float): Float {
        return toFloat() * other
    }

    public operator fun times(other: Double): Double {
        return toDouble() * other
    }

    public operator fun div(other: Byte): CLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CLong(toInt() / other)
        IntOrLong.LONG -> CLong(value / other)
    }

    public operator fun div(other: Short): CLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CLong(toInt() / other)
        IntOrLong.LONG -> CLong(value / other)
    }

    public operator fun div(other: Int): CLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CLong(toInt() / other)
        IntOrLong.LONG -> CLong(value / other)
    }

    public operator fun div(other: Long): Long {
        return value / other
    }

    public operator fun div(other: CLong): CLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CLong(toInt() / other.toInt())
        IntOrLong.LONG -> CLong(value / other.value)
    }

    public operator fun div(other: Float): Float {
        return toFloat() / other
    }

    public operator fun div(other: Double): Double {
        return toDouble() / other
    }

    public operator fun rem(other: Byte): CLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CLong(toInt() % other)
        IntOrLong.LONG -> CLong(value % other)
    }

    public operator fun rem(other: Short): CLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CLong(toInt() % other)
        IntOrLong.LONG -> CLong(value % other)
    }

    public operator fun rem(other: Int): CLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CLong(toInt() % other)
        IntOrLong.LONG -> CLong(value % other)
    }

    public operator fun rem(other: Long): Long {
        return value % other
    }

    public operator fun rem(other: CLong): CLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CLong(toInt() % other.toInt())
        IntOrLong.LONG -> CLong(value % other.value)
    }

    public operator fun rem(other: Float): Float {
        return toFloat() % other
    }

    public operator fun rem(other: Double): Double {
        return toDouble() % other
    }

    public operator fun inc(): CLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CLong(toInt().inc())
        IntOrLong.LONG -> CLong(value.inc())
    }

    public operator fun dec(): CLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CLong(toInt().dec())
        IntOrLong.LONG -> CLong(value.dec())
    }

    public infix fun and(other: CLong): CLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CLong(toInt() and other.toInt())
        IntOrLong.LONG -> CLong(value and other.value)
    }

    public infix fun or(other: CLong): CLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CLong(toInt() or other.toInt())
        IntOrLong.LONG -> CLong(value or other.value)
    }

    public infix fun xor(other: CLong): CLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CLong(toInt() xor other.toInt())
        IntOrLong.LONG -> CLong(value xor other.value)
    }

    public infix fun shl(count: Int): CLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CLong(toInt() shl count)
        IntOrLong.LONG -> CLong(value shl count)
    }

    public infix fun shr(count: Int): CLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CLong(toInt() shr count)
        IntOrLong.LONG -> CLong(value shr count)
    }

    public infix fun ushr(count: Int): CLong = when (C_LONG_TYPE) {
        IntOrLong.INT -> CLong(toInt() ushr count)
        IntOrLong.LONG -> CLong(value ushr count)
    }

    override fun toString(): String = when (C_LONG_TYPE) {
        IntOrLong.INT -> toInt().toString()
        IntOrLong.LONG -> value.toString()
    }

    public companion object {
        public val MIN_VALUE: CLong = when (C_LONG_TYPE) {
            IntOrLong.INT -> CLong(Int.MIN_VALUE)
            IntOrLong.LONG -> CLong(Int.MAX_VALUE)
        }

        public val MAX_VALUE: CLong = when (C_LONG_TYPE) {
            IntOrLong.INT -> CLong(Int.MAX_VALUE)
            IntOrLong.LONG -> CLong(Long.MAX_VALUE)
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

public fun Byte.toCLong(): CLong {
    return CLong(toLong())
}

public fun UByte.toCLong(): CLong {
    return CLong(toLong())
}

public fun Short.toCLong(): CLong {
    return CLong(toLong())
}

public fun UShort.toCLong(): CLong {
    return CLong(toLong())
}

public fun Int.toCLong(): CLong {
    return CLong(this)
}

public fun UInt.toCLong(): CLong = when (C_LONG_TYPE) {
    IntOrLong.INT -> CLong(toInt())
    IntOrLong.LONG -> CLong(toLong())
}

public fun Long.toCLong(): CLong = when (C_LONG_TYPE) {
    IntOrLong.INT -> CLong(toInt())
    IntOrLong.LONG -> CLong(this)
}

public fun ULong.toCLong(): CLong = when (C_LONG_TYPE) {
    IntOrLong.INT -> CLong(toInt())
    IntOrLong.LONG -> CLong(toLong())
}

public fun Float.toCLong(): CLong = when (C_LONG_TYPE) {
    IntOrLong.INT -> CLong(toInt())
    IntOrLong.LONG -> CLong(toLong())
}

public fun Double.toCLong(): CLong = when (C_LONG_TYPE) {
    IntOrLong.INT -> CLong(toInt())
    IntOrLong.LONG -> CLong(toLong())
}

public operator fun Byte.plus(other: CLong): CLong = when (C_LONG_TYPE) {
    IntOrLong.INT -> CLong(this + other.toInt())
    IntOrLong.LONG -> CLong(this + other.value)
}

public operator fun Short.plus(other: CLong): CLong = when (C_LONG_TYPE) {
    IntOrLong.INT -> CLong(this + other.toInt())
    IntOrLong.LONG -> CLong(this + other.value)
}

public operator fun Int.plus(other: CLong): CLong = when (C_LONG_TYPE) {
    IntOrLong.INT -> CLong(this + other.toInt())
    IntOrLong.LONG -> CLong(this + other.value)
}

public operator fun Long.plus(other: CLong): Long {
    return this + other.value
}

public operator fun Float.plus(other: CLong): Float {
    return this + other.toFloat()
}

public operator fun Double.plus(other: CLong): Double {
    return this + other.toDouble()
}

public operator fun Byte.minus(other: CLong): CLong = when (C_LONG_TYPE) {
    IntOrLong.INT -> CLong(this - other.toInt())
    IntOrLong.LONG -> CLong(this - other.value)
}

public operator fun Short.minus(other: CLong): CLong = when (C_LONG_TYPE) {
    IntOrLong.INT -> CLong(this - other.toInt())
    IntOrLong.LONG -> CLong(this - other.value)
}

public operator fun Int.minus(other: CLong): CLong = when (C_LONG_TYPE) {
    IntOrLong.INT -> CLong(this - other.toInt())
    IntOrLong.LONG -> CLong(this - other.value)
}

public operator fun Long.minus(other: CLong): Long {
    return this - other.value
}

public operator fun Float.minus(other: CLong): Float {
    return this - other.toFloat()
}

public operator fun Double.minus(other: CLong): Double {
    return this - other.toDouble()
}

public operator fun Byte.times(other: CLong): CLong = when (C_LONG_TYPE) {
    IntOrLong.INT -> CLong(this * other.toInt())
    IntOrLong.LONG -> CLong(this * other.value)
}

public operator fun Short.times(other: CLong): CLong = when (C_LONG_TYPE) {
    IntOrLong.INT -> CLong(this * other.toInt())
    IntOrLong.LONG -> CLong(this * other.value)
}

public operator fun Int.times(other: CLong): CLong = when (C_LONG_TYPE) {
    IntOrLong.INT -> CLong(this * other.toInt())
    IntOrLong.LONG -> CLong(this * other.value)
}

public operator fun Long.times(other: CLong): Long {
    return this * other.value
}

public operator fun Float.times(other: CLong): Float {
    return this * other.toFloat()
}

public operator fun Double.times(other: CLong): Double {
    return this * other.toDouble()
}

public operator fun Byte.div(other: CLong): CLong = when (C_LONG_TYPE) {
    IntOrLong.INT -> CLong(this / other.toInt())
    IntOrLong.LONG -> CLong(this / other.value)
}

public operator fun Short.div(other: CLong): CLong = when (C_LONG_TYPE) {
    IntOrLong.INT -> CLong(this / other.toInt())
    IntOrLong.LONG -> CLong(this / other.value)
}

public operator fun Int.div(other: CLong): CLong = when (C_LONG_TYPE) {
    IntOrLong.INT -> CLong(this / other.toInt())
    IntOrLong.LONG -> CLong(this / other.value)
}

public operator fun Long.div(other: CLong): Long {
    return this / other.value
}

public operator fun Float.div(other: CLong): Float {
    return this / other.toFloat()
}

public operator fun Double.div(other: CLong): Double {
    return this / other.toDouble()
}

public operator fun Byte.rem(other: CLong): CLong = when (C_LONG_TYPE) {
    IntOrLong.INT -> CLong(this % other.toInt())
    IntOrLong.LONG -> CLong(this % other.value)
}

public operator fun Short.rem(other: CLong): CLong = when (C_LONG_TYPE) {
    IntOrLong.INT -> CLong(this % other.toInt())
    IntOrLong.LONG -> CLong(this % other.value)
}

public operator fun Int.rem(other: CLong): CLong = when (C_LONG_TYPE) {
    IntOrLong.INT -> CLong(this % other.toInt())
    IntOrLong.LONG -> CLong(this % other.value)
}

public operator fun Long.rem(other: CLong): Long {
    return this % other.value
}

public operator fun Float.rem(other: CLong): Float {
    return this % other.toFloat()
}

public operator fun Double.rem(other: CLong): Double {
    return this % other.toDouble()
}

public operator fun Byte.compareTo(other: CLong): Int = when (C_LONG_TYPE) {
    IntOrLong.INT -> compareTo(other.toInt())
    IntOrLong.LONG -> compareTo(other.value)
}

public operator fun Short.compareTo(other: CLong): Int = when (C_LONG_TYPE) {
    IntOrLong.INT -> compareTo(other.toInt())
    IntOrLong.LONG -> compareTo(other.value)
}

public operator fun Int.compareTo(other: CLong): Int = when (C_LONG_TYPE) {
    IntOrLong.INT -> compareTo(other.toInt())
    IntOrLong.LONG -> compareTo(other.value)
}

public operator fun Long.compareTo(other: CLong): Int = when (C_LONG_TYPE) {
    IntOrLong.INT -> compareTo(other.toInt())
    IntOrLong.LONG -> compareTo(other.value)
}

public operator fun Float.compareTo(other: CLong): Int = when (C_LONG_TYPE) {
    IntOrLong.INT -> compareTo(other.toInt())
    IntOrLong.LONG -> compareTo(other.value)
}

public operator fun Double.compareTo(other: CLong): Int = when (C_LONG_TYPE) {
    IntOrLong.INT -> compareTo(other.toInt())
    IntOrLong.LONG -> compareTo(other.value)
}
