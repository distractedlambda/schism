package org.schism.foreign

@JvmInline public value class CSizeT internal constructor(internal val value: ULong) {
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

    public fun toFloat(): Float = when (ADDRESS_TYPE) {
        IntOrLong.INT -> toUInt().toFloat()
        IntOrLong.LONG -> value.toFloat()
    }

    public fun toDouble(): Double = when (ADDRESS_TYPE) {
        IntOrLong.INT -> toUInt().toDouble()
        IntOrLong.LONG -> value.toDouble()
    }

    public operator fun compareTo(other: UByte): Int = when (ADDRESS_TYPE) {
        IntOrLong.INT -> toUInt().compareTo(other)
        IntOrLong.LONG -> value.compareTo(other)
    }

    public operator fun compareTo(other: UShort): Int = when (ADDRESS_TYPE) {
        IntOrLong.INT -> toUInt().compareTo(other)
        IntOrLong.LONG -> value.compareTo(other)
    }

    public operator fun compareTo(other: UInt): Int = when (ADDRESS_TYPE) {
        IntOrLong.INT -> toUInt().compareTo(other)
        IntOrLong.LONG -> value.compareTo(other)
    }

    public operator fun compareTo(other: ULong): Int = when (ADDRESS_TYPE) {
        IntOrLong.INT -> toUInt().compareTo(other)
        IntOrLong.LONG -> value.compareTo(other)
    }

    public operator fun compareTo(other: CSizeT): Int = when (ADDRESS_TYPE) {
        IntOrLong.INT -> toUInt().compareTo(other.toUInt())
        IntOrLong.LONG -> value.compareTo(other.value)
    }

    public operator fun unaryPlus(): CSizeT {
        return this
    }

    public fun inv(): CSizeT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CSizeT(toUInt().inv())
        IntOrLong.LONG -> CSizeT(value.inv())
    }

    public operator fun plus(other: UByte): CSizeT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CSizeT(toUInt() + other)
        IntOrLong.LONG -> CSizeT(value + other)
    }

    public operator fun plus(other: UShort): CSizeT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CSizeT(toUInt() + other)
        IntOrLong.LONG -> CSizeT(value + other)
    }

    public operator fun plus(other: UInt): CSizeT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CSizeT(toUInt() + other)
        IntOrLong.LONG -> CSizeT(value + other)
    }

    public operator fun plus(other: ULong): ULong {
        return value + other
    }

    public operator fun plus(other: CSizeT): CSizeT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CSizeT(toUInt() + other.toUInt())
        IntOrLong.LONG -> CSizeT(value + other.value)
    }

    public operator fun plus(other: Float): Float {
        return toFloat() + other
    }

    public operator fun plus(other: Double): Double {
        return toDouble() + other
    }

    public operator fun minus(other: UByte): CSizeT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CSizeT(toUInt() - other)
        IntOrLong.LONG -> CSizeT(value - other)
    }

    public operator fun minus(other: UShort): CSizeT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CSizeT(toUInt() - other)
        IntOrLong.LONG -> CSizeT(value - other)
    }

    public operator fun minus(other: UInt): CSizeT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CSizeT(toUInt() - other)
        IntOrLong.LONG -> CSizeT(value - other)
    }

    public operator fun minus(other: ULong): ULong {
        return value - other
    }

    public operator fun minus(other: CSizeT): CSizeT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CSizeT(toUInt() - other.toUInt())
        IntOrLong.LONG -> CSizeT(value - other.value)
    }

    public operator fun minus(other: Float): Float {
        return toFloat() - other
    }

    public operator fun minus(other: Double): Double {
        return toDouble() - other
    }

    public operator fun times(other: UByte): CSizeT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CSizeT(toUInt() * other)
        IntOrLong.LONG -> CSizeT(value * other)
    }

    public operator fun times(other: UShort): CSizeT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CSizeT(toUInt() * other)
        IntOrLong.LONG -> CSizeT(value * other)
    }

    public operator fun times(other: UInt): CSizeT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CSizeT(toUInt() * other)
        IntOrLong.LONG -> CSizeT(value * other)
    }

    public operator fun times(other: ULong): ULong {
        return value * other
    }

    public operator fun times(other: CSizeT): CSizeT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CSizeT(toUInt() * other.toUInt())
        IntOrLong.LONG -> CSizeT(value * other.value)
    }

    public operator fun times(other: Float): Float {
        return toFloat() * other
    }

    public operator fun times(other: Double): Double {
        return toDouble() * other
    }

    public operator fun div(other: UByte): CSizeT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CSizeT(toUInt() / other)
        IntOrLong.LONG -> CSizeT(value / other)
    }

    public operator fun div(other: UShort): CSizeT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CSizeT(toUInt() / other)
        IntOrLong.LONG -> CSizeT(value / other)
    }

    public operator fun div(other: UInt): CSizeT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CSizeT(toUInt() / other)
        IntOrLong.LONG -> CSizeT(value / other)
    }

    public operator fun div(other: ULong): ULong {
        return value / other
    }

    public operator fun div(other: CSizeT): CSizeT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CSizeT(toUInt() / other.toUInt())
        IntOrLong.LONG -> CSizeT(value / other.value)
    }

    public operator fun div(other: Float): Float {
        return toFloat() / other
    }

    public operator fun div(other: Double): Double {
        return toDouble() / other
    }

    public operator fun rem(other: UByte): CSizeT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CSizeT(toUInt() % other)
        IntOrLong.LONG -> CSizeT(value % other)
    }

    public operator fun rem(other: UShort): CSizeT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CSizeT(toUInt() % other)
        IntOrLong.LONG -> CSizeT(value % other)
    }

    public operator fun rem(other: UInt): CSizeT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CSizeT(toUInt() % other)
        IntOrLong.LONG -> CSizeT(value % other)
    }

    public operator fun rem(other: ULong): ULong {
        return value % other
    }

    public operator fun rem(other: CSizeT): CSizeT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CSizeT(toUInt() % other.toUInt())
        IntOrLong.LONG -> CSizeT(value % other.value)
    }

    public operator fun rem(other: Float): Float {
        return toFloat() % other
    }

    public operator fun rem(other: Double): Double {
        return toDouble() % other
    }

    public operator fun inc(): CSizeT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CSizeT(toUInt().inc())
        IntOrLong.LONG -> CSizeT(value.inc())
    }

    public operator fun dec(): CSizeT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CSizeT(toUInt().dec())
        IntOrLong.LONG -> CSizeT(value.dec())
    }

    public infix fun and(other: CSizeT): CSizeT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CSizeT(toUInt() and other.toUInt())
        IntOrLong.LONG -> CSizeT(value and other.value)
    }

    public infix fun or(other: CSizeT): CSizeT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CSizeT(toUInt() or other.toUInt())
        IntOrLong.LONG -> CSizeT(value or other.value)
    }

    public infix fun xor(other: CSizeT): CSizeT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CSizeT(toUInt() xor other.toUInt())
        IntOrLong.LONG -> CSizeT(value xor other.value)
    }

    public infix fun shl(count: Int): CSizeT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CSizeT(toUInt() shl count)
        IntOrLong.LONG -> CSizeT(value shl count)
    }

    public infix fun shr(count: Int): CSizeT = when (ADDRESS_TYPE) {
        IntOrLong.INT -> CSizeT(toUInt() shr count)
        IntOrLong.LONG -> CSizeT(value shr count)
    }

    override fun toString(): String = when (ADDRESS_TYPE) {
        IntOrLong.INT -> toUInt().toString()
        IntOrLong.LONG -> value.toString()
    }

    public companion object {
        public val MIN_VALUE: CSizeT = when (ADDRESS_TYPE) {
            IntOrLong.INT -> CSizeT(UInt.MIN_VALUE)
            IntOrLong.LONG -> CSizeT(ULong.MAX_VALUE)
        }

        public val MAX_VALUE: CSizeT = when (ADDRESS_TYPE) {
            IntOrLong.INT -> CSizeT(UInt.MAX_VALUE)
            IntOrLong.LONG -> CSizeT(ULong.MAX_VALUE)
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

public fun Byte.toCSizeT(): CSizeT = when (ADDRESS_TYPE) {
    IntOrLong.INT -> CSizeT(toUInt())
    IntOrLong.LONG -> CSizeT(toULong())
}

public fun UByte.toCSizeT(): CSizeT {
    return CSizeT(toULong())
}

public fun Short.toCSizeT(): CSizeT = when (ADDRESS_TYPE) {
    IntOrLong.INT -> CSizeT(toUInt())
    IntOrLong.LONG -> CSizeT(toULong())
}

public fun UShort.toCSizeT(): CSizeT {
    return CSizeT(toULong())
}

public fun Int.toCSizeT(): CSizeT = when (ADDRESS_TYPE) {
    IntOrLong.INT -> CSizeT(toUInt())
    IntOrLong.LONG -> CSizeT(toULong())
}

public fun UInt.toCSizeT(): CSizeT {
    return CSizeT(this)
}

public fun Long.toCSizeT(): CSizeT = when (ADDRESS_TYPE) {
    IntOrLong.INT -> CSizeT(toUInt())
    IntOrLong.LONG -> CSizeT(toULong())
}

public fun ULong.toCSizeT(): CSizeT = when (ADDRESS_TYPE) {
    IntOrLong.INT -> CSizeT(toUInt())
    IntOrLong.LONG -> CSizeT(this)
}

public fun Float.toCSizeT(): CSizeT = when (ADDRESS_TYPE) {
    IntOrLong.INT -> CSizeT(toUInt())
    IntOrLong.LONG -> CSizeT(toULong())
}

public fun Double.toCSizeT(): CSizeT = when (ADDRESS_TYPE) {
    IntOrLong.INT -> CSizeT(toUInt())
    IntOrLong.LONG -> CSizeT(toULong())
}

public operator fun UByte.plus(other: CSizeT): CSizeT = when (ADDRESS_TYPE) {
    IntOrLong.INT -> CSizeT(this + other.toUInt())
    IntOrLong.LONG -> CSizeT(this + other.value)
}

public operator fun UShort.plus(other: CSizeT): CSizeT = when (ADDRESS_TYPE) {
    IntOrLong.INT -> CSizeT(this + other.toUInt())
    IntOrLong.LONG -> CSizeT(this + other.value)
}

public operator fun UInt.plus(other: CSizeT): CSizeT = when (ADDRESS_TYPE) {
    IntOrLong.INT -> CSizeT(this + other.toUInt())
    IntOrLong.LONG -> CSizeT(this + other.value)
}

public operator fun ULong.plus(other: CSizeT): ULong {
    return this + other.value
}

public operator fun Float.plus(other: CSizeT): Float {
    return this + other.toFloat()
}

public operator fun Double.plus(other: CSizeT): Double {
    return this + other.toDouble()
}

public operator fun UByte.minus(other: CSizeT): CSizeT = when (ADDRESS_TYPE) {
    IntOrLong.INT -> CSizeT(this - other.toUInt())
    IntOrLong.LONG -> CSizeT(this - other.value)
}

public operator fun UShort.minus(other: CSizeT): CSizeT = when (ADDRESS_TYPE) {
    IntOrLong.INT -> CSizeT(this - other.toUInt())
    IntOrLong.LONG -> CSizeT(this - other.value)
}

public operator fun UInt.minus(other: CSizeT): CSizeT = when (ADDRESS_TYPE) {
    IntOrLong.INT -> CSizeT(this - other.toUInt())
    IntOrLong.LONG -> CSizeT(this - other.value)
}

public operator fun ULong.minus(other: CSizeT): ULong {
    return this - other.value
}

public operator fun Float.minus(other: CSizeT): Float {
    return this - other.toFloat()
}

public operator fun Double.minus(other: CSizeT): Double {
    return this - other.toDouble()
}

public operator fun UByte.times(other: CSizeT): CSizeT = when (ADDRESS_TYPE) {
    IntOrLong.INT -> CSizeT(this * other.toUInt())
    IntOrLong.LONG -> CSizeT(this * other.value)
}

public operator fun UShort.times(other: CSizeT): CSizeT = when (ADDRESS_TYPE) {
    IntOrLong.INT -> CSizeT(this * other.toUInt())
    IntOrLong.LONG -> CSizeT(this * other.value)
}

public operator fun UInt.times(other: CSizeT): CSizeT = when (ADDRESS_TYPE) {
    IntOrLong.INT -> CSizeT(this * other.toUInt())
    IntOrLong.LONG -> CSizeT(this * other.value)
}

public operator fun ULong.times(other: CSizeT): ULong {
    return this * other.value
}

public operator fun Float.times(other: CSizeT): Float {
    return this * other.toFloat()
}

public operator fun Double.times(other: CSizeT): Double {
    return this * other.toDouble()
}

public operator fun UByte.div(other: CSizeT): CSizeT = when (ADDRESS_TYPE) {
    IntOrLong.INT -> CSizeT(this / other.toUInt())
    IntOrLong.LONG -> CSizeT(this / other.value)
}

public operator fun UShort.div(other: CSizeT): CSizeT = when (ADDRESS_TYPE) {
    IntOrLong.INT -> CSizeT(this / other.toUInt())
    IntOrLong.LONG -> CSizeT(this / other.value)
}

public operator fun UInt.div(other: CSizeT): CSizeT = when (ADDRESS_TYPE) {
    IntOrLong.INT -> CSizeT(this / other.toUInt())
    IntOrLong.LONG -> CSizeT(this / other.value)
}

public operator fun ULong.div(other: CSizeT): ULong {
    return this / other.value
}

public operator fun Float.div(other: CSizeT): Float {
    return this / other.toFloat()
}

public operator fun Double.div(other: CSizeT): Double {
    return this / other.toDouble()
}

public operator fun UByte.rem(other: CSizeT): CSizeT = when (ADDRESS_TYPE) {
    IntOrLong.INT -> CSizeT(this % other.toUInt())
    IntOrLong.LONG -> CSizeT(this % other.value)
}

public operator fun UShort.rem(other: CSizeT): CSizeT = when (ADDRESS_TYPE) {
    IntOrLong.INT -> CSizeT(this % other.toUInt())
    IntOrLong.LONG -> CSizeT(this % other.value)
}

public operator fun UInt.rem(other: CSizeT): CSizeT = when (ADDRESS_TYPE) {
    IntOrLong.INT -> CSizeT(this % other.toUInt())
    IntOrLong.LONG -> CSizeT(this % other.value)
}

public operator fun ULong.rem(other: CSizeT): ULong {
    return this % other.value
}

public operator fun Float.rem(other: CSizeT): Float {
    return this % other.toFloat()
}

public operator fun Double.rem(other: CSizeT): Double {
    return this % other.toDouble()
}

public operator fun UByte.compareTo(other: CSizeT): Int = when (ADDRESS_TYPE) {
    IntOrLong.INT -> compareTo(other.toUInt())
    IntOrLong.LONG -> compareTo(other.value)
}

public operator fun UShort.compareTo(other: CSizeT): Int = when (ADDRESS_TYPE) {
    IntOrLong.INT -> compareTo(other.toUInt())
    IntOrLong.LONG -> compareTo(other.value)
}

public operator fun UInt.compareTo(other: CSizeT): Int = when (ADDRESS_TYPE) {
    IntOrLong.INT -> compareTo(other.toUInt())
    IntOrLong.LONG -> compareTo(other.value)
}

public operator fun ULong.compareTo(other: CSizeT): Int = when (ADDRESS_TYPE) {
    IntOrLong.INT -> compareTo(other.toUInt())
    IntOrLong.LONG -> compareTo(other.value)
}
