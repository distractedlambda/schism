package org.schism.memory

import java.lang.invoke.MethodHandles
import java.nio.ByteOrder

public fun ByteArray.readByte(offset: Int = 0): Byte {
    return get(offset)
}

public fun ByteArray.readNativeChar(offset: Int = 0): Char {
    return NATIVE_CHAR.get(this, offset) as Char
}

public fun ByteArray.readLeChar(offset: Int = 0): Char {
    return LE_CHAR.get(this, offset) as Char
}

public fun ByteArray.readBeChar(offset: Int = 0): Char {
    return BE_CHAR.get(this, offset) as Char
}

public fun ByteArray.readNativeShort(offset: Int = 0): Short {
    return NATIVE_SHORT.get(this, offset) as Short
}

public fun ByteArray.readLeShort(offset: Int = 0): Short {
    return LE_SHORT.get(this, offset) as Short
}

public fun ByteArray.readBeShort(offset: Int = 0): Short {
    return BE_SHORT.get(this, offset) as Short
}

public fun ByteArray.readNativeInt(offset: Int = 0): Int {
    return NATIVE_INT.get(this, offset) as Int
}

public fun ByteArray.readLeInt(offset: Int = 0): Int {
    return LE_INT.get(this, offset) as Int
}

public fun ByteArray.readBeInt(offset: Int = 0): Int {
    return BE_INT.get(this, offset) as Int
}

public fun ByteArray.readNativeLong(offset: Int = 0): Long {
    return NATIVE_LONG.get(this, offset) as Long
}

public fun ByteArray.readLeLong(offset: Int = 0): Long {
    return LE_LONG.get(this, offset) as Long
}

public fun ByteArray.readBeLong(offset: Int = 0): Long {
    return BE_LONG.get(this, offset) as Long
}

public fun ByteArray.readNativeFloat(offset: Int = 0): Float {
    return NATIVE_FLOAT.get(this, offset) as Float
}

public fun ByteArray.readLeFloat(offset: Int = 0): Float {
    return LE_FLOAT.get(this, offset) as Float
}

public fun ByteArray.readBeFloat(offset: Int = 0): Float {
    return BE_FLOAT.get(this, offset) as Float
}

public fun ByteArray.readNativeDouble(offset: Int = 0): Double {
    return NATIVE_DOUBLE.get(this, offset) as Double
}

public fun ByteArray.readLeDouble(offset: Int = 0): Double {
    return LE_DOUBLE.get(this, offset) as Double
}

public fun ByteArray.readBeDouble(offset: Int = 0): Double {
    return BE_DOUBLE.get(this, offset) as Double
}

public fun ByteArray.writeByte(value: Byte, offset: Int = 0) {
    set(offset, value)
}

public fun ByteArray.writeNativeChar(value: Char, offset: Int = 0) {
    NATIVE_CHAR.set(this, offset, value)
}

public fun ByteArray.writeLeChar(value: Char, offset: Int = 0) {
    LE_CHAR.set(this, offset, value)
}

public fun ByteArray.writeBeChar(value: Char, offset: Int = 0) {
    BE_CHAR.set(this, offset, value)
}

public fun ByteArray.writeNativeShort(value: Short, offset: Int = 0) {
    NATIVE_SHORT.set(this, offset, value)
}

public fun ByteArray.writeLeShort(value: Short, offset: Int = 0) {
    LE_SHORT.set(this, offset, value)
}

public fun ByteArray.writeBeShort(value: Short, offset: Int = 0) {
    BE_SHORT.set(this, offset, value)
}

public fun ByteArray.writeNativeInt(value: Int, offset: Int = 0) {
    NATIVE_INT.set(this, offset, value)
}

public fun ByteArray.writeLeInt(value: Int, offset: Int = 0) {
    LE_INT.set(this, offset, value)
}

public fun ByteArray.writeBeInt(value: Int, offset: Int = 0) {
    BE_INT.set(this, offset, value)
}

public fun ByteArray.writeNativeLong(value: Long, offset: Int = 0) {
    NATIVE_LONG.set(this, offset, value)
}

public fun ByteArray.writeLeLong(value: Long, offset: Int = 0) {
    LE_LONG.set(this, offset, value)
}

public fun ByteArray.writeBeLong(value: Long, offset: Int = 0) {
    BE_LONG.set(this, offset, value)
}

public fun ByteArray.writeNativeFloat(value: Float, offset: Int = 0) {
    NATIVE_FLOAT.set(this, offset, value)
}

public fun ByteArray.writeLeFloat(value: Float, offset: Int = 0) {
    LE_FLOAT.set(this, offset, value)
}

public fun ByteArray.writeBeFloat(value: Float, offset: Int = 0) {
    BE_FLOAT.set(this, offset, value)
}

public fun ByteArray.writeNativeDouble(value: Double, offset: Int = 0) {
    NATIVE_DOUBLE.set(this, offset, value)
}

public fun ByteArray.writeLeDouble(value: Double, offset: Int = 0) {
    LE_DOUBLE.set(this, offset, value)
}

public fun ByteArray.writeBeDouble(value: Double, offset: Int = 0) {
    BE_DOUBLE.set(this, offset, value)
}

public fun ByteArray.readNativeUShort(offset: Int = 0): UShort {
    return readNativeShort(offset).toUShort()
}

public fun ByteArray.readLeUShort(offset: Int = 0): UShort {
    return readLeShort(offset).toUShort()
}

public fun ByteArray.readBeUShort(offset: Int = 0): UShort {
    return readBeShort(offset).toUShort()
}

public fun ByteArray.readNativeUInt(offset: Int = 0): UInt {
    return readNativeInt(offset).toUInt()
}

public fun ByteArray.readLeUInt(offset: Int = 0): UInt {
    return readLeInt(offset).toUInt()
}

public fun ByteArray.readBeUInt(offset: Int = 0): UInt {
    return readBeInt(offset).toUInt()
}

public fun ByteArray.readNativeULong(offset: Int = 0): ULong {
    return readNativeLong(offset).toULong()
}

public fun ByteArray.readLeULong(offset: Int = 0): ULong {
    return readLeLong(offset).toULong()
}

public fun ByteArray.readBeULong(offset: Int = 0): ULong {
    return readBeLong(offset).toULong()
}

public fun ByteArray.writeUByte(value: UByte, offset: Int = 0) {
    writeByte(value.toByte(), offset)
}

public fun ByteArray.writeNativeUShort(value: UShort, offset: Int = 0) {
    writeNativeShort(value.toShort(), offset)
}

public fun ByteArray.writeLeUShort(value: UShort, offset: Int = 0) {
    writeLeShort(value.toShort(), offset)
}

public fun ByteArray.writeBeUShort(value: UShort, offset: Int = 0) {
    writeBeShort(value.toShort(), offset)
}

public fun ByteArray.writeNativeUInt(value: UInt, offset: Int = 0) {
    writeNativeInt(value.toInt(), offset)
}

public fun ByteArray.writeLeUInt(value: UInt, offset: Int = 0) {
    writeLeInt(value.toInt(), offset)
}

public fun ByteArray.writeBeUInt(value: UInt, offset: Int = 0) {
    writeBeInt(value.toInt(), offset)
}

public fun ByteArray.writeNativeULong(value: ULong, offset: Int = 0) {
    writeNativeLong(value.toLong(), offset)
}

public fun ByteArray.writeLeULong(value: ULong, offset: Int = 0) {
    writeLeLong(value.toLong(), offset)
}

public fun ByteArray.writeBeULong(value: ULong, offset: Int = 0) {
    writeBeLong(value.toLong(), offset)
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
