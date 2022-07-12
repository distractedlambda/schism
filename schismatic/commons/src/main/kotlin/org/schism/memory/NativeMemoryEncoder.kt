package org.schism.memory

import java.lang.ref.Reference.reachabilityFence
import java.nio.BufferOverflowException

internal class NativeMemoryEncoder(
    private var nextDst: NativeAddress,
    private val end: NativeAddress,
    private val attachment: Any?,
) : MemoryEncoder {
    override val position: Long get() {
        return nextDst.toBits()
    }

    private inline fun advance(count: Long, block: (destination: NativeAddress) -> Unit) {
        val destination = nextDst

        if (count > end - destination) {
            throw BufferOverflowException()
        }

        try {
            block(destination)
        } finally {
            reachabilityFence(attachment)
        }

        nextDst = destination + count
    }

    override fun skip(count: Long) {
        advance(count) {}
    }

    override fun putBytes(source: Memory) {
        advance(source.size) { destination ->
            memcpy(destination, source)
        }
    }

    override fun putByte(value: Byte) {
        advance(1) { destination ->
            destination.writeByte(value)
        }
    }

    override fun putChar(value: Char) {
        advance(2) { destination ->
            destination.writeChar(value)
        }
    }

    override fun putLeChar(value: Char) {
        advance(2) { destination ->
            destination.writeLeChar(value)
        }
    }

    override fun putBeChar(value: Char) {
        advance(2) { destination ->
            destination.writeBeChar(value)
        }
    }

    override fun putShort(value: Short) {
        advance(2) { destination ->
            destination.writeShort(value)
        }
    }

    override fun putLeShort(value: Short) {
        advance(2) { destination ->
            destination.writeLeShort(value)
        }
    }

    override fun putBeShort(value: Short) {
        advance(2) { destination ->
            destination.writeBeShort(value)
        }
    }

    override fun putInt(value: Int) {
        advance(4) { destination ->
            destination.writeInt(value)
        }
    }

    override fun putLeInt(value: Int) {
        advance(4) { destination ->
            destination.writeLeInt(value)
        }
    }

    override fun putBeInt(value: Int) {
        advance(4) { destination ->
            destination.writeBeInt(value)
        }
    }

    override fun putLong(value: Long) {
        advance(8) { destination ->
            destination.writeLong(value)
        }
    }

    override fun putLeLong(value: Long) {
        advance(8) { destination ->
            destination.writeLeLong(value)
        }
    }

    override fun putBeLong(value: Long) {
        advance(8) { destination ->
            destination.writeBeLong(value)
        }
    }

    override fun putFloat(value: Float) {
        advance(4) { destination ->
            destination.writeFloat(value)
        }
    }

    override fun putLeFloat(value: Float) {
        advance(4) { destination ->
            destination.writeLeFloat(value)
        }
    }

    override fun putBeFloat(value: Float) {
        advance(4) { destination ->
            destination.writeBeFloat(value)
        }
    }

    override fun putDouble(value: Double) {
        advance(8) { destination ->
            destination.writeDouble(value)
        }
    }

    override fun putLeDouble(value: Double) {
        advance(8) { destination ->
            destination.writeLeDouble(value)
        }
    }

    override fun putBeDouble(value: Double) {
        advance(8) { destination ->
            destination.writeBeDouble(value)
        }
    }
}
