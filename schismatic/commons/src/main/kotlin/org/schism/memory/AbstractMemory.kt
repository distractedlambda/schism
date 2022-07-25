package org.schism.memory

import java.util.Objects.checkFromIndexSize
import java.util.Objects.checkIndex

internal abstract class AbstractMemory(
    final override val size: Long,
    final override val isReadable: Boolean,
    final override val isWritable: Boolean,
) : Memory {
    private fun checkReadable() {
        if (!isReadable) {
            throw UnsupportedOperationException("Memory is not readable")
        }
    }

    private fun checkWritable() {
        if (!isWritable) {
            throw UnsupportedOperationException("Memory is not writable")
        }
    }

    private fun checkBounds(index: Long, size: Long) {
        checkFromIndexSize(index, size, this.size)
    }

    protected abstract fun shallowReadOnlyCopy(): Memory

    final override fun asReadOnly(): Memory {
        checkReadable()
        return when {
            isWritable -> shallowReadOnlyCopy()
            else -> this
        }
    }

    protected abstract fun copyToSafe(destination: ByteArray, destinationOffset: Int, copySize: Int)

    final override fun copyTo(destination: ByteArray, destinationOffset: Int) {
        checkReadable()
        checkIndex(destinationOffset, destination.size)
        val copySize = minOf((destination.size - destinationOffset).toLong(), size).toInt()
        copyToSafe(destination, destinationOffset, copySize)
    }

    protected abstract fun copyToSafe(destination: NativeAddress)

    final override fun copyTo(destination: NativeAddress) {
        checkReadable()
        copyToSafe(destination)
    }

    protected abstract fun copyToSafe(destination: Memory)

    final override fun copyTo(destination: Memory) {
        checkReadable()
        copyToSafe(destination.slice(size = minOf(size, destination.size)))
    }

    protected abstract fun copyFromSafe(source: ReadOnlyByteArray, sourceOffset: Int, copySize: Int)

    final override fun copyFrom(source: ReadOnlyByteArray, sourceOffset: Int) {
        checkWritable()
        checkIndex(sourceOffset, source.size)
        val copySize = minOf((source.size - sourceOffset).toLong(), size).toInt()
        copyFromSafe(source, sourceOffset, copySize)
    }

    protected abstract fun copyFromSafe(source: NativeAddress)

    final override fun copyFrom(source: NativeAddress) {
        checkReadable()
        copyFromSafe(source)
    }

    protected abstract fun fillSafe(value: Byte)

    final override fun fill(value: Byte) {
        checkWritable()
        fillSafe(value)
    }

    protected abstract fun encoderSafe(): MemoryEncoder

    final override fun encoder(): MemoryEncoder {
        checkWritable()
        return encoderSafe()
    }

    protected abstract fun decoderSafe(): MemoryDecoder

    final override fun decoder(): MemoryDecoder {
        checkReadable()
        return decoderSafe()
    }

    protected abstract fun sliceSafe(offset: Long, size: Long): Memory

    final override fun slice(offset: Long, size: Long): Memory {
        checkBounds(offset, size)
        return when {
            size == this.size -> this
            else -> sliceSafe(offset, size)
        }
    }

    protected abstract fun getByteSafe(offset: Long): Byte

    final override fun getByte(offset: Long): Byte {
        checkReadable()
        checkBounds(offset, 1)
        return getByteSafe(offset)
    }

    protected abstract fun getShortSafe(offset: Long): Short

    final override fun getShort(offset: Long): Short {
        checkReadable()
        checkBounds(offset, 2)
        return getShortSafe(offset)
    }

    protected abstract fun getLeShortSafe(offset: Long): Short

    final override fun getLeShort(offset: Long): Short {
        checkReadable()
        checkBounds(offset, 2)
        return getLeShortSafe(offset)
    }

    protected abstract fun getBeShortSafe(offset: Long): Short

    final override fun getBeShort(offset: Long): Short {
        checkReadable()
        checkBounds(offset, 2)
        return getBeShortSafe(offset)
    }

    protected abstract fun getIntSafe(offset: Long): Int

    final override fun getInt(offset: Long): Int {
        checkReadable()
        checkBounds(offset, 4)
        return getIntSafe(offset)
    }

    protected abstract fun getLeIntSafe(offset: Long): Int

    final override fun getLeInt(offset: Long): Int {
        checkReadable()
        checkBounds(offset, 4)
        return getLeIntSafe(offset)
    }

    protected abstract fun getBeIntSafe(offset: Long): Int

    final override fun getBeInt(offset: Long): Int {
        checkReadable()
        checkBounds(offset, 4)
        return getBeIntSafe(offset)
    }

    protected abstract fun getLongSafe(offset: Long): Long

    final override fun getLong(offset: Long): Long {
        checkReadable()
        checkBounds(offset, 8)
        return getLongSafe(offset)
    }

    protected abstract fun getLeLongSafe(offset: Long): Long

    final override fun getLeLong(offset: Long): Long {
        checkReadable()
        checkBounds(offset, 8)
        return getLeLongSafe(offset)
    }

    protected abstract fun getBeLongSafe(offset: Long): Long

    final override fun getBeLong(offset: Long): Long {
        checkReadable()
        checkBounds(offset, 8)
        return getBeLongSafe(offset)
    }

    protected abstract fun getFloatSafe(offset: Long): Float

    final override fun getFloat(offset: Long): Float {
        checkReadable()
        checkBounds(offset, 4)
        return getFloatSafe(offset)
    }

    protected abstract fun getLeFloatSafe(offset: Long): Float

    final override fun getLeFloat(offset: Long): Float {
        checkReadable()
        checkBounds(offset, 4)
        return getLeFloatSafe(offset)
    }

    protected abstract fun getBeFloatSafe(offset: Long): Float

    final override fun getBeFloat(offset: Long): Float {
        checkReadable()
        checkBounds(offset, 4)
        return getBeFloatSafe(offset)
    }

    protected abstract fun getDoubleSafe(offset: Long): Double

    final override fun getDouble(offset: Long): Double {
        checkReadable()
        checkBounds(offset, 8)
        return getDoubleSafe(offset)
    }

    protected abstract fun getLeDoubleSafe(offset: Long): Double

    final override fun getLeDouble(offset: Long): Double {
        checkReadable()
        checkBounds(offset, 8)
        return getLeDoubleSafe(offset)
    }

    protected abstract fun getBeDoubleSafe(offset: Long): Double

    final override fun getBeDouble(offset: Long): Double {
        checkReadable()
        checkBounds(offset, 8)
        return getBeDoubleSafe(offset)
    }

    protected abstract fun setByteSafe(value: Byte, offset: Long)

    final override fun setByte(value: Byte, offset: Long) {
        checkWritable()
        checkBounds(offset, 1)
        setByteSafe(value, offset)
    }

    protected abstract fun setShortSafe(value: Short, offset: Long)

    final override fun setShort(value: Short, offset: Long) {
        checkWritable()
        checkBounds(offset, 2)
        setShortSafe(value, offset)
    }

    protected abstract fun setLeShortSafe(value: Short, offset: Long)

    final override fun setLeShort(value: Short, offset: Long) {
        checkWritable()
        checkBounds(offset, 2)
        setLeShortSafe(value, offset)
    }

    protected abstract fun setBeShortSafe(value: Short, offset: Long)

    final override fun setBeShort(value: Short, offset: Long) {
        checkWritable()
        checkBounds(offset, 2)
        setBeShortSafe(value, offset)
    }

    protected abstract fun setIntSafe(value: Int, offset: Long)

    final override fun setInt(value: Int, offset: Long) {
        checkWritable()
        checkBounds(offset, 2)
        setIntSafe(value, offset)
    }

    protected abstract fun setLeIntSafe(value: Int, offset: Long)

    final override fun setLeInt(value: Int, offset: Long) {
        checkWritable()
        checkBounds(offset, 2)
        setLeIntSafe(value, offset)
    }

    protected abstract fun setBeIntSafe(value: Int, offset: Long)

    final override fun setBeInt(value: Int, offset: Long) {
        checkWritable()
        checkBounds(offset, 2)
        setBeIntSafe(value, offset)
    }

    protected abstract fun setLongSafe(value: Long, offset: Long)

    final override fun setLong(value: Long, offset: Long) {
        checkWritable()
        checkBounds(offset, 2)
        setLongSafe(value, offset)
    }

    protected abstract fun setLeLongSafe(value: Long, offset: Long)

    final override fun setLeLong(value: Long, offset: Long) {
        checkWritable()
        checkBounds(offset, 2)
        setLeLongSafe(value, offset)
    }

    protected abstract fun setBeLongSafe(value: Long, offset: Long)

    final override fun setBeLong(value: Long, offset: Long) {
        checkWritable()
        checkBounds(offset, 2)
        setBeLongSafe(value, offset)
    }

    protected abstract fun setFloatSafe(value: Float, offset: Long)

    final override fun setFloat(value: Float, offset: Long) {
        checkWritable()
        checkBounds(offset, 2)
        setFloatSafe(value, offset)
    }

    protected abstract fun setLeFloatSafe(value: Float, offset: Long)

    final override fun setLeFloat(value: Float, offset: Long) {
        checkWritable()
        checkBounds(offset, 2)
        setLeFloatSafe(value, offset)
    }

    protected abstract fun setBeFloatSafe(value: Float, offset: Long)

    final override fun setBeFloat(value: Float, offset: Long) {
        checkWritable()
        checkBounds(offset, 2)
        setBeFloatSafe(value, offset)
    }

    protected abstract fun setDoubleSafe(value: Double, offset: Long)

    final override fun setDouble(value: Double, offset: Long) {
        checkWritable()
        checkBounds(offset, 2)
        setDoubleSafe(value, offset)
    }

    protected abstract fun setLeDoubleSafe(value: Double, offset: Long)

    final override fun setLeDouble(value: Double, offset: Long) {
        checkWritable()
        checkBounds(offset, 2)
        setLeDoubleSafe(value, offset)
    }

    protected abstract fun setBeDoubleSafe(value: Double, offset: Long)

    final override fun setBeDouble(value: Double, offset: Long) {
        checkWritable()
        checkBounds(offset, 2)
        setBeDoubleSafe(value, offset)
    }
}
