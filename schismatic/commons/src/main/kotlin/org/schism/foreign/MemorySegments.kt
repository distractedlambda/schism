@file:Suppress("NOTHING_TO_INLINE")

package org.schism.foreign

import java.lang.foreign.MemoryAddress
import java.lang.foreign.MemoryLayout
import java.lang.foreign.MemorySegment
import java.lang.foreign.MemorySession
import java.lang.foreign.ValueLayout.JAVA_BYTE

public fun MemoryAddress.asMemorySegment(
    bytesSize: Long,
    session: MemorySession = globalMemorySession(),
): MemorySegment {
    return MemorySegment.ofAddress(this, bytesSize, session)
}

public fun MemorySegment.drop(count: Long): MemorySegment {
    return asSlice(count)
}

public fun MemorySegment.dropLast(count: Long): MemorySegment {
    return asSlice(0, byteSize() - count)
}

public fun MemorySegment.take(count: Long): MemorySegment {
    return asSlice(0, count)
}

public fun MemorySegment.takeLast(count: Long): MemorySegment {
    return asSlice(byteSize() - count, count)
}

public fun allocateHeapSegment(size: Long): MemorySegment {
    require(size in 0 .. Int.MAX_VALUE)
    return ByteArray(size.toInt()).asMemorySegment()
}

public fun emptyHeapSegment(): MemorySegment {
    return EMPTY_HEAP_SEGMENT
}

private val EMPTY_HEAP_SEGMENT = allocateHeapSegment(0)

public fun heapCopyOf(segment: MemorySegment): MemorySegment {
    return allocateHeapSegment(segment.byteSize()).apply {
        copyFrom(segment)
    }
}

public fun ByteArray.asMemorySegment(): MemorySegment {
    return MemorySegment.ofArray(this)
}

@ExperimentalUnsignedTypes
public fun UByteArray.asMemorySegment(): MemorySegment {
    return asByteArray().asMemorySegment()
}

public fun ShortArray.asMemorySegment(): MemorySegment {
    return MemorySegment.ofArray(this)
}

@ExperimentalUnsignedTypes
public fun UShortArray.asMemorySegment(): MemorySegment {
    return asShortArray().asMemorySegment()
}

public fun CharArray.asMemorySegment(): MemorySegment {
    return MemorySegment.ofArray(this)
}

public fun IntArray.asMemorySegment(): MemorySegment {
    return MemorySegment.ofArray(this)
}

@ExperimentalUnsignedTypes
public fun UIntArray.asMemorySegment(): MemorySegment {
    return asIntArray().asMemorySegment()
}

public fun LongArray.asMemorySegment(): MemorySegment {
    return MemorySegment.ofArray(this)
}

@ExperimentalUnsignedTypes
public fun ULongArray.asMemorySegment(): MemorySegment {
    return asLongArray().asMemorySegment()
}

public fun FloatArray.asMemorySegment(): MemorySegment {
    return MemorySegment.ofArray(this)
}

public fun DoubleArray.asMemorySegment(): MemorySegment {
    return MemorySegment.ofArray(this)
}

public fun MemorySegment.requireAlignedTo(alignment: Long) {
    if (isNative) {
        address().requireAlignedTo(alignment)
    }
}

public fun MemorySegment.requireAlignedFor(layout: MemoryLayout) {
    requireAlignedTo(layout.byteAlignment())
}

public fun MemorySegment.requireSizedAndAlignedFor(layout: MemoryLayout) {
    require(byteSize() == layout.byteSize())
    requireAlignedFor(layout)
}

public inline fun MemorySegment.getByte(offset: Long = 0): Byte {
    return get(JAVA_BYTE, offset)
}

public inline fun MemorySegment.getUByte(offset: Long = 0): UByte {
    return getByte(offset).toUByte()
}

public inline fun MemorySegment.getShort(offset: Long = 0): Short {
    return get(UNALIGNED_SHORT, offset)
}

public inline fun MemorySegment.getLeShort(offset: Long = 0): Short {
    return get(UNALIGNED_LE_SHORT, offset)
}

public inline fun MemorySegment.getBeShort(offset: Long = 0): Short {
    return get(UNALIGNED_BE_SHORT, offset)
}

public inline fun MemorySegment.getUShort(offset: Long = 0): UShort {
    return getShort(offset).toUShort()
}

public inline fun MemorySegment.getLeUShort(offset: Long = 0): UShort {
    return getLeShort(offset).toUShort()
}

public inline fun MemorySegment.getBeUShort(offset: Long = 0): UShort {
    return getBeShort(offset).toUShort()
}

public inline fun MemorySegment.getChar(offset: Long = 0): Char {
    return get(UNALIGNED_CHAR, offset)
}

public inline fun MemorySegment.getLeChar(offset: Long = 0): Char {
    return get(UNALIGNED_LE_CHAR, offset)
}

public inline fun MemorySegment.getBeChar(offset: Long = 0): Char {
    return get(UNALIGNED_BE_CHAR, offset)
}

public inline fun MemorySegment.getInt(offset: Long = 0): Int {
    return get(UNALIGNED_INT, offset)
}

public inline fun MemorySegment.getLeInt(offset: Long = 0): Int {
    return get(UNALIGNED_LE_INT, offset)
}

public inline fun MemorySegment.getBeInt(offset: Long = 0): Int {
    return get(UNALIGNED_BE_INT, offset)
}

public inline fun MemorySegment.getUInt(offset: Long = 0): UInt {
    return getInt(offset).toUInt()
}

public inline fun MemorySegment.getLeUInt(offset: Long = 0): UInt {
    return getLeInt(offset).toUInt()
}

public inline fun MemorySegment.getBeUInt(offset: Long = 0): UInt {
    return getBeInt(offset).toUInt()
}

public inline fun MemorySegment.getLong(offset: Long = 0): Long {
    return get(UNALIGNED_LONG, offset)
}

public inline fun MemorySegment.getLeLong(offset: Long = 0): Long {
    return get(UNALIGNED_LE_LONG, offset)
}

public inline fun MemorySegment.getBeLong(offset: Long = 0): Long {
    return get(UNALIGNED_BE_LONG, offset)
}

public inline fun MemorySegment.getULong(offset: Long = 0): ULong {
    return getLong(offset).toULong()
}

public inline fun MemorySegment.getLeULong(offset: Long = 0): ULong {
    return getLeLong(offset).toULong()
}

public inline fun MemorySegment.getBeULong(offset: Long = 0): ULong {
    return getBeLong(offset).toULong()
}

public inline fun MemorySegment.getFloat(offset: Long = 0): Float {
    return get(UNALIGNED_FLOAT, offset)
}

public inline fun MemorySegment.getLeFloat(offset: Long = 0): Float {
    return get(UNALIGNED_LE_FLOAT, offset)
}

public inline fun MemorySegment.getBeFloat(offset: Long = 0): Float {
    return get(UNALIGNED_BE_FLOAT, offset)
}

public inline fun MemorySegment.getDouble(offset: Long = 0): Double {
    return get(UNALIGNED_DOUBLE, offset)
}

public inline fun MemorySegment.getLeDouble(offset: Long = 0): Double {
    return get(UNALIGNED_LE_DOUBLE, offset)
}

public inline fun MemorySegment.getBeDouble(offset: Long = 0): Double {
    return get(UNALIGNED_BE_DOUBLE, offset)
}

public inline fun MemorySegment.getPointer(offset: Long = 0): MemoryAddress {
    return get(UNALIGNED_ADDRESS, offset)
}

public inline fun MemorySegment.setByte(offset: Long = 0, value: Byte) {
    set(JAVA_BYTE, offset, value)
}

public inline fun MemorySegment.setUByte(offset: Long = 0, value: UByte) {
    setByte(offset, value.toByte())
}

public inline fun MemorySegment.setShort(offset: Long = 0, value: Short) {
    set(UNALIGNED_SHORT, offset, value)
}

public inline fun MemorySegment.setLeShort(offset: Long = 0, value: Short) {
    set(UNALIGNED_LE_SHORT, offset, value)
}

public inline fun MemorySegment.setBeShort(offset: Long = 0, value: Short) {
    set(UNALIGNED_BE_SHORT, offset, value)
}

public inline fun MemorySegment.setUShort(offset: Long = 0, value: UShort) {
    setShort(offset, value.toShort())
}

public inline fun MemorySegment.setLeUShort(offset: Long = 0, value: UShort) {
    setLeShort(offset, value.toShort())
}

public inline fun MemorySegment.setBeUShort(offset: Long = 0, value: UShort) {
    setBeShort(offset, value.toShort())
}

public inline fun MemorySegment.setChar(offset: Long = 0, value: Char) {
    set(UNALIGNED_CHAR, offset, value)
}

public inline fun MemorySegment.setLeChar(offset: Long = 0, value: Char) {
    set(UNALIGNED_LE_CHAR, offset, value)
}

public inline fun MemorySegment.setBeChar(offset: Long = 0, value: Char) {
    set(UNALIGNED_BE_CHAR, offset, value)
}

public inline fun MemorySegment.setInt(offset: Long = 0, value: Int) {
    set(UNALIGNED_INT, offset, value)
}

public inline fun MemorySegment.setLeInt(offset: Long = 0, value: Int) {
    set(UNALIGNED_LE_INT, offset, value)
}

public inline fun MemorySegment.setBeInt(offset: Long = 0, value: Int) {
    set(UNALIGNED_BE_INT, offset, value)
}

public inline fun MemorySegment.setUInt(offset: Long = 0, value: UInt) {
    setInt(offset, value.toInt())
}

public inline fun MemorySegment.setLeUInt(offset: Long = 0, value: UInt) {
    setLeInt(offset, value.toInt())
}

public inline fun MemorySegment.setBeUInt(offset: Long = 0, value: UInt) {
    setBeInt(offset, value.toInt())
}

public inline fun MemorySegment.setLong(offset: Long = 0, value: Long) {
    set(UNALIGNED_LONG, offset, value)
}

public inline fun MemorySegment.setLeLong(offset: Long = 0, value: Long) {
    set(UNALIGNED_LE_LONG, offset, value)
}

public inline fun MemorySegment.setBeLong(offset: Long = 0, value: Long) {
    set(UNALIGNED_BE_LONG, offset, value)
}

public inline fun MemorySegment.setULong(offset: Long = 0, value: ULong) {
    setLong(offset, value.toLong())
}

public inline fun MemorySegment.setLeULong(offset: Long = 0, value: ULong) {
    setLeLong(offset, value.toLong())
}

public inline fun MemorySegment.setBeULong(offset: Long = 0, value: ULong) {
    setBeLong(offset, value.toLong())
}

public inline fun MemorySegment.setFloat(offset: Long = 0, value: Float) {
    set(UNALIGNED_FLOAT, offset, value)
}

public inline fun MemorySegment.setLeFloat(offset: Long = 0, value: Float) {
    set(UNALIGNED_LE_FLOAT, offset, value)
}

public inline fun MemorySegment.setBeFloat(offset: Long = 0, value: Float) {
    set(UNALIGNED_BE_FLOAT, offset, value)
}

public inline fun MemorySegment.setDouble(offset: Long = 0, value: Double) {
    set(UNALIGNED_DOUBLE, offset, value)
}

public inline fun MemorySegment.setLeDouble(offset: Long = 0, value: Double) {
    set(UNALIGNED_LE_DOUBLE, offset, value)
}

public inline fun MemorySegment.setBeDouble(offset: Long = 0, value: Double) {
    set(UNALIGNED_BE_DOUBLE, offset, value)
}

public inline fun MemorySegment.setPointer(offset: Long = 0, value: MemoryAddress) {
    set(UNALIGNED_ADDRESS, offset, value)
}
