package org.schism.memory

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

    override fun readByte(offset: Long): Byte {
        checkReadable()
        checkIndex(offset, size)

        try {
            return (startAddress + offset).readByte()
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun readNativeChar(offset: Long): Char {
        checkReadable()
        checkFromIndexSize(offset, 2, size)

        try {
            return (startAddress + offset).readNativeChar()
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun readLeChar(offset: Long): Char {
        checkReadable()
        checkFromIndexSize(offset, 2, size)

        try {
            return (startAddress + offset).readLeChar()
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun readBeChar(offset: Long): Char {
        checkReadable()
        checkFromIndexSize(offset, 2, size)

        try {
            return (startAddress + offset).readBeChar()
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun readNativeShort(offset: Long): Short {
        checkReadable()
        checkFromIndexSize(offset, 2, size)

        try {
            return (startAddress + offset).readNativeShort()
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun readLeShort(offset: Long): Short {
        checkReadable()
        checkFromIndexSize(offset, 2, size)

        try {
            return (startAddress + offset).readLeShort()
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun readBeShort(offset: Long): Short {
        checkReadable()
        checkFromIndexSize(offset, 2, size)

        try {
            return (startAddress + offset).readBeShort()
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun readNativeInt(offset: Long): Int {
        checkReadable()
        checkFromIndexSize(offset, 4, size)

        try {
            return (startAddress + offset).readNativeInt()
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun readLeInt(offset: Long): Int {
        checkReadable()
        checkFromIndexSize(offset, 4, size)

        try {
            return (startAddress + offset).readLeInt()
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun readBeInt(offset: Long): Int {
        checkReadable()
        checkFromIndexSize(offset, 4, size)

        try {
            return (startAddress + offset).readBeInt()
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun readNativeLong(offset: Long): Long {
        checkReadable()
        checkFromIndexSize(offset, 8, size)

        try {
            return (startAddress + offset).readNativeLong()
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun readLeLong(offset: Long): Long {
        checkReadable()
        checkFromIndexSize(offset, 8, size)

        try {
            return (startAddress + offset).readLeLong()
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun readBeLong(offset: Long): Long {
        checkReadable()
        checkFromIndexSize(offset, 8, size)

        try {
            return (startAddress + offset).readBeLong()
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun readNativeFloat(offset: Long): Float {
        checkReadable()
        checkFromIndexSize(offset, 4, size)

        try {
            return (startAddress + offset).readNativeFloat()
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun readLeFloat(offset: Long): Float {
        checkReadable()
        checkFromIndexSize(offset, 4, size)

        try {
            return (startAddress + offset).readLeFloat()
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun readBeFloat(offset: Long): Float {
        checkReadable()
        checkFromIndexSize(offset, 4, size)

        try {
            return (startAddress + offset).readBeFloat()
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun readNativeDouble(offset: Long): Double {
        checkReadable()
        checkFromIndexSize(offset, 8, size)

        try {
            return (startAddress + offset).readNativeDouble()
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun readLeDouble(offset: Long): Double {
        checkReadable()
        checkFromIndexSize(offset, 8, size)

        try {
            return (startAddress + offset).readLeDouble()
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun readBeDouble(offset: Long): Double {
        checkReadable()
        checkFromIndexSize(offset, 8, size)

        try {
            return (startAddress + offset).readBeDouble()
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun writeByte(value: Byte, offset: Long) {
        checkWritable()
        checkIndex(offset, size)

        try {
            (startAddress + offset).writeByte(value)
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun writeNativeChar(value: Char, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 2, size)

        try {
            (startAddress + offset).writeNativeChar(value)
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun writeLeChar(value: Char, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 2, size)

        try {
            (startAddress + offset).writeLeChar(value)
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun writeBeChar(value: Char, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 2, size)

        try {
            (startAddress + offset).writeBeChar(value)
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun writeNativeShort(value: Short, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 2, size)

        try {
            (startAddress + offset).writeNativeShort(value)
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun writeLeShort(value: Short, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 2, size)

        try {
            (startAddress + offset).writeLeShort(value)
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun writeBeShort(value: Short, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 2, size)

        try {
            (startAddress + offset).writeBeShort(value)
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun writeNativeInt(value: Int, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 4, size)

        try {
            (startAddress + offset).writeNativeInt(value)
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun writeLeInt(value: Int, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 4, size)

        try {
            (startAddress + offset).writeLeInt(value)
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun writeBeInt(value: Int, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 4, size)

        try {
            (startAddress + offset).writeBeInt(value)
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun writeNativeLong(value: Long, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 8, size)

        try {
            (startAddress + offset).writeNativeLong(value)
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun writeLeLong(value: Long, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 8, size)

        try {
            (startAddress + offset).writeLeLong(value)
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun writeBeLong(value: Long, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 8, size)

        try {
            (startAddress + offset).writeBeLong(value)
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun writeNativeFloat(value: Float, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 4, size)

        try {
            (startAddress + offset).writeNativeFloat(value)
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun writeLeFloat(value: Float, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 4, size)

        try {
            (startAddress + offset).writeLeFloat(value)
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun writeBeFloat(value: Float, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 4, size)

        try {
            (startAddress + offset).writeBeFloat(value)
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun writeNativeDouble(value: Double, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 8, size)

        try {
            (startAddress + offset).writeNativeDouble(value)
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun writeLeDouble(value: Double, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 8, size)

        try {
            (startAddress + offset).writeLeDouble(value)
        } finally {
            reachabilityFence(attachment)
        }
    }

    override fun writeBeDouble(value: Double, offset: Long) {
        checkWritable()
        checkFromIndexSize(offset, 8, size)

        try {
            (startAddress + offset).writeBeDouble(value)
        } finally {
            reachabilityFence(attachment)
        }
    }

    companion object {
        const val READABLE = 0x1
        const val WRITABLE = 0x2
    }
}
