package org.schism.memory

import java.lang.invoke.MethodHandles
import java.nio.ByteOrder

public fun ByteArray.getChar(offset: Int = 0): Char {
    return NATIVE_CHAR.get(this, offset) as Char
}

public fun ByteArray.getLeChar(offset: Int = 0): Char {
    return LE_CHAR.get(this, offset) as Char
}

public fun ByteArray.getBeChar(offset: Int = 0): Char {
    return BE_CHAR.get(this, offset) as Char
}

public fun ByteArray.getShort(offset: Int = 0): Short {
    return NATIVE_SHORT.get(this, offset) as Short
}

public fun ByteArray.getLeShort(offset: Int = 0): Short {
    return LE_SHORT.get(this, offset) as Short
}

public fun ByteArray.getBeShort(offset: Int = 0): Short {
    return BE_SHORT.get(this, offset) as Short
}

public fun ByteArray.getInt(offset: Int = 0): Int {
    return NATIVE_INT.get(this, offset) as Int
}

public fun ByteArray.getLeInt(offset: Int = 0): Int {
    return LE_INT.get(this, offset) as Int
}

public fun ByteArray.getBeInt(offset: Int = 0): Int {
    return BE_INT.get(this, offset) as Int
}

public fun ByteArray.getLong(offset: Int = 0): Long {
    return NATIVE_LONG.get(this, offset) as Long
}

public fun ByteArray.getLeLong(offset: Int = 0): Long {
    return LE_LONG.get(this, offset) as Long
}

public fun ByteArray.getBeLong(offset: Int = 0): Long {
    return BE_LONG.get(this, offset) as Long
}

public fun ByteArray.getFloat(offset: Int = 0): Float {
    return NATIVE_FLOAT.get(this, offset) as Float
}

public fun ByteArray.getLeFloat(offset: Int = 0): Float {
    return LE_FLOAT.get(this, offset) as Float
}

public fun ByteArray.getBeFloat(offset: Int = 0): Float {
    return BE_FLOAT.get(this, offset) as Float
}

public fun ByteArray.getDouble(offset: Int = 0): Double {
    return NATIVE_DOUBLE.get(this, offset) as Double
}

public fun ByteArray.getLeDouble(offset: Int = 0): Double {
    return LE_DOUBLE.get(this, offset) as Double
}

public fun ByteArray.getBeDouble(offset: Int = 0): Double {
    return BE_DOUBLE.get(this, offset) as Double
}

public fun ByteArray.setChar(value: Char, offset: Int = 0) {
    NATIVE_CHAR.set(this, offset, value)
}

public fun ByteArray.setLeChar(value: Char, offset: Int = 0) {
    LE_CHAR.set(this, offset, value)
}

public fun ByteArray.setBeChar(value: Char, offset: Int = 0) {
    BE_CHAR.set(this, offset, value)
}

public fun ByteArray.setShort(value: Short, offset: Int = 0) {
    NATIVE_SHORT.set(this, offset, value)
}

public fun ByteArray.setLeShort(value: Short, offset: Int = 0) {
    LE_SHORT.set(this, offset, value)
}

public fun ByteArray.setBeShort(value: Short, offset: Int = 0) {
    BE_SHORT.set(this, offset, value)
}

public fun ByteArray.setInt(value: Int, offset: Int = 0) {
    NATIVE_INT.set(this, offset, value)
}

public fun ByteArray.setLeInt(value: Int, offset: Int = 0) {
    LE_INT.set(this, offset, value)
}

public fun ByteArray.setBeInt(value: Int, offset: Int = 0) {
    BE_INT.set(this, offset, value)
}

public fun ByteArray.setLong(value: Long, offset: Int = 0) {
    NATIVE_LONG.set(this, offset, value)
}

public fun ByteArray.setLeLong(value: Long, offset: Int = 0) {
    LE_LONG.set(this, offset, value)
}

public fun ByteArray.setBeLong(value: Long, offset: Int = 0) {
    BE_LONG.set(this, offset, value)
}

public fun ByteArray.setFloat(value: Float, offset: Int = 0) {
    NATIVE_FLOAT.set(this, offset, value)
}

public fun ByteArray.setLeFloat(value: Float, offset: Int = 0) {
    LE_FLOAT.set(this, offset, value)
}

public fun ByteArray.setBeFloat(value: Float, offset: Int = 0) {
    BE_FLOAT.set(this, offset, value)
}

public fun ByteArray.setDouble(value: Double, offset: Int = 0) {
    NATIVE_DOUBLE.set(this, offset, value)
}

public fun ByteArray.setLeDouble(value: Double, offset: Int = 0) {
    LE_DOUBLE.set(this, offset, value)
}

public fun ByteArray.setBeDouble(value: Double, offset: Int = 0) {
    BE_DOUBLE.set(this, offset, value)
}

public fun ByteArray.getUByte(offset: Int = 0): UByte {
    return get(offset).toUByte()
}

public fun ByteArray.getUShort(offset: Int = 0): UShort {
    return getShort(offset).toUShort()
}

public fun ByteArray.getLeUShort(offset: Int = 0): UShort {
    return getLeShort(offset).toUShort()
}

public fun ByteArray.getBeUShort(offset: Int = 0): UShort {
    return getBeShort(offset).toUShort()
}

public fun ByteArray.getUInt(offset: Int = 0): UInt {
    return getInt(offset).toUInt()
}

public fun ByteArray.getLeUInt(offset: Int = 0): UInt {
    return getLeInt(offset).toUInt()
}

public fun ByteArray.getBeUInt(offset: Int = 0): UInt {
    return getBeInt(offset).toUInt()
}

public fun ByteArray.getULong(offset: Int = 0): ULong {
    return getLong(offset).toULong()
}

public fun ByteArray.getLeULong(offset: Int = 0): ULong {
    return getLeLong(offset).toULong()
}

public fun ByteArray.getBeULong(offset: Int = 0): ULong {
    return getBeLong(offset).toULong()
}

public fun ByteArray.setUByte(value: UByte, offset: Int = 0) {
    set(offset, value.toByte())
}

public fun ByteArray.setUShort(value: UShort, offset: Int = 0) {
    setShort(value.toShort(), offset)
}

public fun ByteArray.setLeUShort(value: UShort, offset: Int = 0) {
    setLeShort(value.toShort(), offset)
}

public fun ByteArray.setBeUShort(value: UShort, offset: Int = 0) {
    setBeShort(value.toShort(), offset)
}

public fun ByteArray.setUInt(value: UInt, offset: Int = 0) {
    setInt(value.toInt(), offset)
}

public fun ByteArray.setLeUInt(value: UInt, offset: Int = 0) {
    setLeInt(value.toInt(), offset)
}

public fun ByteArray.setBeUInt(value: UInt, offset: Int = 0) {
    setBeInt(value.toInt(), offset)
}

public fun ByteArray.setULong(value: ULong, offset: Int = 0) {
    setLong(value.toLong(), offset)
}

public fun ByteArray.setLeULong(value: ULong, offset: Int = 0) {
    setLeLong(value.toLong(), offset)
}

public fun ByteArray.setBeULong(value: ULong, offset: Int = 0) {
    setBeLong(value.toLong(), offset)
}

public fun memcpy(
    dst: ByteArray,
    dstOffset: Int = 0,
    src: ByteArray,
    srcOffset: Int = 0,
    size: Int = minOf(dst.size - dstOffset, src.size - srcOffset),
) {
    src.copyInto(dst, destinationOffset = dstOffset, startIndex = srcOffset, endIndex = srcOffset + size)
}

public fun memset(dst: ByteArray, dstOffset: Int = 0, value: Byte, size: Int = dst.size - dstOffset) {
    dst.fill(value, fromIndex = dstOffset, toIndex = dstOffset + size)
}

public fun memset(dst: ByteArray, dstOffset: Int = 0, value: UByte, size: Int = dst.size - dstOffset) {
    memset(dst, dstOffset, value.toByte(), size)
}

private val NATIVE_CHAR = MethodHandles.byteArrayViewVarHandle(Char::class.java, ByteOrder.nativeOrder())
private val LE_CHAR = MethodHandles.byteArrayViewVarHandle(Char::class.java, ByteOrder.LITTLE_ENDIAN)
private val BE_CHAR = MethodHandles.byteArrayViewVarHandle(Char::class.java, ByteOrder.BIG_ENDIAN)

private val NATIVE_SHORT = MethodHandles.byteArrayViewVarHandle(Short::class.java, ByteOrder.nativeOrder())
private val LE_SHORT = MethodHandles.byteArrayViewVarHandle(Short::class.java, ByteOrder.LITTLE_ENDIAN)
private val BE_SHORT = MethodHandles.byteArrayViewVarHandle(Short::class.java, ByteOrder.BIG_ENDIAN)

private val NATIVE_INT = MethodHandles.byteArrayViewVarHandle(Int::class.java, ByteOrder.nativeOrder())
private val LE_INT = MethodHandles.byteArrayViewVarHandle(Int::class.java, ByteOrder.LITTLE_ENDIAN)
private val BE_INT = MethodHandles.byteArrayViewVarHandle(Int::class.java, ByteOrder.BIG_ENDIAN)

private val NATIVE_LONG = MethodHandles.byteArrayViewVarHandle(Long::class.java, ByteOrder.nativeOrder())
private val LE_LONG = MethodHandles.byteArrayViewVarHandle(Long::class.java, ByteOrder.LITTLE_ENDIAN)
private val BE_LONG = MethodHandles.byteArrayViewVarHandle(Long::class.java, ByteOrder.BIG_ENDIAN)

private val NATIVE_FLOAT = MethodHandles.byteArrayViewVarHandle(Float::class.java, ByteOrder.nativeOrder())
private val LE_FLOAT = MethodHandles.byteArrayViewVarHandle(Float::class.java, ByteOrder.LITTLE_ENDIAN)
private val BE_FLOAT = MethodHandles.byteArrayViewVarHandle(Float::class.java, ByteOrder.BIG_ENDIAN)

private val NATIVE_DOUBLE = MethodHandles.byteArrayViewVarHandle(Double::class.java, ByteOrder.nativeOrder())
private val LE_DOUBLE = MethodHandles.byteArrayViewVarHandle(Double::class.java, ByteOrder.LITTLE_ENDIAN)
private val BE_DOUBLE = MethodHandles.byteArrayViewVarHandle(Double::class.java, ByteOrder.BIG_ENDIAN)
