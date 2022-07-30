@file:Suppress("NOTHING_TO_INLINE")

package org.schism.foreign

import org.schism.math.requireAlignedTo
import java.lang.foreign.MemoryAddress
import java.lang.foreign.MemoryLayout
import java.lang.foreign.ValueLayout
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

public fun Long.toMemoryAddress(): MemoryAddress {
    return MemoryAddress.ofLong(this)
}

public fun MemoryAddress.isNULL(): Boolean {
    return this == MemoryAddress.NULL
}

public fun MemoryAddress.isNotNULL(): Boolean {
    return this != MemoryAddress.NULL
}

@OptIn(ExperimentalContracts::class)
public inline fun MemoryAddress.nonNULLOrElse(block: () -> MemoryAddress): MemoryAddress {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    return when {
        isNULL() -> block()
        else -> this
    }
}

public operator fun MemoryAddress.plus(offset: Int): MemoryAddress {
    return (toRawLongValue() + offset).toMemoryAddress()
}

public operator fun MemoryAddress.plus(offset: Long): MemoryAddress {
    return (toRawLongValue() + offset).toMemoryAddress()
}

public operator fun MemoryAddress.minus(other: MemoryAddress): Long {
    return toRawLongValue() - other.toRawLongValue()
}

public fun MemoryAddress.requireAlignedTo(alignment: Long) {
    toRawLongValue().requireAlignedTo(alignment)
}

public fun MemoryAddress.requireAlignedFor(layout: MemoryLayout) {
    requireAlignedTo(layout.byteAlignment())
}

public inline fun MemoryAddress.getByte(offset: Long = 0): Byte {
    return get(ValueLayout.JAVA_BYTE, offset)
}

public inline fun MemoryAddress.getUByte(offset: Long = 0): UByte {
    return getByte(offset).toUByte()
}

public inline fun MemoryAddress.getShort(offset: Long = 0): Short {
    return get(UNALIGNED_SHORT, offset)
}

public inline fun MemoryAddress.getLeShort(offset: Long = 0): Short {
    return get(UNALIGNED_LE_SHORT, offset)
}

public inline fun MemoryAddress.getBeShort(offset: Long = 0): Short {
    return get(UNALIGNED_BE_SHORT, offset)
}

public inline fun MemoryAddress.getUShort(offset: Long = 0): UShort {
    return getShort(offset).toUShort()
}

public inline fun MemoryAddress.getLeUShort(offset: Long = 0): UShort {
    return getLeShort(offset).toUShort()
}

public inline fun MemoryAddress.getBeUShort(offset: Long = 0): UShort {
    return getBeShort(offset).toUShort()
}

public inline fun MemoryAddress.getChar(offset: Long = 0): Char {
    return get(UNALIGNED_CHAR, offset)
}

public inline fun MemoryAddress.getLeChar(offset: Long = 0): Char {
    return get(UNALIGNED_LE_CHAR, offset)
}

public inline fun MemoryAddress.getBeChar(offset: Long = 0): Char {
    return get(UNALIGNED_BE_CHAR, offset)
}

public inline fun MemoryAddress.getInt(offset: Long = 0): Int {
    return get(UNALIGNED_INT, offset)
}

public inline fun MemoryAddress.getLeInt(offset: Long = 0): Int {
    return get(UNALIGNED_LE_INT, offset)
}

public inline fun MemoryAddress.getBeInt(offset: Long = 0): Int {
    return get(UNALIGNED_BE_INT, offset)
}

public inline fun MemoryAddress.getUInt(offset: Long = 0): UInt {
    return getInt(offset).toUInt()
}

public inline fun MemoryAddress.getLeUInt(offset: Long = 0): UInt {
    return getLeInt(offset).toUInt()
}

public inline fun MemoryAddress.getBeUInt(offset: Long = 0): UInt {
    return getBeInt(offset).toUInt()
}

public inline fun MemoryAddress.getLong(offset: Long = 0): Long {
    return get(UNALIGNED_LONG, offset)
}

public inline fun MemoryAddress.getLeLong(offset: Long = 0): Long {
    return get(UNALIGNED_LE_LONG, offset)
}

public inline fun MemoryAddress.getBeLong(offset: Long = 0): Long {
    return get(UNALIGNED_BE_LONG, offset)
}

public inline fun MemoryAddress.getULong(offset: Long = 0): ULong {
    return getLong(offset).toULong()
}

public inline fun MemoryAddress.getLeULong(offset: Long = 0): ULong {
    return getLeLong(offset).toULong()
}

public inline fun MemoryAddress.getBeULong(offset: Long = 0): ULong {
    return getBeLong(offset).toULong()
}

public inline fun MemoryAddress.getFloat(offset: Long = 0): Float {
    return get(UNALIGNED_FLOAT, offset)
}

public inline fun MemoryAddress.getLeFloat(offset: Long = 0): Float {
    return get(UNALIGNED_LE_FLOAT, offset)
}

public inline fun MemoryAddress.getBeFloat(offset: Long = 0): Float {
    return get(UNALIGNED_BE_FLOAT, offset)
}

public inline fun MemoryAddress.getDouble(offset: Long = 0): Double {
    return get(UNALIGNED_DOUBLE, offset)
}

public inline fun MemoryAddress.getLeDouble(offset: Long = 0): Double {
    return get(UNALIGNED_LE_DOUBLE, offset)
}

public inline fun MemoryAddress.getBeDouble(offset: Long = 0): Double {
    return get(UNALIGNED_BE_DOUBLE, offset)
}

public inline fun MemoryAddress.getPointer(offset: Long = 0): MemoryAddress {
    return get(UNALIGNED_ADDRESS, offset)
}

public inline fun MemoryAddress.setByte(offset: Long = 0, value: Byte) {
    set(ValueLayout.JAVA_BYTE, offset, value)
}

public inline fun MemoryAddress.setUByte(offset: Long = 0, value: UByte) {
    setByte(offset, value.toByte())
}

public inline fun MemoryAddress.setShort(offset: Long = 0, value: Short) {
    set(UNALIGNED_SHORT, offset, value)
}

public inline fun MemoryAddress.setLeShort(offset: Long = 0, value: Short) {
    set(UNALIGNED_LE_SHORT, offset, value)
}

public inline fun MemoryAddress.setBeShort(offset: Long = 0, value: Short) {
    set(UNALIGNED_BE_SHORT, offset, value)
}

public inline fun MemoryAddress.setUShort(offset: Long = 0, value: UShort) {
    setShort(offset, value.toShort())
}

public inline fun MemoryAddress.setLeUShort(offset: Long = 0, value: UShort) {
    setLeShort(offset, value.toShort())
}

public inline fun MemoryAddress.setBeUShort(offset: Long = 0, value: UShort) {
    setBeShort(offset, value.toShort())
}

public inline fun MemoryAddress.setChar(offset: Long = 0, value: Char) {
    set(UNALIGNED_CHAR, offset, value)
}

public inline fun MemoryAddress.setLeChar(offset: Long = 0, value: Char) {
    set(UNALIGNED_LE_CHAR, offset, value)
}

public inline fun MemoryAddress.setBeChar(offset: Long = 0, value: Char) {
    set(UNALIGNED_BE_CHAR, offset, value)
}

public inline fun MemoryAddress.setInt(offset: Long = 0, value: Int) {
    set(UNALIGNED_INT, offset, value)
}

public inline fun MemoryAddress.setLeInt(offset: Long = 0, value: Int) {
    set(UNALIGNED_LE_INT, offset, value)
}

public inline fun MemoryAddress.setBeInt(offset: Long = 0, value: Int) {
    set(UNALIGNED_BE_INT, offset, value)
}

public inline fun MemoryAddress.setUInt(offset: Long = 0, value: UInt) {
    setInt(offset, value.toInt())
}

public inline fun MemoryAddress.setLeUInt(offset: Long = 0, value: UInt) {
    setLeInt(offset, value.toInt())
}

public inline fun MemoryAddress.setBeUInt(offset: Long = 0, value: UInt) {
    setBeInt(offset, value.toInt())
}

public inline fun MemoryAddress.setLong(offset: Long = 0, value: Long) {
    set(UNALIGNED_LONG, offset, value)
}

public inline fun MemoryAddress.setLeLong(offset: Long = 0, value: Long) {
    set(UNALIGNED_LE_LONG, offset, value)
}

public inline fun MemoryAddress.setBeLong(offset: Long = 0, value: Long) {
    set(UNALIGNED_BE_LONG, offset, value)
}

public inline fun MemoryAddress.setULong(offset: Long = 0, value: ULong) {
    setLong(offset, value.toLong())
}

public inline fun MemoryAddress.setLeULong(offset: Long = 0, value: ULong) {
    setLeLong(offset, value.toLong())
}

public inline fun MemoryAddress.setBeULong(offset: Long = 0, value: ULong) {
    setBeLong(offset, value.toLong())
}

public inline fun MemoryAddress.setFloat(offset: Long = 0, value: Float) {
    set(UNALIGNED_FLOAT, offset, value)
}

public inline fun MemoryAddress.setLeFloat(offset: Long = 0, value: Float) {
    set(UNALIGNED_LE_FLOAT, offset, value)
}

public inline fun MemoryAddress.setBeFloat(offset: Long = 0, value: Float) {
    set(UNALIGNED_BE_FLOAT, offset, value)
}

public inline fun MemoryAddress.setDouble(offset: Long = 0, value: Double) {
    set(UNALIGNED_DOUBLE, offset, value)
}

public inline fun MemoryAddress.setLeDouble(offset: Long = 0, value: Double) {
    set(UNALIGNED_LE_DOUBLE, offset, value)
}

public inline fun MemoryAddress.setBeDouble(offset: Long = 0, value: Double) {
    set(UNALIGNED_BE_DOUBLE, offset, value)
}

public inline fun MemoryAddress.setPointer(offset: Long = 0, value: MemoryAddress) {
    set(UNALIGNED_ADDRESS, offset, value)
}
