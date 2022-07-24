package org.schism.memory

import org.schism.ffi.ADDRESS_TYPE
import org.schism.ffi.IntOrLong
import org.schism.math.requireAlignedTo
import java.lang.Math.addExact
import java.lang.Math.subtractExact
import java.lang.foreign.MemoryAddress
import java.lang.foreign.MemorySegment
import java.lang.foreign.MemorySession
import java.lang.foreign.ValueLayout.ADDRESS
import java.lang.foreign.ValueLayout.JAVA_BYTE

@JvmInline public value class NativeAddress private constructor(private val bits: Long) {
    public fun toBits(): Long {
        return bits
    }

    override fun toString(): String {
        return "<${bits.toString(16).padStart(16, '0')}>"
    }

    public companion object {
        @JvmStatic public val NULL: NativeAddress get() = NativeAddress(0)

        @JvmStatic public val BIT_SIZE: Int = ADDRESS.bitSize().toInt()

        @JvmStatic public val BYTE_SIZE: Int = ADDRESS.byteSize().toInt()

        public fun fromBits(bits: Long): NativeAddress {
            return NativeAddress(bits)
        }
    }
}

public fun NativeAddress.toMemoryAddress(): MemoryAddress {
    return MemoryAddress.ofLong(toBits())
}

public operator fun NativeAddress.plus(offset: Long): NativeAddress {
    return NativeAddress.fromBits(addExact(toBits(), offset))
}

public operator fun NativeAddress.minus(offset: Long): NativeAddress {
    return NativeAddress.fromBits(subtractExact(toBits(), offset))
}

public operator fun NativeAddress.minus(other: NativeAddress): Long {
    return subtractExact(toBits(), other.toBits())
}

public fun NativeAddress.requireAlignedTo(alignment: Long) {
    toBits().requireAlignedTo(alignment)
}

public fun NativeAddress.isNULL(): Boolean {
    return toBits() == 0L
}

public fun NativeAddress.readByte(): Byte {
    return toMemoryAddress()[JAVA_BYTE, 0]
}

public fun NativeAddress.readUByte(): UByte {
    return readByte().toUByte()
}

public fun NativeAddress.writeByte(value: Byte) {
    toMemoryAddress()[JAVA_BYTE, 0] = value
}

public fun NativeAddress.writeUByte(value: UByte) {
    writeByte(value.toByte())
}

public fun NativeAddress.readChar(): Char {
    return toMemoryAddress()[UNALIGNED_NATIVE_CHAR, 0]
}

public fun NativeAddress.writeChar(value: Char) {
    toMemoryAddress()[UNALIGNED_NATIVE_CHAR, 0] = value
}

public fun NativeAddress.readLeChar(): Char {
    return toMemoryAddress()[UNALIGNED_LE_CHAR, 0]
}

public fun NativeAddress.writeLeChar(value: Char) {
    toMemoryAddress()[UNALIGNED_LE_CHAR, 0] = value
}

public fun NativeAddress.readBeChar(): Char {
    return toMemoryAddress()[UNALIGNED_BE_CHAR, 0]
}

public fun NativeAddress.writeBeChar(value: Char) {
    toMemoryAddress()[UNALIGNED_BE_CHAR, 0] = value
}

public fun NativeAddress.readShort(): Short {
    return toMemoryAddress()[UNALIGNED_NATIVE_SHORT, 0]
}

public fun NativeAddress.readUShort(): UShort {
    return readShort().toUShort()
}

public fun NativeAddress.writeShort(value: Short) {
    toMemoryAddress()[UNALIGNED_NATIVE_SHORT, 0] = value
}

public fun NativeAddress.writeUShort(value: UShort) {
    writeShort(value.toShort())
}

public fun NativeAddress.readLeShort(): Short {
    return toMemoryAddress()[UNALIGNED_LE_SHORT, 0]
}

public fun NativeAddress.readLeUShort(): UShort {
    return readLeShort().toUShort()
}

public fun NativeAddress.writeLeShort(value: Short) {
    toMemoryAddress()[UNALIGNED_LE_SHORT, 0] = value
}

public fun NativeAddress.writeLeUShort(value: UShort) {
    writeLeShort(value.toShort())
}

public fun NativeAddress.readBeShort(): Short {
    return toMemoryAddress()[UNALIGNED_BE_SHORT, 0]
}

public fun NativeAddress.readBeUShort(): UShort {
    return readBeShort().toUShort()
}

public fun NativeAddress.writeBeShort(value: Short) {
    toMemoryAddress()[UNALIGNED_BE_SHORT, 0] = value
}

public fun NativeAddress.writeBeUShort(value: UShort) {
    writeBeShort(value.toShort())
}

public fun NativeAddress.readInt(): Int {
    return toMemoryAddress()[UNALIGNED_NATIVE_INT, 0]
}

public fun NativeAddress.readUInt(): UInt {
    return readInt().toUInt()
}

public fun NativeAddress.writeInt(value: Int) {
    toMemoryAddress()[UNALIGNED_NATIVE_INT, 0] = value
}

public fun NativeAddress.writeUInt(value: UInt) {
    writeInt(value.toInt())
}

public fun NativeAddress.readLeInt(): Int {
    return toMemoryAddress()[UNALIGNED_LE_INT, 0]
}

public fun NativeAddress.readLeUInt(): UInt {
    return readLeInt().toUInt()
}

public fun NativeAddress.writeLeInt(value: Int) {
    toMemoryAddress()[UNALIGNED_LE_INT, 0] = value
}

public fun NativeAddress.writeLeUInt(value: UInt) {
    writeLeInt(value.toInt())
}

public fun NativeAddress.readBeInt(): Int {
    return toMemoryAddress()[UNALIGNED_BE_INT, 0]
}

public fun NativeAddress.readBeUInt(): UInt {
    return readBeInt().toUInt()
}

public fun NativeAddress.writeBeInt(value: Int) {
    toMemoryAddress()[UNALIGNED_BE_INT, 0] = value
}

public fun NativeAddress.writeBeUInt(value: UInt) {
    writeBeInt(value.toInt())
}

public fun NativeAddress.readLong(): Long {
    return toMemoryAddress()[UNALIGNED_NATIVE_LONG, 0]
}

public fun NativeAddress.readULong(): ULong {
    return readLong().toULong()
}

public fun NativeAddress.writeLong(value: Long) {
    toMemoryAddress()[UNALIGNED_NATIVE_LONG, 0] = value
}

public fun NativeAddress.writeULong(value: ULong) {
    writeLong(value.toLong())
}

public fun NativeAddress.readLeLong(): Long {
    return toMemoryAddress()[UNALIGNED_LE_LONG, 0]
}

public fun NativeAddress.readLeULong(): ULong {
    return readLeLong().toULong()
}

public fun NativeAddress.writeLeLong(value: Long) {
    toMemoryAddress()[UNALIGNED_LE_LONG, 0] = value
}

public fun NativeAddress.writeLeULong(value: ULong) {
    writeLeLong(value.toLong())
}

public fun NativeAddress.readBeLong(): Long {
    return toMemoryAddress()[UNALIGNED_BE_LONG, 0]
}

public fun NativeAddress.readBeULong(): ULong {
    return readBeLong().toULong()
}

public fun NativeAddress.writeBeLong(value: Long) {
    toMemoryAddress()[UNALIGNED_BE_LONG, 0] = value
}

public fun NativeAddress.writeBeULong(value: ULong) {
    writeBeLong(value.toLong())
}

public fun NativeAddress.readFloat(): Float {
    return toMemoryAddress()[UNALIGNED_NATIVE_FLOAT, 0]
}

public fun NativeAddress.writeFloat(value: Float) {
    toMemoryAddress()[UNALIGNED_NATIVE_FLOAT, 0] = value
}

public fun NativeAddress.readLeFloat(): Float {
    return toMemoryAddress()[UNALIGNED_LE_FLOAT, 0]
}

public fun NativeAddress.writeLeFloat(value: Float) {
    toMemoryAddress()[UNALIGNED_LE_FLOAT, 0] = value
}

public fun NativeAddress.readBeFloat(): Float {
    return toMemoryAddress()[UNALIGNED_BE_FLOAT, 0]
}

public fun NativeAddress.writeBeFloat(value: Float) {
    toMemoryAddress()[UNALIGNED_BE_FLOAT, 0] = value
}

public fun NativeAddress.readDouble(): Double {
    return toMemoryAddress()[UNALIGNED_NATIVE_DOUBLE, 0]
}

public fun NativeAddress.writeDouble(value: Double) {
    toMemoryAddress()[UNALIGNED_NATIVE_DOUBLE, 0] = value
}

public fun NativeAddress.readLeDouble(): Double {
    return toMemoryAddress()[UNALIGNED_LE_DOUBLE, 0]
}

public fun NativeAddress.writeLeDouble(value: Double) {
    toMemoryAddress()[UNALIGNED_LE_DOUBLE, 0] = value
}

public fun NativeAddress.readBeDouble(): Double {
    return toMemoryAddress()[UNALIGNED_BE_DOUBLE, 0]
}

public fun NativeAddress.writeBeDouble(value: Double) {
    toMemoryAddress()[UNALIGNED_BE_DOUBLE, 0] = value
}

public fun NativeAddress.readUtf8CString(): String {
    return toMemoryAddress().getUtf8String(0)
}

public fun NativeAddress.readPointer(): NativeAddress {
    return when (ADDRESS_TYPE) {
        IntOrLong.INT -> NativeAddress.fromBits(readUInt().toLong())
        IntOrLong.LONG -> NativeAddress.fromBits(readLong())
    }
}

public fun NativeAddress.writePointer(value: NativeAddress) {
    when (ADDRESS_TYPE) {
        IntOrLong.INT -> writeInt(value.toBits().toInt())
        IntOrLong.LONG -> writeLong(value.toBits())
    }
}

public fun MemoryAddress.toNativeAddress(): NativeAddress {
    return NativeAddress.fromBits(toRawLongValue())
}

public fun memcpy(destination: NativeAddress, source: NativeAddress, size: Long) {
    MemorySegment.ofAddress(destination.toMemoryAddress(), size, MemorySession.global())
        .copyFrom(MemorySegment.ofAddress(source.toMemoryAddress(), size, MemorySession.global()))
}

public fun memcpy(
    destination: ByteArray,
    destinationOffset: Int = 0,
    source: NativeAddress,
    size: Int = destination.size - destinationOffset,
) {
    MemorySegment.ofArray(destination).asSlice(destinationOffset.toLong(), size.toLong())
        .copyFrom(MemorySegment.ofAddress(source.toMemoryAddress(), size.toLong(), MemorySession.global()))
}

public fun memcpy(
    destination: NativeAddress,
    source: ByteArray,
    sourceOffset: Int = 0,
    size: Int = source.size - sourceOffset,
) {
    MemorySegment.ofAddress(destination.toMemoryAddress(), size.toLong(), MemorySession.global())
        .copyFrom(MemorySegment.ofArray(source).asSlice(sourceOffset.toLong(), size.toLong()))
}

public fun memset(destination: NativeAddress, value: Byte, size: Long) {
    MemorySegment.ofAddress(destination.toMemoryAddress(), size, MemorySession.global()).fill(value)
}

public fun memset(destination: NativeAddress, value: UByte, size: Long) {
    memset(destination, value.toByte(), size)
}
