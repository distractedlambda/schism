package org.schism.memory

import org.schism.math.requireAlignedTo
import java.lang.Math.addExact
import java.lang.Math.subtractExact
import java.lang.foreign.MemoryAddress
import java.lang.foreign.MemorySegment
import java.lang.foreign.MemorySession
import java.lang.foreign.ValueLayout.JAVA_BYTE

@JvmInline public value class NativeAddress
@PublishedApi internal constructor(@PublishedApi internal val numericValue: Long) {
    internal fun toMemoryAddress(): MemoryAddress {
        return MemoryAddress.ofLong(numericValue)
    }

    public operator fun plus(offset: Long): NativeAddress {
        return NativeAddress(addExact(numericValue, offset))
    }

    public operator fun minus(offset: Long): NativeAddress {
        return NativeAddress(subtractExact(numericValue, offset))
    }

    public operator fun minus(other: NativeAddress): Long {
        return subtractExact(numericValue, other.numericValue)
    }

    public fun requireAlignedTo(alignment: Long) {
        numericValue.requireAlignedTo(alignment)
    }

    public fun isNULL(): Boolean {
        return numericValue == 0L
    }

    public fun readByte(): Byte {
        return toMemoryAddress()[JAVA_BYTE, 0]
    }

    public fun readUByte(): UByte {
        return readByte().toUByte()
    }

    public fun writeByte(value: Byte) {
        toMemoryAddress()[JAVA_BYTE, 0] = value
    }

    public fun writeUByte(value: UByte) {
        writeByte(value.toByte())
    }

    public fun readNativeChar(): Char {
        return toMemoryAddress()[UNALIGNED_NATIVE_CHAR, 0]
    }

    public fun writeNativeChar(value: Char) {
        toMemoryAddress()[UNALIGNED_NATIVE_CHAR, 0] = value
    }

    public fun readLeChar(): Char {
        return toMemoryAddress()[UNALIGNED_LE_CHAR, 0]
    }

    public fun writeLeChar(value: Char) {
        toMemoryAddress()[UNALIGNED_LE_CHAR, 0] = value
    }

    public fun readBeChar(): Char {
        return toMemoryAddress()[UNALIGNED_BE_CHAR, 0]
    }

    public fun writeBeChar(value: Char) {
        toMemoryAddress()[UNALIGNED_BE_CHAR, 0] = value
    }

    public fun readNativeShort(): Short {
        return toMemoryAddress()[UNALIGNED_NATIVE_SHORT, 0]
    }

    public fun readNativeUShort(): UShort {
        return readNativeShort().toUShort()
    }

    public fun writeNativeShort(value: Short) {
        toMemoryAddress()[UNALIGNED_NATIVE_SHORT, 0] = value
    }

    public fun writeNativeUShort(value: UShort) {
        writeNativeShort(value.toShort())
    }

    public fun readLeShort(): Short {
        return toMemoryAddress()[UNALIGNED_LE_SHORT, 0]
    }

    public fun readLeUShort(): UShort {
        return readLeShort().toUShort()
    }

    public fun writeLeShort(value: Short) {
        toMemoryAddress()[UNALIGNED_LE_SHORT, 0] = value
    }

    public fun writeLeUShort(value: UShort) {
        writeLeShort(value.toShort())
    }

    public fun readBeShort(): Short {
        return toMemoryAddress()[UNALIGNED_BE_SHORT, 0]
    }

    public fun readBeUShort(): UShort {
        return readBeShort().toUShort()
    }

    public fun writeBeShort(value: Short) {
        toMemoryAddress()[UNALIGNED_BE_SHORT, 0] = value
    }

    public fun writeBeUShort(value: UShort) {
        writeBeShort(value.toShort())
    }

    public fun readNativeInt(): Int {
        return toMemoryAddress()[UNALIGNED_NATIVE_INT, 0]
    }

    public fun readNativeUInt(): UInt {
        return readNativeInt().toUInt()
    }

    public fun writeNativeInt(value: Int) {
        toMemoryAddress()[UNALIGNED_NATIVE_INT, 0] = value
    }

    public fun writeNativeUInt(value: UInt) {
        writeNativeInt(value.toInt())
    }

    public fun readLeInt(): Int {
        return toMemoryAddress()[UNALIGNED_LE_INT, 0]
    }

    public fun readLeUInt(): UInt {
        return readLeInt().toUInt()
    }

    public fun writeLeInt(value: Int) {
        toMemoryAddress()[UNALIGNED_LE_INT, 0] = value
    }

    public fun writeLeUInt(value: UInt) {
        writeLeInt(value.toInt())
    }

    public fun readBeInt(): Int {
        return toMemoryAddress()[UNALIGNED_BE_INT, 0]
    }

    public fun readBeUInt(): UInt {
        return readBeInt().toUInt()
    }

    public fun writeBeInt(value: Int) {
        toMemoryAddress()[UNALIGNED_BE_INT, 0] = value
    }

    public fun writeBeUInt(value: UInt) {
        writeBeInt(value.toInt())
    }

    public fun readNativeLong(): Long {
        return toMemoryAddress()[UNALIGNED_NATIVE_LONG, 0]
    }

    public fun readNativeULong(): ULong {
        return readNativeLong().toULong()
    }

    public fun writeNativeLong(value: Long) {
        toMemoryAddress()[UNALIGNED_NATIVE_LONG, 0] = value
    }

    public fun writeNativeULong(value: ULong) {
        writeNativeLong(value.toLong())
    }

    public fun readLeLong(): Long {
        return toMemoryAddress()[UNALIGNED_LE_LONG, 0]
    }

    public fun readLeULong(): ULong {
        return readLeLong().toULong()
    }

    public fun writeLeLong(value: Long) {
        toMemoryAddress()[UNALIGNED_LE_LONG, 0] = value
    }

    public fun writeLeULong(value: ULong) {
        writeLeLong(value.toLong())
    }

    public fun readBeLong(): Long {
        return toMemoryAddress()[UNALIGNED_BE_LONG, 0]
    }

    public fun readBeULong(): ULong {
        return readBeLong().toULong()
    }

    public fun writeBeLong(value: Long) {
        toMemoryAddress()[UNALIGNED_BE_LONG, 0] = value
    }

    public fun writeBeULong(value: ULong) {
        writeBeLong(value.toLong())
    }

    public fun readNativeFloat(): Float {
        return toMemoryAddress()[UNALIGNED_NATIVE_FLOAT, 0]
    }

    public fun writeNativeFloat(value: Float) {
        toMemoryAddress()[UNALIGNED_NATIVE_FLOAT, 0] = value
    }

    public fun readLeFloat(): Float {
        return toMemoryAddress()[UNALIGNED_LE_FLOAT, 0]
    }

    public fun writeLeFloat(value: Float) {
        toMemoryAddress()[UNALIGNED_LE_FLOAT, 0] = value
    }

    public fun readBeFloat(): Float {
        return toMemoryAddress()[UNALIGNED_BE_FLOAT, 0]
    }

    public fun writeBeFloat(value: Float) {
        toMemoryAddress()[UNALIGNED_BE_FLOAT, 0] = value
    }

    public fun readNativeDouble(): Double {
        return toMemoryAddress()[UNALIGNED_NATIVE_DOUBLE, 0]
    }

    public fun writeNativeDouble(value: Double) {
        toMemoryAddress()[UNALIGNED_NATIVE_DOUBLE, 0] = value
    }

    public fun readLeDouble(): Double {
        return toMemoryAddress()[UNALIGNED_LE_DOUBLE, 0]
    }

    public fun writeLeDouble(value: Double) {
        toMemoryAddress()[UNALIGNED_LE_DOUBLE, 0] = value
    }

    public fun readBeDouble(): Double {
        return toMemoryAddress()[UNALIGNED_BE_DOUBLE, 0]
    }

    public fun writeBeDouble(value: Double) {
        toMemoryAddress()[UNALIGNED_BE_DOUBLE, 0] = value
    }

    override fun toString(): String {
        return "NativeAddress(0x${numericValue.toString(16).padStart(16, '0')})"
    }

    public companion object {
        public val NULL: NativeAddress get() = NativeAddress(0)
    }
}

internal fun MemoryAddress.toNativeAddress(): NativeAddress {
    return NativeAddress(toRawLongValue())
}

public fun memcpy(dst: NativeAddress, src: NativeAddress, size: Long) {
    MemorySegment.ofAddress(dst.toMemoryAddress(), size, MemorySession.global())
        .copyFrom(MemorySegment.ofAddress(src.toMemoryAddress(), size, MemorySession.global()))
}

public fun memcpy(dst: ByteArray, dstOffset: Int = 0, src: NativeAddress, size: Int = dst.size - dstOffset) {
    MemorySegment.ofArray(dst).asSlice(dstOffset.toLong(), size.toLong())
        .copyFrom(MemorySegment.ofAddress(src.toMemoryAddress(), size.toLong(), MemorySession.global()))
}

public fun memcpy(dst: NativeAddress, src: ByteArray, srcOffset: Int = 0, size: Int = src.size - srcOffset) {
    MemorySegment.ofAddress(dst.toMemoryAddress(), size.toLong(), MemorySession.global())
        .copyFrom(MemorySegment.ofArray(src).asSlice(srcOffset.toLong(), size.toLong()))
}

public fun memset(dst: NativeAddress, value: Byte, size: Long) {
    MemorySegment.ofAddress(dst.toMemoryAddress(), size, MemorySession.global()).fill(value)
}

public fun memset(dst: NativeAddress, value: UByte, size: Long) {
    memset(dst, value.toByte(), size)
}
