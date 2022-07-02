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

    override fun readByte(offset: Long): Byte {
        checkReadable()
        checkIndex(offset, size)
        return array.readByte(arrayOffset + offset.toInt())
    }

    override fun readNativeChar(offset: Long): Char {
        checkReadable()
        checkFromIndexSize(offset, 2, size)
        return array.readNativeChar(arrayOffset + offset.toInt())
    }

    override fun readLeChar(offset: Long): Char {
        checkReadable()
        checkFromIndexSize(offset, 2, size)
        return array.readLeChar(arrayOffset + offset.toInt())
    }

    override fun readBeChar(offset: Long): Char {
        checkReadable()
        checkFromIndexSize(offset, 2, size)
        return array.readBeChar(arrayOffset + offset.toInt())
    }

    override fun readNativeShort(offset: Long): Short {
        checkReadable()
        checkFromIndexSize(offset, 2, size)
        return array.readNativeShort(arrayOffset + offset.toInt())
    }

    override fun readLeShort(offset: Long): Short {
        checkReadable()
        checkFromIndexSize(offset, 2, size)
        return array.readLeShort(arrayOffset + offset.toInt())
    }

    override fun readBeShort(offset: Long): Short {
        checkReadable()
        checkFromIndexSize(offset, 2, size)
        return array.readBeShort(arrayOffset + offset.toInt())
    }

    override fun readNativeInt(offset: Long): Int {
        checkReadable()
        checkFromIndexSize(offset, 4, size)
        return array.readNativeInt(arrayOffset + offset.toInt())
    }

    override fun readLeInt(offset: Long): Int {
        checkReadable()
        checkFromIndexSize(offset, 4, size)
        return array.readLeInt(arrayOffset + offset.toInt())
    }

    override fun readBeInt(offset: Long): Int {
        checkReadable()
        checkFromIndexSize(offset, 4, size)
        return array.readBeInt(arrayOffset + offset.toInt())
    }

    override fun readNativeLong(offset: Long): Long {
        checkReadable()
        checkFromIndexSize(offset, 8, size)
        return array.readNativeLong(arrayOffset + offset.toInt())
    }

    override fun readLeLong(offset: Long): Long {
        checkReadable()
        checkFromIndexSize(offset, 8, size)
        return array.readLeLong(arrayOffset + offset.toInt())
    }

    override fun readBeLong(offset: Long): Long {
        checkReadable()
        checkFromIndexSize(offset, 8, size)
        return array.readBeLong(arrayOffset + offset.toInt())
    }

    override fun readNativeFloat(offset: Long): Float {
        checkReadable()
        checkFromIndexSize(offset, 4, size)
        return array.readNativeFloat(arrayOffset + offset.toInt())
    }

    override fun readLeFloat(offset: Long): Float {
        checkReadable()
        checkFromIndexSize(offset, 4, size)
        return array.readLeFloat(arrayOffset + offset.toInt())
    }

    override fun readBeFloat(offset: Long): Float {
        checkReadable()
        checkFromIndexSize(offset, 4, size)
        return array.readBeFloat(arrayOffset + offset.toInt())
    }

    override fun readNativeDouble(offset: Long): Double {
        checkReadable()
        checkFromIndexSize(offset, 8, size)
        return array.readNativeDouble(arrayOffset + offset.toInt())
    }

    override fun readLeDouble(offset: Long): Double {
        checkReadable()
        checkFromIndexSize(offset, 8, size)
        return array.readLeDouble(arrayOffset + offset.toInt())
    }

    override fun readBeDouble(offset: Long): Double {
        checkReadable()
        checkFromIndexSize(offset, 8, size)
        return array.readBeDouble(arrayOffset + offset.toInt())
    }

    override fun writeByte(value: Byte, offset: Long) {
        checkWritable()
        checkIndex(offset, size)
        array.writeByte(value, arrayOffset + offset.toInt())
    }

    override fun writeNativeChar(value: Char, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 2, size)
        array.writeNativeChar(value, arrayOffset + offset.toInt())
    }

    override fun writeLeChar(value: Char, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 2, size)
        array.writeLeChar(value, arrayOffset + offset.toInt())
    }

    override fun writeBeChar(value: Char, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 2, size)
        array.writeBeChar(value, arrayOffset + offset.toInt())
    }

    override fun writeNativeShort(value: Short, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 2, size)
        array.writeNativeShort(value, arrayOffset + offset.toInt())
    }

    override fun writeLeShort(value: Short, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 2, size)
        array.writeLeShort(value, arrayOffset + offset.toInt())
    }

    override fun writeBeShort(value: Short, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 2, size)
        array.writeBeShort(value, arrayOffset + offset.toInt())
    }

    override fun writeNativeInt(value: Int, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 4, size)
        array.writeNativeInt(value, arrayOffset + offset.toInt())
    }

    override fun writeLeInt(value: Int, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 4, size)
        array.writeLeInt(value, arrayOffset + offset.toInt())
    }

    override fun writeBeInt(value: Int, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 4, size)
        array.writeBeInt(value, arrayOffset + offset.toInt())
    }

    override fun writeNativeLong(value: Long, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 8, size)
        array.writeNativeLong(value, arrayOffset + offset.toInt())
    }

    override fun writeLeLong(value: Long, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 8, size)
        array.writeLeLong(value, arrayOffset + offset.toInt())
    }

    override fun writeBeLong(value: Long, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 8, size)
        array.writeBeLong(value, arrayOffset + offset.toInt())
    }

    override fun writeNativeFloat(value: Float, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 4, size)
        array.writeNativeFloat(value, arrayOffset + offset.toInt())
    }

    override fun writeLeFloat(value: Float, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 4, size)
        array.writeLeFloat(value, arrayOffset + offset.toInt())
    }

    override fun writeBeFloat(value: Float, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 4, size)
        array.writeBeFloat(value, arrayOffset + offset.toInt())
    }

    override fun writeNativeDouble(value: Double, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 8, size)
        array.writeNativeDouble(value, arrayOffset + offset.toInt())
    }

    override fun writeLeDouble(value: Double, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 8, size)
        array.writeLeDouble(value, arrayOffset + offset.toInt())
    }

    override fun writeBeDouble(value: Double, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 8, size)
        array.writeBeDouble(value, arrayOffset + offset.toInt())
    }

    companion object {
        const val READABLE = 0x1
        const val WRITABLE = 0x2
    }
}
