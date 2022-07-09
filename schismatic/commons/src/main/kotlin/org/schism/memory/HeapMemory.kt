package org.schism.memory

import java.util.Objects.checkFromIndexSize
import java.util.Objects.checkIndex

internal class HeapMemory(
    private val array: ByteArray,
    private val arrayOffset: Int,
    private val intSize: Int,
    private val flags: Int,
) : Memory {
    override val size: Long get() {
        return intSize.toLong()
    }

    override val isReadable: Boolean get() {
        return (flags and READABLE) != 0
    }

    override val isWritable: Boolean get() {
        return (flags and WRITABLE) != 0
    }

    override val isNative: Boolean get() {
        return false
    }

    override val startAddress: NativeAddress get() {
        throw UnsupportedOperationException()
    }

    private fun checkReadable() {
        if (!isReadable) {
            throw UnsupportedOperationException()
        }
    }

    private fun checkWritable() {
        if (!isWritable) {
            throw UnsupportedOperationException()
        }
    }

    override fun asReadOnly(): Memory {
        return if (flags == READABLE) {
            this
        } else {
            checkReadable()
            HeapMemory(array, arrayOffset, intSize, READABLE)
        }
    }

    override fun copyTo(destination: ByteArray, destinationOffset: Int) {
        checkReadable()
        memcpy(destination, destinationOffset, array, arrayOffset, minOf(intSize, destination.size - destinationOffset))
    }

    override fun copyTo(destination: NativeAddress) {
        checkReadable()
        memcpy(destination, array, arrayOffset)
    }

    override fun copyTo(destination: Memory) {
        checkReadable()
        memcpy(destination.slice(size = minOf(destination.size, intSize.toLong())), array, arrayOffset)
    }

    override fun copyFrom(source: NativeAddress) {
        checkWritable()
        memcpy(array, arrayOffset, source, intSize)
    }

    override fun copyFrom(source: ByteArray, sourceOffset: Int) {
        checkWritable()
        memcpy(array, arrayOffset, source, sourceOffset, minOf(intSize, source.size - sourceOffset))
    }

    override fun fill(value: Byte) {
        checkWritable()
        memset(array, arrayOffset, value, intSize)
    }

    override fun encoder(): MemoryEncoder {
        checkWritable()
        return HeapMemoryEncoder(array, arrayOffset, arrayOffset + intSize)
    }

    override fun decoder(): MemoryDecoder {
        checkReadable()
        return HeapMemoryDecoder(array, arrayOffset, arrayOffset + intSize)
    }

    override fun slice(offset: Long, size: Long): Memory {
        checkFromIndexSize(offset, size, this.size)
        return HeapMemory(array, arrayOffset + offset.toInt(), size.toInt(), flags)
    }

    override fun getByte(offset: Long): Byte {
        checkReadable()
        checkIndex(offset, size)
        return array[arrayOffset + offset.toInt()]
    }

    override fun getChar(offset: Long): Char {
        checkReadable()
        checkFromIndexSize(offset, 2, size)
        return array.getChar(arrayOffset + offset.toInt())
    }

    override fun getLeChar(offset: Long): Char {
        checkReadable()
        checkFromIndexSize(offset, 2, size)
        return array.getLeChar(arrayOffset + offset.toInt())
    }

    override fun getBeChar(offset: Long): Char {
        checkReadable()
        checkFromIndexSize(offset, 2, size)
        return array.getBeChar(arrayOffset + offset.toInt())
    }

    override fun getShort(offset: Long): Short {
        checkReadable()
        checkFromIndexSize(offset, 2, size)
        return array.getShort(arrayOffset + offset.toInt())
    }

    override fun getLeShort(offset: Long): Short {
        checkReadable()
        checkFromIndexSize(offset, 2, size)
        return array.getLeShort(arrayOffset + offset.toInt())
    }

    override fun getBeShort(offset: Long): Short {
        checkReadable()
        checkFromIndexSize(offset, 2, size)
        return array.getBeShort(arrayOffset + offset.toInt())
    }

    override fun getInt(offset: Long): Int {
        checkReadable()
        checkFromIndexSize(offset, 4, size)
        return array.getInt(arrayOffset + offset.toInt())
    }

    override fun getLeInt(offset: Long): Int {
        checkReadable()
        checkFromIndexSize(offset, 4, size)
        return array.getLeInt(arrayOffset + offset.toInt())
    }

    override fun getBeInt(offset: Long): Int {
        checkReadable()
        checkFromIndexSize(offset, 4, size)
        return array.getBeInt(arrayOffset + offset.toInt())
    }

    override fun getLong(offset: Long): Long {
        checkReadable()
        checkFromIndexSize(offset, 8, size)
        return array.getLong(arrayOffset + offset.toInt())
    }

    override fun getLeLong(offset: Long): Long {
        checkReadable()
        checkFromIndexSize(offset, 8, size)
        return array.getLeLong(arrayOffset + offset.toInt())
    }

    override fun getBeLong(offset: Long): Long {
        checkReadable()
        checkFromIndexSize(offset, 8, size)
        return array.getBeLong(arrayOffset + offset.toInt())
    }

    override fun getFloat(offset: Long): Float {
        checkReadable()
        checkFromIndexSize(offset, 4, size)
        return array.getFloat(arrayOffset + offset.toInt())
    }

    override fun getLeFloat(offset: Long): Float {
        checkReadable()
        checkFromIndexSize(offset, 4, size)
        return array.getLeFloat(arrayOffset + offset.toInt())
    }

    override fun getBeFloat(offset: Long): Float {
        checkReadable()
        checkFromIndexSize(offset, 4, size)
        return array.getBeFloat(arrayOffset + offset.toInt())
    }

    override fun getDouble(offset: Long): Double {
        checkReadable()
        checkFromIndexSize(offset, 8, size)
        return array.getDouble(arrayOffset + offset.toInt())
    }

    override fun getLeDouble(offset: Long): Double {
        checkReadable()
        checkFromIndexSize(offset, 8, size)
        return array.getLeDouble(arrayOffset + offset.toInt())
    }

    override fun getBeDouble(offset: Long): Double {
        checkReadable()
        checkFromIndexSize(offset, 8, size)
        return array.getBeDouble(arrayOffset + offset.toInt())
    }

    override fun setByte(value: Byte, offset: Long) {
        checkWritable()
        checkIndex(offset, size)
        array[arrayOffset + offset.toInt()] = value
    }

    override fun setChar(value: Char, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 2, size)
        array.setChar(value, arrayOffset + offset.toInt())
    }

    override fun setLeChar(value: Char, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 2, size)
        array.setLeChar(value, arrayOffset + offset.toInt())
    }

    override fun setBeChar(value: Char, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 2, size)
        array.setBeChar(value, arrayOffset + offset.toInt())
    }

    override fun setShort(value: Short, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 2, size)
        array.setShort(value, arrayOffset + offset.toInt())
    }

    override fun setLeShort(value: Short, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 2, size)
        array.setLeShort(value, arrayOffset + offset.toInt())
    }

    override fun setBeShort(value: Short, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 2, size)
        array.setBeShort(value, arrayOffset + offset.toInt())
    }

    override fun setInt(value: Int, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 4, size)
        array.setInt(value, arrayOffset + offset.toInt())
    }

    override fun setLeInt(value: Int, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 4, size)
        array.setLeInt(value, arrayOffset + offset.toInt())
    }

    override fun setBeInt(value: Int, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 4, size)
        array.setBeInt(value, arrayOffset + offset.toInt())
    }

    override fun setLong(value: Long, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 8, size)
        array.setLong(value, arrayOffset + offset.toInt())
    }

    override fun setLeLong(value: Long, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 8, size)
        array.setLeLong(value, arrayOffset + offset.toInt())
    }

    override fun setBeLong(value: Long, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 8, size)
        array.setBeLong(value, arrayOffset + offset.toInt())
    }

    override fun setFloat(value: Float, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 4, size)
        array.setFloat(value, arrayOffset + offset.toInt())
    }

    override fun setLeFloat(value: Float, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 4, size)
        array.setLeFloat(value, arrayOffset + offset.toInt())
    }

    override fun setBeFloat(value: Float, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 4, size)
        array.setBeFloat(value, arrayOffset + offset.toInt())
    }

    override fun setDouble(value: Double, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 8, size)
        array.setDouble(value, arrayOffset + offset.toInt())
    }

    override fun setLeDouble(value: Double, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 8, size)
        array.setLeDouble(value, arrayOffset + offset.toInt())
    }

    override fun setBeDouble(value: Double, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 8, size)
        array.setBeDouble(value, arrayOffset + offset.toInt())
    }

    companion object {
        const val READABLE = 0x1
        const val WRITABLE = 0x2
    }
}
