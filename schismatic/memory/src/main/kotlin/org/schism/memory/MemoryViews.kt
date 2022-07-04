@file:Suppress("NOTHING_TO_INLINE")

package org.schism.memory

import org.schism.math.timesExact

public val Memory.bytes: MemoryBytes get() {
    return MemoryBytes(this)
}

public val Memory.ubytes: MemoryUBytes get() {
    return MemoryUBytes(this)
}

public val Memory.chars: MemoryChars get() {
    return MemoryChars(this)
}

public val Memory.shorts: MemoryShorts get() {
    return MemoryShorts(this)
}

public val Memory.ushorts: MemoryUShorts get() {
    return MemoryUShorts(this)
}

public val Memory.ints: MemoryInts get() {
    return MemoryInts(this)
}

public val Memory.uints: MemoryUInts get() {
    return MemoryUInts(this)
}

public val Memory.longs: MemoryLongs get() {
    return MemoryLongs(this)
}

public val Memory.ulongs: MemoryULongs get() {
    return MemoryULongs(this)
}

public val Memory.floats: MemoryFloats get() {
    return MemoryFloats(this)
}

public val Memory.doubles: MemoryDoubles get() {
    return MemoryDoubles(this)
}

@JvmInline public value class MemoryBytes internal constructor(public val memory: Memory) {
    public inline val size: Long get() {
        return memory.size
    }

    public inline val indices: LongRange get() {
        return 0 until size
    }

    public inline operator fun get(index: Long): Byte {
        return memory.getByte(index)
    }

    public inline operator fun set(index: Long, value: Byte) {
        memory.setByte(value, index)
    }

    public operator fun iterator(): Iterator {
        return Iterator(this)
    }

    public class Iterator internal constructor(@PublishedApi internal val elements: MemoryBytes) {
        @PublishedApi internal var index: Long = 0L

        public inline operator fun hasNext(): Boolean {
            return index < elements.size
        }

        public inline operator fun next(): Byte {
            return elements[index++]
        }

        public companion object
    }

    public companion object
}

@JvmInline public value class MemoryUBytes internal constructor(public val memory: Memory) {
    public inline val size: Long get() {
        return memory.size
    }

    public inline val indices: LongRange get() {
        return 0 until size
    }

    public inline operator fun get(index: Long): UByte {
        return memory.getUByte(index)
    }

    public inline operator fun set(index: Long, value: UByte) {
        memory.setUByte(value, index)
    }

    public operator fun iterator(): Iterator {
        return Iterator(this)
    }

    public class Iterator internal constructor(@PublishedApi internal val elements: MemoryUBytes) {
        @PublishedApi internal var index: Long = 0L

        public inline operator fun hasNext(): Boolean {
            return index < elements.size
        }

        public inline operator fun next(): UByte {
            return elements[index++]
        }

        public companion object
    }

    public companion object
}

@JvmInline public value class MemoryChars internal constructor(public val memory: Memory) {
    public inline val size: Long get() {
        return memory.size / ELEMENT_STRIDE
    }

    public inline val indices: LongRange get() {
        return 0 until size
    }

    public inline operator fun get(index: Long): Char {
        return memory.getChar(index timesExact ELEMENT_STRIDE)
    }

    public inline operator fun set(index: Long, value: Char) {
        memory.setChar(value, index timesExact ELEMENT_STRIDE)
    }

    public operator fun iterator(): Iterator {
        return Iterator(this)
    }

    public class Iterator internal constructor(@PublishedApi internal val elements: MemoryChars) {
        @PublishedApi internal var index: Long = 0L

        public inline operator fun hasNext(): Boolean {
            return index < elements.size
        }

        public inline operator fun next(): Char {
            return elements[index].also { index += ELEMENT_STRIDE }
        }

        public companion object
    }

    public companion object {
        @PublishedApi internal const val ELEMENT_STRIDE: Long = 2
    }
}

@JvmInline public value class MemoryShorts internal constructor(public val memory: Memory) {
    public inline val size: Long get() {
        return memory.size / ELEMENT_STRIDE
    }

    public inline val indices: LongRange get() {
        return 0 until size
    }

    public inline operator fun get(index: Long): Short {
        return memory.getShort(index timesExact ELEMENT_STRIDE)
    }

    public inline operator fun set(index: Long, value: Short) {
        memory.setShort(value, index timesExact ELEMENT_STRIDE)
    }

    public operator fun iterator(): Iterator {
        return Iterator(this)
    }

    public class Iterator internal constructor(@PublishedApi internal val elements: MemoryShorts) {
        @PublishedApi internal var index: Long = 0L

        public inline operator fun hasNext(): Boolean {
            return index < elements.size
        }

        public inline operator fun next(): Short {
            return elements[index].also { index += ELEMENT_STRIDE }
        }

        public companion object
    }

    public companion object {
        @PublishedApi internal const val ELEMENT_STRIDE: Long = 2
    }
}

@JvmInline public value class MemoryUShorts internal constructor(public val memory: Memory) {
    public inline val size: Long get() {
        return memory.size / ELEMENT_STRIDE
    }

    public inline val indices: LongRange get() {
        return 0 until size
    }

    public inline operator fun get(index: Long): UShort {
        return memory.getUShort(index timesExact ELEMENT_STRIDE)
    }

    public inline operator fun set(index: Long, value: UShort) {
        memory.setUShort(value, index timesExact ELEMENT_STRIDE)
    }

    public operator fun iterator(): Iterator {
        return Iterator(this)
    }

    public class Iterator internal constructor(@PublishedApi internal val elements: MemoryUShorts) {
        @PublishedApi internal var index: Long = 0L

        public inline operator fun hasNext(): Boolean {
            return index < elements.size
        }

        public inline operator fun next(): UShort {
            return elements[index].also { index += ELEMENT_STRIDE }
        }

        public companion object
    }

    public companion object {
        @PublishedApi internal const val ELEMENT_STRIDE: Long = 2
    }
}

@JvmInline public value class MemoryInts internal constructor(public val memory: Memory) {
    public inline val size: Long get() {
        return memory.size / ELEMENT_STRIDE
    }

    public inline val indices: LongRange get() {
        return 0 until size
    }

    public inline operator fun get(index: Long): Int {
        return memory.getInt(index timesExact ELEMENT_STRIDE)
    }

    public inline operator fun set(index: Long, value: Int) {
        memory.setInt(value, index timesExact ELEMENT_STRIDE)
    }

    public operator fun iterator(): Iterator {
        return Iterator(this)
    }

    public class Iterator internal constructor(@PublishedApi internal val elements: MemoryInts) {
        @PublishedApi internal var index: Long = 0L

        public inline operator fun hasNext(): Boolean {
            return index < elements.size
        }

        public inline operator fun next(): Int {
            return elements[index].also { index += ELEMENT_STRIDE }
        }

        public companion object
    }

    public companion object {
        @PublishedApi internal const val ELEMENT_STRIDE: Long = 4
    }
}

@JvmInline public value class MemoryUInts internal constructor(public val memory: Memory) {
    public inline val size: Long get() {
        return memory.size / ELEMENT_STRIDE
    }

    public inline val indices: LongRange get() {
        return 0 until size
    }

    public inline operator fun get(index: Long): UInt {
        return memory.getUInt(index timesExact ELEMENT_STRIDE)
    }

    public inline operator fun set(index: Long, value: UInt) {
        memory.setUInt(value, index timesExact ELEMENT_STRIDE)
    }

    public operator fun iterator(): Iterator {
        return Iterator(this)
    }

    public class Iterator internal constructor(@PublishedApi internal val elements: MemoryUInts) {
        @PublishedApi internal var index: Long = 0L

        public inline operator fun hasNext(): Boolean {
            return index < elements.size
        }

        public inline operator fun next(): UInt {
            return elements[index].also { index += ELEMENT_STRIDE }
        }

        public companion object
    }

    public companion object {
        @PublishedApi internal const val ELEMENT_STRIDE: Long = 4
    }
}

@JvmInline public value class MemoryFloats internal constructor(public val memory: Memory) {
    public inline val size: Long get() {
        return memory.size / ELEMENT_STRIDE
    }

    public inline val indices: LongRange get() {
        return 0 until size
    }

    public inline operator fun get(index: Long): Float {
        return memory.getFloat(index timesExact ELEMENT_STRIDE)
    }

    public inline operator fun set(index: Long, value: Float) {
        memory.setFloat(value, index timesExact ELEMENT_STRIDE)
    }

    public operator fun iterator(): Iterator {
        return Iterator(this)
    }

    public class Iterator internal constructor(@PublishedApi internal val elements: MemoryFloats) {
        @PublishedApi internal var index: Long = 0L

        public inline operator fun hasNext(): Boolean {
            return index < elements.size
        }

        public inline operator fun next(): Float {
            return elements[index].also { index += ELEMENT_STRIDE }
        }

        public companion object
    }

    public companion object {
        @PublishedApi internal const val ELEMENT_STRIDE: Long = 4
    }
}

@JvmInline public value class MemoryLongs internal constructor(public val memory: Memory) {
    public inline val size: Long get() {
        return memory.size / ELEMENT_STRIDE
    }

    public inline val indices: LongRange get() {
        return 0 until size
    }

    public inline operator fun get(index: Long): Long {
        return memory.getLong(index timesExact ELEMENT_STRIDE)
    }

    public inline operator fun set(index: Long, value: Long) {
        memory.setLong(value, index timesExact ELEMENT_STRIDE)
    }

    public operator fun iterator(): Iterator {
        return Iterator(this)
    }

    public class Iterator internal constructor(@PublishedApi internal val elements: MemoryLongs) {
        @PublishedApi internal var index: Long = 0L

        public inline operator fun hasNext(): Boolean {
            return index < elements.size
        }

        public inline operator fun next(): Long {
            return elements[index].also { index += ELEMENT_STRIDE }
        }

        public companion object
    }

    public companion object {
        @PublishedApi internal const val ELEMENT_STRIDE: Long = 8
    }
}

@JvmInline public value class MemoryULongs internal constructor(public val memory: Memory) {
    public inline val size: Long get() {
        return memory.size / ELEMENT_STRIDE
    }

    public inline val indices: LongRange get() {
        return 0 until size
    }

    public inline operator fun get(index: Long): ULong {
        return memory.getULong(index timesExact ELEMENT_STRIDE)
    }

    public inline operator fun set(index: Long, value: ULong) {
        memory.setULong(value, index timesExact ELEMENT_STRIDE)
    }

    public operator fun iterator(): Iterator {
        return Iterator(this)
    }

    public class Iterator internal constructor(@PublishedApi internal val elements: MemoryULongs) {
        @PublishedApi internal var index: Long = 0L

        public inline operator fun hasNext(): Boolean {
            return index < elements.size
        }

        public inline operator fun next(): ULong {
            return elements[index].also { index += ELEMENT_STRIDE }
        }

        public companion object
    }

    public companion object {
        @PublishedApi internal const val ELEMENT_STRIDE: Long = 8
    }
}

@JvmInline public value class MemoryDoubles internal constructor(public val memory: Memory) {
    public inline val size: Long get() {
        return memory.size / ELEMENT_STRIDE
    }

    public inline val indices: LongRange get() {
        return 0 until size
    }

    public inline operator fun get(index: Long): Double {
        return memory.getDouble(index timesExact ELEMENT_STRIDE)
    }

    public inline operator fun set(index: Long, value: Double) {
        memory.setDouble(value, index timesExact ELEMENT_STRIDE)
    }

    public operator fun iterator(): Iterator {
        return Iterator(this)
    }

    public class Iterator internal constructor(@PublishedApi internal val elements: MemoryDoubles) {
        @PublishedApi internal var index: Long = 0L

        public inline operator fun hasNext(): Boolean {
            return index < elements.size
        }

        public inline operator fun next(): Double {
            return elements[index].also { index += ELEMENT_STRIDE }
        }

        public companion object
    }

    public companion object {
        @PublishedApi internal const val ELEMENT_STRIDE: Long = 8
    }
}
