@file:Suppress("NOTHING_TO_INLINE")

package org.schism.foreign

import java.lang.foreign.MemoryAddress
import java.lang.foreign.ValueLayout
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

public fun Long.toMemoryAddress(): MemoryAddress {
    return MemoryAddress.ofLong(this)
}

public fun MemoryAddress.isNULL(): Boolean {
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

public inline fun MemoryAddress.getByte(offset: Long): Byte {
    return get(ValueLayout.JAVA_BYTE, offset)
}

public inline fun MemoryAddress.getUByte(offset: Long): UByte {
    return getByte(offset).toUByte()
}

public inline fun MemoryAddress.getShort(offset: Long): Short {
    return get(SHORT_LAYOUT, offset)
}

public inline fun MemoryAddress.getLeShort(offset: Long): Short {
    return get(LE_SHORT_LAYOUT, offset)
}

public inline fun MemoryAddress.getBeShort(offset: Long): Short {
    return get(BE_SHORT_LAYOUT, offset)
}

public inline fun MemoryAddress.getUShort(offset: Long): UShort {
    return getShort(offset).toUShort()
}

public inline fun MemoryAddress.getLeUShort(offset: Long): UShort {
    return getLeShort(offset).toUShort()
}

public inline fun MemoryAddress.getBeUShort(offset: Long): UShort {
    return getBeShort(offset).toUShort()
}

public inline fun MemoryAddress.getChar(offset: Long): Char {
    return get(CHAR_LAYOUT, offset)
}

public inline fun MemoryAddress.getLeChar(offset: Long): Char {
    return get(LE_CHAR_LAYOUT, offset)
}

public inline fun MemoryAddress.getBeChar(offset: Long): Char {
    return get(BE_CHAR_LAYOUT, offset)
}

public inline fun MemoryAddress.getInt(offset: Long): Int {
    return get(INT_LAYOUT, offset)
}

public inline fun MemoryAddress.getLeInt(offset: Long): Int {
    return get(LE_INT_LAYOUT, offset)
}

public inline fun MemoryAddress.getBeInt(offset: Long): Int {
    return get(BE_INT_LAYOUT, offset)
}

public inline fun MemoryAddress.getUInt(offset: Long): UInt {
    return getInt(offset).toUInt()
}

public inline fun MemoryAddress.getLeUInt(offset: Long): UInt {
    return getLeInt(offset).toUInt()
}

public inline fun MemoryAddress.getBeUInt(offset: Long): UInt {
    return getBeInt(offset).toUInt()
}

public inline fun MemoryAddress.getLong(offset: Long): Long {
    return get(LONG_LAYOUT, offset)
}

public inline fun MemoryAddress.getLeLong(offset: Long): Long {
    return get(LE_LONG_LAYOUT, offset)
}

public inline fun MemoryAddress.getBeLong(offset: Long): Long {
    return get(BE_LONG_LAYOUT, offset)
}

public inline fun MemoryAddress.getULong(offset: Long): ULong {
    return getLong(offset).toULong()
}

public inline fun MemoryAddress.getLeULong(offset: Long): ULong {
    return getLeLong(offset).toULong()
}

public inline fun MemoryAddress.getBeULong(offset: Long): ULong {
    return getBeLong(offset).toULong()
}

public inline fun MemoryAddress.getFloat(offset: Long): Float {
    return get(FLOAT_LAYOUT, offset)
}

public inline fun MemoryAddress.getLeFloat(offset: Long): Float {
    return get(LE_FLOAT_LAYOUT, offset)
}

public inline fun MemoryAddress.getBeFloat(offset: Long): Float {
    return get(BE_FLOAT_LAYOUT, offset)
}

public inline fun MemoryAddress.getDouble(offset: Long): Double {
    return get(DOUBLE_LAYOUT, offset)
}

public inline fun MemoryAddress.getLeDouble(offset: Long): Double {
    return get(LE_DOUBLE_LAYOUT, offset)
}

public inline fun MemoryAddress.getBeDouble(offset: Long): Double {
    return get(BE_DOUBLE_LAYOUT, offset)
}

public inline fun MemoryAddress.getAddress(offset: Long): MemoryAddress {
    return get(ADDRESS_LAYOUT, offset)
}

public inline fun MemoryAddress.setByte(offset: Long, value: Byte) {
    set(ValueLayout.JAVA_BYTE, offset, value)
}

public inline fun MemoryAddress.setUByte(offset: Long, value: UByte) {
    setByte(offset, value.toByte())
}

public inline fun MemoryAddress.setShort(offset: Long, value: Short) {
    set(SHORT_LAYOUT, offset, value)
}

public inline fun MemoryAddress.setLeShort(offset: Long, value: Short) {
    set(LE_SHORT_LAYOUT, offset, value)
}

public inline fun MemoryAddress.setBeShort(offset: Long, value: Short) {
    set(BE_SHORT_LAYOUT, offset, value)
}

public inline fun MemoryAddress.setUShort(offset: Long, value: UShort) {
    setShort(offset, value.toShort())
}

public inline fun MemoryAddress.setLeUShort(offset: Long, value: UShort) {
    setLeShort(offset, value.toShort())
}

public inline fun MemoryAddress.setBeUShort(offset: Long, value: UShort) {
    setBeShort(offset, value.toShort())
}

public inline fun MemoryAddress.setChar(offset: Long, value: Char) {
    set(CHAR_LAYOUT, offset, value)
}

public inline fun MemoryAddress.setLeChar(offset: Long, value: Char) {
    set(LE_CHAR_LAYOUT, offset, value)
}

public inline fun MemoryAddress.setBeChar(offset: Long, value: Char) {
    set(BE_CHAR_LAYOUT, offset, value)
}

public inline fun MemoryAddress.setInt(offset: Long, value: Int) {
    set(INT_LAYOUT, offset, value)
}

public inline fun MemoryAddress.setLeInt(offset: Long, value: Int) {
    set(LE_INT_LAYOUT, offset, value)
}

public inline fun MemoryAddress.setBeInt(offset: Long, value: Int) {
    set(BE_INT_LAYOUT, offset, value)
}

public inline fun MemoryAddress.setUInt(offset: Long, value: UInt) {
    setInt(offset, value.toInt())
}

public inline fun MemoryAddress.setLeUInt(offset: Long, value: UInt) {
    setLeInt(offset, value.toInt())
}

public inline fun MemoryAddress.setBeUInt(offset: Long, value: UInt) {
    setBeInt(offset, value.toInt())
}

public inline fun MemoryAddress.setLong(offset: Long, value: Long) {
    set(LONG_LAYOUT, offset, value)
}

public inline fun MemoryAddress.setLeLong(offset: Long, value: Long) {
    set(LE_LONG_LAYOUT, offset, value)
}

public inline fun MemoryAddress.setBeLong(offset: Long, value: Long) {
    set(BE_LONG_LAYOUT, offset, value)
}

public inline fun MemoryAddress.setULong(offset: Long, value: ULong) {
    setLong(offset, value.toLong())
}

public inline fun MemoryAddress.setLeULong(offset: Long, value: ULong) {
    setLeLong(offset, value.toLong())
}

public inline fun MemoryAddress.setBeULong(offset: Long, value: ULong) {
    setBeLong(offset, value.toLong())
}

public inline fun MemoryAddress.setFloat(offset: Long, value: Float) {
    set(FLOAT_LAYOUT, offset, value)
}

public inline fun MemoryAddress.setLeFloat(offset: Long, value: Float) {
    set(LE_FLOAT_LAYOUT, offset, value)
}

public inline fun MemoryAddress.setBeFloat(offset: Long, value: Float) {
    set(BE_FLOAT_LAYOUT, offset, value)
}

public inline fun MemoryAddress.setDouble(offset: Long, value: Double) {
    set(DOUBLE_LAYOUT, offset, value)
}

public inline fun MemoryAddress.setLeDouble(offset: Long, value: Double) {
    set(LE_DOUBLE_LAYOUT, offset, value)
}

public inline fun MemoryAddress.setBeDouble(offset: Long, value: Double) {
    set(BE_DOUBLE_LAYOUT, offset, value)
}

public inline fun MemoryAddress.setAddress(offset: Long, value: MemoryAddress) {
    set(ADDRESS_LAYOUT, offset, value)
}
