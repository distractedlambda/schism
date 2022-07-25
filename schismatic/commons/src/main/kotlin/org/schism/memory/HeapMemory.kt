package org.schism.memory

import org.schism.math.foldHashCode
import java.lang.System.identityHashCode
import java.lang.foreign.MemorySegment
import java.lang.foreign.MemorySession

internal class HeapMemory(
    private val array: ByteArray,
    private val arrayOffset: Int,
    size: Long,
    isReadable: Boolean,
    isWritable: Boolean,
) : AbstractMemory(size, isReadable, isWritable) {
    override val isNative: Boolean get() {
        return false
    }

    override val startAddress: NativeAddress get() {
        throw UnsupportedOperationException("Memory is not native")
    }

    private fun effectiveOffset(offset: Long): Int {
        return arrayOffset + offset.toInt()
    }

    override fun shallowReadOnlyCopy(): Memory {
        return HeapMemory(array, arrayOffset, size, isReadable = true, isWritable = false)
    }

    override fun copyToSafe(destination: ByteArray, destinationOffset: Int, copySize: Int) {
        array.copyInto(destination, destinationOffset, arrayOffset, arrayOffset + copySize)
    }

    override fun copyToSafe(destination: NativeAddress) {
        MemorySegment
            .ofAddress(destination.toMemoryAddress(), size, MemorySession.global())
            .copyFrom(MemorySegment.ofArray(array).asSlice(arrayOffset.toLong(), size))
    }

    override fun copyToSafe(destination: Memory) {
        destination.copyFrom(ReadOnlyByteArray(array), arrayOffset)
    }

    override fun copyFromSafe(source: ReadOnlyByteArray, sourceOffset: Int, copySize: Int) {
        source.copyInto(array, arrayOffset, sourceOffset, sourceOffset + copySize)
    }

    override fun copyFromSafe(source: NativeAddress) {
        MemorySegment
            .ofArray(array)
            .asSlice(arrayOffset.toLong(), size)
            .copyFrom(MemorySegment.ofAddress(source.toMemoryAddress(), size, MemorySession.global()))
    }

    override fun fillSafe(value: Byte) {
        array.fill(value, arrayOffset, effectiveOffset(size))
    }

    override fun encoderSafe(): MemoryEncoder {
        return HeapMemoryEncoder(array, arrayOffset, effectiveOffset(size))
    }

    override fun decoderSafe(): MemoryDecoder {
        return HeapMemoryDecoder(array, arrayOffset, effectiveOffset(size))
    }

    override fun sliceSafe(offset: Long, size: Long): Memory {
        return HeapMemory(array, effectiveOffset(offset), size, isReadable, isWritable)
    }

    override fun getByteSafe(offset: Long): Byte {
        return array[effectiveOffset(offset)]
    }

    override fun getShortSafe(offset: Long): Short {
        return array.getShort(effectiveOffset(offset))
    }

    override fun getLeShortSafe(offset: Long): Short {
        return array.getLeShort(effectiveOffset(offset))
    }

    override fun getBeShortSafe(offset: Long): Short {
        return array.getBeShort(effectiveOffset(offset))
    }

    override fun getIntSafe(offset: Long): Int {
        return array.getInt(effectiveOffset(offset))
    }

    override fun getLeIntSafe(offset: Long): Int {
        return array.getLeInt(effectiveOffset(offset))
    }

    override fun getBeIntSafe(offset: Long): Int {
        return array.getBeInt(effectiveOffset(offset))
    }

    override fun getLongSafe(offset: Long): Long {
        return array.getLong(effectiveOffset(offset))
    }

    override fun getLeLongSafe(offset: Long): Long {
        return array.getLeLong(effectiveOffset(offset))
    }

    override fun getBeLongSafe(offset: Long): Long {
        return array.getBeLong(effectiveOffset(offset))
    }

    override fun getFloatSafe(offset: Long): Float {
        return array.getFloat(effectiveOffset(offset))
    }

    override fun getLeFloatSafe(offset: Long): Float {
        return array.getLeFloat(effectiveOffset(offset))
    }

    override fun getBeFloatSafe(offset: Long): Float {
        return array.getBeFloat(effectiveOffset(offset))
    }

    override fun getDoubleSafe(offset: Long): Double {
        return array.getDouble(effectiveOffset(offset))
    }

    override fun getLeDoubleSafe(offset: Long): Double {
        return array.getLeDouble(effectiveOffset(offset))
    }

    override fun getBeDoubleSafe(offset: Long): Double {
        return array.getBeDouble(effectiveOffset(offset))
    }

    override fun setByteSafe(value: Byte, offset: Long) {
        array[effectiveOffset(offset)] = value
    }

    override fun setShortSafe(value: Short, offset: Long) {
        array.setShort(value, effectiveOffset(offset))
    }

    override fun setLeShortSafe(value: Short, offset: Long) {
        array.setLeShort(value, effectiveOffset(offset))
    }

    override fun setBeShortSafe(value: Short, offset: Long) {
        array.setBeShort(value, effectiveOffset(offset))
    }

    override fun setIntSafe(value: Int, offset: Long) {
        array.setInt(value, effectiveOffset(offset))
    }

    override fun setLeIntSafe(value: Int, offset: Long) {
        array.setLeInt(value, effectiveOffset(offset))
    }

    override fun setBeIntSafe(value: Int, offset: Long) {
        array.setBeInt(value, effectiveOffset(offset))
    }

    override fun setLongSafe(value: Long, offset: Long) {
        array.setLong(value, effectiveOffset(offset))
    }

    override fun setLeLongSafe(value: Long, offset: Long) {
        array.setLeLong(value, effectiveOffset(offset))
    }

    override fun setBeLongSafe(value: Long, offset: Long) {
        array.setBeLong(value, effectiveOffset(offset))
    }

    override fun setFloatSafe(value: Float, offset: Long) {
        array.setFloat(value, effectiveOffset(offset))
    }

    override fun setLeFloatSafe(value: Float, offset: Long) {
        array.setLeFloat(value, effectiveOffset(offset))
    }

    override fun setBeFloatSafe(value: Float, offset: Long) {
        array.setBeFloat(value, effectiveOffset(offset))
    }

    override fun setDoubleSafe(value: Double, offset: Long) {
        array.setDouble(value, effectiveOffset(offset))
    }

    override fun setLeDoubleSafe(value: Double, offset: Long) {
        array.setLeDouble(value, effectiveOffset(offset))
    }

    override fun setBeDoubleSafe(value: Double, offset: Long) {
        array.setBeDouble(value, effectiveOffset(offset))
    }

    override fun equals(other: Any?): Boolean {
        return other is HeapMemory
            && array === other.array
            && arrayOffset == other.arrayOffset
            && size == other.size
            && isReadable == other.isReadable
            && isWritable == other.isWritable
    }

    override fun hashCode(): Int {
        return identityHashCode(array) foldHashCode
            arrayOffset.hashCode() foldHashCode
            size.hashCode() foldHashCode
            isReadable.hashCode() foldHashCode
            isWritable.hashCode()
    }

    override fun toString(): String {
        val arrayLastIndex = arrayOffset + size.toInt() - 1
        return "Memory($array[$arrayOffset..${arrayLastIndex}], isReadable=$isReadable, isWritable=$isWritable)"
    }
}
