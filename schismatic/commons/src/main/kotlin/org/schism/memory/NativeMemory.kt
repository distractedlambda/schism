package org.schism.memory

import org.schism.math.foldHashCode
import java.lang.ref.Reference.reachabilityFence
import java.util.Objects.checkFromIndexSize
import java.util.Objects.checkIndex

internal class NativeMemory : Memory {
    override val startAddress: NativeAddress
    override val size: Long
    private val flags: Int
    private val attachment: Any?

    constructor(startAddress: NativeAddress, size: Long, flags: Int, attachment: Any?) {
        this.startAddress = startAddress
        this.size = size
        this.flags = flags
        this.attachment = attachment
    }

    constructor(startAddress: NativeAddress, size: Long, flags: Int) {
        this.startAddress = startAddress
        this.size = size
        this.flags = flags
        this.attachment = this
    }

    override val isReadable: Boolean get() {
        return (flags and READABLE) != 0
    }

    override val isWritable: Boolean get() {
        return (flags and WRITABLE) != 0
    }

    override val isNative: Boolean get() {
        return true
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
            NativeMemory(startAddress, size, READABLE, attachment)
        }
    }

    override fun copyTo(destination: ByteArray, destinationOffset: Int) {
        checkReadable()
        try {
            memcpy(
                destination,
                destinationOffset,
                startAddress,
                minOf(size, (destination.size - destinationOffset).toLong()).toInt(),
            )
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun copyTo(destination: NativeAddress) {
        checkReadable()
        try {
            memcpy(destination, startAddress, size)
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun copyTo(destination: Memory) {
        checkReadable()
        try {
            memcpy(destination.slice(size = minOf(size, destination.size)), startAddress)
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun copyFrom(source: ByteArray, sourceOffset: Int) {
        checkWritable()
        try {
            memcpy(startAddress, source, sourceOffset, minOf(size, (source.size - sourceOffset).toLong()).toInt())
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun copyFrom(source: NativeAddress) {
        checkWritable()
        try {
            memcpy(startAddress, source, size)
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun fill(value: Byte) {
        checkWritable()
        try {
            memset(startAddress, value, size)
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun slice(offset: Long, size: Long): Memory {
        checkFromIndexSize(offset, size, this.size)
        return NativeMemory(startAddress + offset, size, flags, attachment)
    }

    override fun encoder(): MemoryEncoder {
        checkWritable()
        return NativeMemoryEncoder(startAddress, startAddress + size, attachment)
    }

    override fun decoder(): MemoryDecoder {
        checkReadable()
        return NativeMemoryDecoder(startAddress, startAddress + size, attachment)
    }

    override fun getByte(offset: Long): Byte {
        checkReadable()
        checkIndex(offset, size)

        try {
            return (startAddress + offset).readByte()
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun getChar(offset: Long): Char {
        checkReadable()
        checkFromIndexSize(offset, 2, size)

        try {
            return (startAddress + offset).readChar()
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun getLeChar(offset: Long): Char {
        checkReadable()
        checkFromIndexSize(offset, 2, size)

        try {
            return (startAddress + offset).readLeChar()
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun getBeChar(offset: Long): Char {
        checkReadable()
        checkFromIndexSize(offset, 2, size)

        try {
            return (startAddress + offset).readBeChar()
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun getShort(offset: Long): Short {
        checkReadable()
        checkFromIndexSize(offset, 2, size)

        try {
            return (startAddress + offset).readShort()
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun getLeShort(offset: Long): Short {
        checkReadable()
        checkFromIndexSize(offset, 2, size)

        try {
            return (startAddress + offset).readLeShort()
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun getBeShort(offset: Long): Short {
        checkReadable()
        checkFromIndexSize(offset, 2, size)

        try {
            return (startAddress + offset).readBeShort()
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun getInt(offset: Long): Int {
        checkReadable()
        checkFromIndexSize(offset, 4, size)

        try {
            return (startAddress + offset).readInt()
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun getLeInt(offset: Long): Int {
        checkReadable()
        checkFromIndexSize(offset, 4, size)

        try {
            return (startAddress + offset).readLeInt()
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun getBeInt(offset: Long): Int {
        checkReadable()
        checkFromIndexSize(offset, 4, size)

        try {
            return (startAddress + offset).readBeInt()
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun getLong(offset: Long): Long {
        checkReadable()
        checkFromIndexSize(offset, 8, size)

        try {
            return (startAddress + offset).readLong()
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun getLeLong(offset: Long): Long {
        checkReadable()
        checkFromIndexSize(offset, 8, size)

        try {
            return (startAddress + offset).readLeLong()
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun getBeLong(offset: Long): Long {
        checkReadable()
        checkFromIndexSize(offset, 8, size)

        try {
            return (startAddress + offset).readBeLong()
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun getFloat(offset: Long): Float {
        checkReadable()
        checkFromIndexSize(offset, 4, size)

        try {
            return (startAddress + offset).readFloat()
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun getLeFloat(offset: Long): Float {
        checkReadable()
        checkFromIndexSize(offset, 4, size)

        try {
            return (startAddress + offset).readLeFloat()
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun getBeFloat(offset: Long): Float {
        checkReadable()
        checkFromIndexSize(offset, 4, size)

        try {
            return (startAddress + offset).readBeFloat()
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun getDouble(offset: Long): Double {
        checkReadable()
        checkFromIndexSize(offset, 8, size)

        try {
            return (startAddress + offset).readDouble()
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun getLeDouble(offset: Long): Double {
        checkReadable()
        checkFromIndexSize(offset, 8, size)

        try {
            return (startAddress + offset).readLeDouble()
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun getBeDouble(offset: Long): Double {
        checkReadable()
        checkFromIndexSize(offset, 8, size)

        try {
            return (startAddress + offset).readBeDouble()
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun setByte(value: Byte, offset: Long) {
        checkWritable()
        checkIndex(offset, size)

        try {
            (startAddress + offset).writeByte(value)
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun setChar(value: Char, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 2, size)

        try {
            (startAddress + offset).writeChar(value)
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun setLeChar(value: Char, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 2, size)

        try {
            (startAddress + offset).writeLeChar(value)
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun setBeChar(value: Char, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 2, size)

        try {
            (startAddress + offset).writeBeChar(value)
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun setShort(value: Short, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 2, size)

        try {
            (startAddress + offset).writeShort(value)
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun setLeShort(value: Short, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 2, size)

        try {
            (startAddress + offset).writeLeShort(value)
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun setBeShort(value: Short, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 2, size)

        try {
            (startAddress + offset).writeBeShort(value)
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun setInt(value: Int, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 4, size)

        try {
            (startAddress + offset).writeInt(value)
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun setLeInt(value: Int, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 4, size)

        try {
            (startAddress + offset).writeLeInt(value)
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun setBeInt(value: Int, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 4, size)

        try {
            (startAddress + offset).writeBeInt(value)
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun setLong(value: Long, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 8, size)

        try {
            (startAddress + offset).writeLong(value)
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun setLeLong(value: Long, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 8, size)

        try {
            (startAddress + offset).writeLeLong(value)
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun setBeLong(value: Long, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 8, size)

        try {
            (startAddress + offset).writeBeLong(value)
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun setFloat(value: Float, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 4, size)

        try {
            (startAddress + offset).writeFloat(value)
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun setLeFloat(value: Float, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 4, size)

        try {
            (startAddress + offset).writeLeFloat(value)
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun setBeFloat(value: Float, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 4, size)

        try {
            (startAddress + offset).writeBeFloat(value)
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun setDouble(value: Double, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 8, size)

        try {
            (startAddress + offset).writeDouble(value)
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun setLeDouble(value: Double, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 8, size)

        try {
            (startAddress + offset).writeLeDouble(value)
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun setBeDouble(value: Double, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 8, size)

        try {
            (startAddress + offset).writeBeDouble(value)
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun equals(other: Any?): Boolean {
        return other is NativeMemory
            && startAddress == other.startAddress
            && size == other.size
            && flags == other.flags
    }

    override fun hashCode(): Int {
        return startAddress.hashCode() foldHashCode size.hashCode() foldHashCode flags.hashCode()
    }

    override fun toString(): String {
        val lastAddress = startAddress + (size - 1)
        return "Memory($startAddress..$lastAddress, isReadable=$isReadable, isWritable=$isWritable)"
    }

    companion object {
        const val READABLE = 0x1
        const val WRITABLE = 0x2
    }
}
