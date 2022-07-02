@file:Suppress("NOTHING_TO_INLINE")

package org.schism.memory

public interface Memory {
    public val size: Long

    public val isReadable: Boolean

    public val isWritable: Boolean

    public val isNative: Boolean

    public val startAddress: NativeAddress

    public fun encoder(): MemoryEncoder

    public fun decoder(): MemoryDecoder

    public fun slice(offset: Long = 0L, size: Long = this.size - offset): Memory

    public fun readByte(offset: Long = 0L): Byte

    public fun readNativeChar(offset: Long = 0L): Char

    public fun readLeChar(offset: Long = 0L): Char

    public fun readBeChar(offset: Long = 0L): Char

    public fun readNativeShort(offset: Long = 0L): Short

    public fun readLeShort(offset: Long = 0L): Short

    public fun readBeShort(offset: Long = 0L): Short

    public fun readNativeInt(offset: Long = 0L): Int

    public fun readLeInt(offset: Long = 0L): Int

    public fun readBeInt(offset: Long = 0L): Int

    public fun readNativeLong(offset: Long = 0L): Long

    public fun readLeLong(offset: Long = 0L): Long

    public fun readBeLong(offset: Long = 0L): Long

    public fun readNativeFloat(offset: Long = 0L): Float

    public fun readLeFloat(offset: Long = 0L): Float

    public fun readBeFloat(offset: Long = 0L): Float

    public fun readNativeDouble(offset: Long = 0L): Double

    public fun readLeDouble(offset: Long = 0L): Double

    public fun readBeDouble(offset: Long = 0L): Double

    public fun writeByte(value: Byte, offset: Long = 0L)

    public fun writeNativeChar(value: Char, offset: Long = 0L)

    public fun writeLeChar(value: Char, offset: Long = 0L)

    public fun writeBeChar(value: Char, offset: Long = 0L)

    public fun writeNativeShort(value: Short, offset: Long = 0L)

    public fun writeLeShort(value: Short, offset: Long = 0L)

    public fun writeBeShort(value: Short, offset: Long = 0L)

    public fun writeNativeInt(value: Int, offset: Long = 0L)

    public fun writeLeInt(value: Int, offset: Long = 0L)

    public fun writeBeInt(value: Int, offset: Long = 0L)

    public fun writeNativeLong(value: Long, offset: Long = 0L)

    public fun writeLeLong(value: Long, offset: Long = 0L)

    public fun writeBeLong(value: Long, offset: Long = 0L)

    public fun writeNativeFloat(value: Float, offset: Long = 0L)

    public fun writeLeFloat(value: Float, offset: Long = 0L)

    public fun writeBeFloat(value: Float, offset: Long = 0L)

    public fun writeNativeDouble(value: Double, offset: Long = 0L)

    public fun writeLeDouble(value: Double, offset: Long = 0L)

    public fun writeBeDouble(value: Double, offset: Long = 0L)
}

public inline fun Memory.readUByte(offset: Long = 0L): UByte {
    return readByte(offset).toUByte()
}

public inline fun Memory.readNativeUShort(offset: Long = 0L): UShort {
    return readNativeShort(offset).toUShort()
}

public inline fun Memory.readLeUShort(offset: Long = 0L): UShort {
    return readLeShort(offset).toUShort()
}

public inline fun Memory.readBeUShort(offset: Long = 0L): UShort {
    return readBeShort(offset).toUShort()
}

public inline fun Memory.readNativeUInt(offset: Long = 0L): UInt {
    return readNativeInt(offset).toUInt()
}

public inline fun Memory.readLeUInt(offset: Long = 0L): UInt {
    return readLeInt(offset).toUInt()
}

public inline fun Memory.readBeUInt(offset: Long = 0L): UInt {
    return readBeInt(offset).toUInt()
}

public inline fun Memory.readNativeULong(offset: Long = 0L): ULong {
    return readNativeLong(offset).toULong()
}

public inline fun Memory.readLeULong(offset: Long = 0L): ULong {
    return readLeLong(offset).toULong()
}

public inline fun Memory.readBeULong(offset: Long = 0L): ULong {
    return readBeLong(offset).toULong()
}

public inline fun Memory.writeUByte(value: UByte, offset: Long = 0L) {
    writeByte(value.toByte(), offset)
}

public inline fun Memory.writeNativeUShort(value: UShort, offset: Long = 0L) {
    writeNativeShort(value.toShort(), offset)
}

public inline fun Memory.writeLeUShort(value: UShort, offset: Long = 0L) {
    writeLeShort(value.toShort(), offset)
}

public inline fun Memory.writeBeUShort(value: UShort, offset: Long = 0L) {
    writeBeShort(value.toShort(), offset)
}

public inline fun Memory.writeNativeUInt(value: UInt, offset: Long = 0L) {
    writeNativeInt(value.toInt(), offset)
}

public inline fun Memory.writeLeUInt(value: UInt, offset: Long = 0L) {
    writeLeInt(value.toInt(), offset)
}

public inline fun Memory.writeBeUInt(value: UInt, offset: Long = 0L) {
    writeBeInt(value.toInt(), offset)
}

public inline fun Memory.writeNativeULong(value: ULong, offset: Long = 0L) {
    writeNativeLong(value.toLong(), offset)
}

public inline fun Memory.writeLeULong(value: ULong, offset: Long = 0L) {
    writeLeLong(value.toLong(), offset)
}

public inline fun Memory.writeBeULong(value: ULong, offset: Long = 0L) {
    writeBeLong(value.toLong(), offset)
}
