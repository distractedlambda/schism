package org.schism.memory

import java.awt.Point

public interface Memory {
    public val size: Long

    public val isReadable: Boolean

    public val isWritable: Boolean

    public val isNative: Boolean

    public val startAddress: NativeAddress

    public fun encoder(): MemoryEncoder

    public fun decoder(): MemoryDecoder

    public fun slice(offset: Long = 0L, size: Long = this.size - offset): Memory

    public fun getByte(offset: Long = 0L): Byte

    public fun getChar(offset: Long = 0L): Char

    public fun getLeChar(offset: Long = 0L): Char

    public fun getBeChar(offset: Long = 0L): Char

    public fun getShort(offset: Long = 0L): Short

    public fun getLeShort(offset: Long = 0L): Short

    public fun getBeShort(offset: Long = 0L): Short

    public fun getInt(offset: Long = 0L): Int

    public fun getLeInt(offset: Long = 0L): Int

    public fun getBeInt(offset: Long = 0L): Int

    public fun getLong(offset: Long = 0L): Long

    public fun getLeLong(offset: Long = 0L): Long

    public fun getBeLong(offset: Long = 0L): Long

    public fun getFloat(offset: Long = 0L): Float

    public fun getLeFloat(offset: Long = 0L): Float

    public fun getBeFloat(offset: Long = 0L): Float

    public fun getDouble(offset: Long = 0L): Double

    public fun getLeDouble(offset: Long = 0L): Double

    public fun getBeDouble(offset: Long = 0L): Double

    public fun setByte(value: Byte, offset: Long = 0L)

    public fun setChar(value: Char, offset: Long = 0L)

    public fun setLeChar(value: Char, offset: Long = 0L)

    public fun setBeChar(value: Char, offset: Long = 0L)

    public fun setShort(value: Short, offset: Long = 0L)

    public fun setLeShort(value: Short, offset: Long = 0L)

    public fun setBeShort(value: Short, offset: Long = 0L)

    public fun setInt(value: Int, offset: Long = 0L)

    public fun setLeInt(value: Int, offset: Long = 0L)

    public fun setBeInt(value: Int, offset: Long = 0L)

    public fun setLong(value: Long, offset: Long = 0L)

    public fun setLeLong(value: Long, offset: Long = 0L)

    public fun setBeLong(value: Long, offset: Long = 0L)

    public fun setFloat(value: Float, offset: Long = 0L)

    public fun setLeFloat(value: Float, offset: Long = 0L)

    public fun setBeFloat(value: Float, offset: Long = 0L)

    public fun setDouble(value: Double, offset: Long = 0L)

    public fun setLeDouble(value: Double, offset: Long = 0L)

    public fun setBeDouble(value: Double, offset: Long = 0L)

    public companion object
}

public fun Memory.requireAlignedTo(alignment: Long) {
    // FIXME: what about heap memory?
    if (isNative) {
        startAddress.requireAlignedTo(alignment)
    }
}

public fun Memory.getUByte(offset: Long = 0L): UByte {
    return getByte(offset).toUByte()
}

public fun Memory.getUShort(offset: Long = 0L): UShort {
    return getShort(offset).toUShort()
}

public fun Memory.getLeUShort(offset: Long = 0L): UShort {
    return getLeShort(offset).toUShort()
}

public fun Memory.getBeUShort(offset: Long = 0L): UShort {
    return getBeShort(offset).toUShort()
}

public fun Memory.getUInt(offset: Long = 0L): UInt {
    return getInt(offset).toUInt()
}

public fun Memory.getLeUInt(offset: Long = 0L): UInt {
    return getLeInt(offset).toUInt()
}

public fun Memory.getBeUInt(offset: Long = 0L): UInt {
    return getBeInt(offset).toUInt()
}

public fun Memory.getULong(offset: Long = 0L): ULong {
    return getLong(offset).toULong()
}

public fun Memory.getLeULong(offset: Long = 0L): ULong {
    return getLeLong(offset).toULong()
}

public fun Memory.getBeULong(offset: Long = 0L): ULong {
    return getBeLong(offset).toULong()
}

public fun Memory.setUByte(value: UByte, offset: Long = 0L) {
    setByte(value.toByte(), offset)
}

public fun Memory.setUShort(value: UShort, offset: Long = 0L) {
    setShort(value.toShort(), offset)
}

public fun Memory.setLeUShort(value: UShort, offset: Long = 0L) {
    setLeShort(value.toShort(), offset)
}

public fun Memory.setBeUShort(value: UShort, offset: Long = 0L) {
    setBeShort(value.toShort(), offset)
}

public fun Memory.setUInt(value: UInt, offset: Long = 0L) {
    setInt(value.toInt(), offset)
}

public fun Memory.setLeUInt(value: UInt, offset: Long = 0L) {
    setLeInt(value.toInt(), offset)
}

public fun Memory.setBeUInt(value: UInt, offset: Long = 0L) {
    setBeInt(value.toInt(), offset)
}

public fun Memory.setULong(value: ULong, offset: Long = 0L) {
    setLong(value.toLong(), offset)
}

public fun Memory.setLeULong(value: ULong, offset: Long = 0L) {
    setLeLong(value.toLong(), offset)
}

public fun Memory.setBeULong(value: ULong, offset: Long = 0L) {
    setBeLong(value.toLong(), offset)
}

public fun Memory.getPointer(offset: Long = 0L): NativeAddress {
    return NativeAddress.fromBits(when (NativeAddress.BIT_SIZE) {
        32 -> getULong(offset).toLong()
        64 -> getLong(offset)
        else -> throw UnsupportedOperationException("Unsupported native address width")
    })
}

public fun Memory.setPointer(value: NativeAddress, offset: Long = 0L) {
    when (NativeAddress.BIT_SIZE) {
        32 -> setInt(value.toBits().toInt(), offset)
        64 -> setLong(value.toBits(), offset)
        else -> throw UnsupportedOperationException("Unsupported native address width")
    }
}