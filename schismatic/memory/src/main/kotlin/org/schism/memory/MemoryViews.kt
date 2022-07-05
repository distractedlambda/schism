@file:Suppress("NOTHING_TO_INLINE")

package org.schism.memory

import org.schism.math.timesExact
import kotlin.reflect.KProperty

public val Memory.byte: MemoryByteView get() {
    return MemoryByteView(slice(size = 1))
}

public val Memory.ubyte: MemoryUByteView get() {
    return MemoryUByteView(slice(size = 1))
}

public val Memory.char: MemoryCharView get() {
    return MemoryCharView(slice(size = 2))
}

public val Memory.short: MemoryShortView get() {
    return MemoryShortView(slice(size = 2))
}

public val Memory.ushort: MemoryUShortView get() {
    return MemoryUShortView(slice(size = 2))
}

public val Memory.int: MemoryIntView get() {
    return MemoryIntView(slice(size = 4))
}

public val Memory.uint: MemoryUIntView get() {
    return MemoryUIntView(slice(size = 4))
}

public val Memory.long: MemoryLongView get() {
    return MemoryLongView(slice(size = 8))
}

public val Memory.ulong: MemoryULongView get() {
    return MemoryULongView(slice(size = 8))
}

public val Memory.float: MemoryFloatView get() {
    return MemoryFloatView(slice(size = 4))
}

public val Memory.double: MemoryDoubleView get() {
    return MemoryDoubleView(slice(size = 8))
}

public val Memory.pointer: MemoryPointerView get() {
    return MemoryPointerView(slice(size = NativeAddress.BYTE_SIZE.toLong()))
}

public fun Memory.byte(offset: Long): MemoryByteView {
    return MemoryByteView(slice(offset, 1))
}

public fun Memory.ubyte(offset: Long): MemoryUByteView {
    return MemoryUByteView(slice(offset, 1))
}

public fun Memory.char(offset: Long): MemoryCharView {
    return MemoryCharView(slice(offset, 2))
}

public fun Memory.short(offset: Long): MemoryShortView {
    return MemoryShortView(slice(offset, 2))
}

public fun Memory.ushort(offset: Long): MemoryUShortView {
    return MemoryUShortView(slice(offset, 2))
}

public fun Memory.int(offset: Long): MemoryIntView {
    return MemoryIntView(slice(offset, 4))
}

public fun Memory.uint(offset: Long): MemoryUIntView {
    return MemoryUIntView(slice(offset, 4))
}

public fun Memory.long(offset: Long): MemoryLongView {
    return MemoryLongView(slice(offset, 8))
}

public fun Memory.ulong(offset: Long): MemoryULongView {
    return MemoryULongView(slice(offset, 8))
}

public fun Memory.float(offset: Long): MemoryFloatView {
    return MemoryFloatView(slice(offset, 4))
}

public fun Memory.double(offset: Long): MemoryDoubleView {
    return MemoryDoubleView(slice(offset, 8))
}

public fun Memory.pointer(offset: Long): MemoryPointerView {
    return MemoryPointerView(slice(offset, NativeAddress.BYTE_SIZE.toLong()))
}

@JvmInline public value class MemoryByteView internal constructor(public val memory: Memory) {
    public inline var value: Byte
        get() = memory.getByte()
        set(newValue) = memory.setByte(newValue)

    public inline operator fun getValue(thisRef: Any?, property: KProperty<*>): Byte {
        return value
    }

    public inline operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Byte) {
        this.value = value
    }
}

@JvmInline public value class MemoryUByteView internal constructor(public val memory: Memory) {
    public inline var value: UByte
        get() = memory.getUByte()
        set(newValue) = memory.setUByte(newValue)

    public inline operator fun getValue(thisRef: Any?, property: KProperty<*>): UByte {
        return value
    }

    public inline operator fun setValue(thisRef: Any?, property: KProperty<*>, value: UByte) {
        this.value = value
    }
}

@JvmInline public value class MemoryCharView internal constructor(public val memory: Memory) {
    public inline var value: Char
        get() = memory.getChar()
        set(newValue) = memory.setChar(newValue)

    public inline operator fun getValue(thisRef: Any?, property: KProperty<*>): Char {
        return value
    }

    public inline operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Char) {
        this.value = value
    }
}

@JvmInline public value class MemoryShortView internal constructor(public val memory: Memory) {
    public inline var value: Short
        get() = memory.getShort()
        set(newValue) = memory.setShort(newValue)

    public inline operator fun getValue(thisRef: Any?, property: KProperty<*>): Short {
        return value
    }

    public inline operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Short) {
        this.value = value
    }
}

@JvmInline public value class MemoryUShortView internal constructor(public val memory: Memory) {
    public inline var value: UShort
        get() = memory.getUShort()
        set(newValue) = memory.setUShort(newValue)

    public inline operator fun getValue(thisRef: Any?, property: KProperty<*>): UShort {
        return value
    }

    public inline operator fun setValue(thisRef: Any?, property: KProperty<*>, value: UShort) {
        this.value = value
    }
}

@JvmInline public value class MemoryIntView internal constructor(public val memory: Memory) {
    public inline var value: Int
        get() = memory.getInt()
        set(newValue) = memory.setInt(newValue)

    public inline operator fun getValue(thisRef: Any?, property: KProperty<*>): Int {
        return value
    }

    public inline operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
        this.value = value
    }
}

@JvmInline public value class MemoryUIntView internal constructor(public val memory: Memory) {
    public inline var value: UInt
        get() = memory.getUInt()
        set(newValue) = memory.setUInt(newValue)

    public inline operator fun getValue(thisRef: Any?, property: KProperty<*>): UInt {
        return value
    }

    public inline operator fun setValue(thisRef: Any?, property: KProperty<*>, value: UInt) {
        this.value = value
    }
}

@JvmInline public value class MemoryLongView internal constructor(public val memory: Memory) {
    public inline var value: Long
        get() = memory.getLong()
        set(newValue) = memory.setLong(newValue)

    public inline operator fun getValue(thisRef: Any?, property: KProperty<*>): Long {
        return value
    }

    public inline operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Long) {
        this.value = value
    }
}

@JvmInline public value class MemoryULongView internal constructor(public val memory: Memory) {
    public inline var value: ULong
        get() = memory.getULong()
        set(newValue) = memory.setULong(newValue)

    public inline operator fun getValue(thisRef: Any?, property: KProperty<*>): ULong {
        return value
    }

    public inline operator fun setValue(thisRef: Any?, property: KProperty<*>, value: ULong) {
        this.value = value
    }
}

@JvmInline public value class MemoryFloatView internal constructor(public val memory: Memory) {
    public inline var value: Float
        get() = memory.getFloat()
        set(newValue) = memory.setFloat(newValue)

    public inline operator fun getValue(thisRef: Any?, property: KProperty<*>): Float {
        return value
    }

    public inline operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Float) {
        this.value = value
    }
}

@JvmInline public value class MemoryDoubleView internal constructor(public val memory: Memory) {
    public inline var value: Double
        get() = memory.getDouble()
        set(newValue) = memory.setDouble(newValue)

    public inline operator fun getValue(thisRef: Any?, property: KProperty<*>): Double {
        return value
    }

    public inline operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Double) {
        this.value = value
    }
}

@JvmInline public value class MemoryPointerView internal constructor(public val memory: Memory) {
    public inline var value: NativeAddress
        get() = memory.getPointer()
        set(newValue) = memory.setPointer(newValue)

    public inline operator fun getValue(thisRef: Any?, property: KProperty<*>): NativeAddress {
        return value
    }

    public inline operator fun setValue(thisRef: Any?, property: KProperty<*>, value: NativeAddress) {
        this.value = value
    }
}

public val Memory.bytes: MemoryByteArrayView get() {
    return MemoryByteArrayView(this)
}

public val Memory.ubytes: MemoryUByteArrayView get() {
    return MemoryUByteArrayView(this)
}

public val Memory.chars: MemoryCharArrayView get() {
    return MemoryCharArrayView(this)
}

public val Memory.shorts: MemoryShortArrayView get() {
    return MemoryShortArrayView(this)
}

public val Memory.ushorts: MemoryUShortArrayView get() {
    return MemoryUShortArrayView(this)
}

public val Memory.ints: MemoryIntArrayView get() {
    return MemoryIntArrayView(this)
}

public val Memory.uints: MemoryUIntArrayView get() {
    return MemoryUIntArrayView(this)
}

public val Memory.longs: MemoryLongArrayView get() {
    return MemoryLongArrayView(this)
}

public val Memory.ulongs: MemoryULongArrayView get() {
    return MemoryULongArrayView(this)
}

public val Memory.floats: MemoryFloatArrayView get() {
    return MemoryFloatArrayView(this)
}

public val Memory.doubles: MemoryDoubleArrayView get() {
    return MemoryDoubleArrayView(this)
}

public val Memory.pointers: MemoryPointerArrayView get() {
    return MemoryPointerArrayView(this)
}

@JvmInline public value class MemoryByteArrayView internal constructor(public val memory: Memory) {
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

    public class Iterator internal constructor(@PublishedApi internal val elements: MemoryByteArrayView) {
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

@JvmInline public value class MemoryUByteArrayView internal constructor(public val memory: Memory) {
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

    public class Iterator internal constructor(@PublishedApi internal val elements: MemoryUByteArrayView) {
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

@JvmInline public value class MemoryCharArrayView internal constructor(public val memory: Memory) {
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

    public class Iterator internal constructor(@PublishedApi internal val elements: MemoryCharArrayView) {
        @PublishedApi internal var index: Long = 0L

        public inline operator fun hasNext(): Boolean {
            return index < elements.size
        }

        public inline operator fun next(): Char {
            return elements[index++]
        }

        public companion object
    }

    public companion object {
        @PublishedApi internal const val ELEMENT_STRIDE: Long = 2
    }
}

@JvmInline public value class MemoryShortArrayView internal constructor(public val memory: Memory) {
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

    public class Iterator internal constructor(@PublishedApi internal val elements: MemoryShortArrayView) {
        @PublishedApi internal var index: Long = 0L

        public inline operator fun hasNext(): Boolean {
            return index < elements.size
        }

        public inline operator fun next(): Short {
            return elements[index++]
        }

        public companion object
    }

    public companion object {
        @PublishedApi internal const val ELEMENT_STRIDE: Long = 2
    }
}

@JvmInline public value class MemoryUShortArrayView internal constructor(public val memory: Memory) {
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

    public class Iterator internal constructor(@PublishedApi internal val elements: MemoryUShortArrayView) {
        @PublishedApi internal var index: Long = 0L

        public inline operator fun hasNext(): Boolean {
            return index < elements.size
        }

        public inline operator fun next(): UShort {
            return elements[index++]
        }

        public companion object
    }

    public companion object {
        @PublishedApi internal const val ELEMENT_STRIDE: Long = 2
    }
}

@JvmInline public value class MemoryIntArrayView internal constructor(public val memory: Memory) {
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

    public class Iterator internal constructor(@PublishedApi internal val elements: MemoryIntArrayView) {
        @PublishedApi internal var index: Long = 0L

        public inline operator fun hasNext(): Boolean {
            return index < elements.size
        }

        public inline operator fun next(): Int {
            return elements[index++]
        }

        public companion object
    }

    public companion object {
        @PublishedApi internal const val ELEMENT_STRIDE: Long = 4
    }
}

@JvmInline public value class MemoryUIntArrayView internal constructor(public val memory: Memory) {
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

    public class Iterator internal constructor(@PublishedApi internal val elements: MemoryUIntArrayView) {
        @PublishedApi internal var index: Long = 0L

        public inline operator fun hasNext(): Boolean {
            return index < elements.size
        }

        public inline operator fun next(): UInt {
            return elements[index++]
        }

        public companion object
    }

    public companion object {
        @PublishedApi internal const val ELEMENT_STRIDE: Long = 4
    }
}

@JvmInline public value class MemoryFloatArrayView internal constructor(public val memory: Memory) {
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

    public class Iterator internal constructor(@PublishedApi internal val elements: MemoryFloatArrayView) {
        @PublishedApi internal var index: Long = 0L

        public inline operator fun hasNext(): Boolean {
            return index < elements.size
        }

        public inline operator fun next(): Float {
            return elements[index++]
        }

        public companion object
    }

    public companion object {
        @PublishedApi internal const val ELEMENT_STRIDE: Long = 4
    }
}

@JvmInline public value class MemoryLongArrayView internal constructor(public val memory: Memory) {
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

    public class Iterator internal constructor(@PublishedApi internal val elements: MemoryLongArrayView) {
        @PublishedApi internal var index: Long = 0L

        public inline operator fun hasNext(): Boolean {
            return index < elements.size
        }

        public inline operator fun next(): Long {
            return elements[index++]
        }

        public companion object
    }

    public companion object {
        @PublishedApi internal const val ELEMENT_STRIDE: Long = 8
    }
}

@JvmInline public value class MemoryULongArrayView internal constructor(public val memory: Memory) {
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

    public class Iterator internal constructor(@PublishedApi internal val elements: MemoryULongArrayView) {
        @PublishedApi internal var index: Long = 0L

        public inline operator fun hasNext(): Boolean {
            return index < elements.size
        }

        public inline operator fun next(): ULong {
            return elements[index++]
        }

        public companion object
    }

    public companion object {
        @PublishedApi internal const val ELEMENT_STRIDE: Long = 8
    }
}

@JvmInline public value class MemoryDoubleArrayView internal constructor(public val memory: Memory) {
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

    public class Iterator internal constructor(@PublishedApi internal val elements: MemoryDoubleArrayView) {
        @PublishedApi internal var index: Long = 0L

        public inline operator fun hasNext(): Boolean {
            return index < elements.size
        }

        public inline operator fun next(): Double {
            return elements[index++]
        }

        public companion object
    }

    public companion object {
        @PublishedApi internal const val ELEMENT_STRIDE: Long = 8
    }
}

@JvmInline public value class MemoryPointerArrayView internal constructor(public val memory: Memory) {
    public inline val size: Long get() {
        return memory.size / NativeAddress.BYTE_SIZE
    }

    public inline val indices: LongRange get() {
        return 0 until size
    }

    public inline operator fun get(index: Long): NativeAddress {
        return memory.getPointer(index timesExact NativeAddress.BYTE_SIZE.toLong())
    }

    public inline operator fun set(index: Long, value: NativeAddress) {
        memory.setPointer(value, index timesExact NativeAddress.BYTE_SIZE.toLong())
    }

    public operator fun iterator(): Iterator {
        return Iterator(this)
    }

    public class Iterator internal constructor(@PublishedApi internal val elements: MemoryPointerArrayView) {
        @PublishedApi internal var index: Long = 0L

        public inline operator fun hasNext(): Boolean {
            return index < elements.size
        }

        public inline operator fun next(): NativeAddress {
            return elements[index++]
        }
    }
}
