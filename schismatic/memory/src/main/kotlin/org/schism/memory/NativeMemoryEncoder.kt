package org.schism.memory

import java.lang.ref.Reference.reachabilityFence
import java.nio.BufferOverflowException

internal class NativeMemoryEncoder(
    private var nextDst: NativeAddress,
    private val end: NativeAddress,
    private val attachment: Any?,
) : MemoryEncoder {
    override val position: Long get() {
        return nextDst.numericValue
    }

    private inline fun advance(count: Long, block: (dst: NativeAddress) -> Unit) {
        val dst = nextDst

        if (count >= end - dst) {
            throw BufferOverflowException()
        }

        try {
            block(dst)
        } finally {
            reachabilityFence(attachment)
        }

        nextDst = dst + count
    }

    override fun skip(count: Long) {
        advance(count) {}
    }

    override fun putByte(value: Byte) {
        advance(1) { dst ->
            dst.writeByte(value)
        }
    }

    override fun putNativeChar(value: Char) {
        advance(2) { dst ->
            dst.writeNativeChar(value)
        }
    }

    override fun putLeChar(value: Char) {
        advance(2) { dst ->
            dst.writeLeChar(value)
        }
    }

    override fun putBeChar(value: Char) {
        advance(2) { dst ->
            dst.writeBeChar(value)
        }
    }

    override fun putNativeShort(value: Short) {
        advance(2) { dst ->
            dst.writeNativeShort(value)
        }
    }

    override fun putLeShort(value: Short) {
        advance(2) { dst ->
            dst.writeLeShort(value)
        }
    }

    override fun putBeShort(value: Short) {
        advance(2) { dst ->
            dst.writeBeShort(value)
        }
    }

    override fun putNativeInt(value: Int) {
        advance(4) { dst ->
            dst.writeNativeInt(value)
        }
    }

    override fun putLeInt(value: Int) {
        advance(4) { dst ->
            dst.writeLeInt(value)
        }
    }

    override fun putBeInt(value: Int) {
        advance(4) { dst ->
            dst.writeBeInt(value)
        }
    }

    override fun putNativeLong(value: Long) {
        advance(8) { dst ->
            dst.writeNativeLong(value)
        }
    }

    override fun putLeLong(value: Long) {
        advance(8) { dst ->
            dst.writeLeLong(value)
        }
    }

    override fun putBeLong(value: Long) {
        advance(8) { dst ->
            dst.writeBeLong(value)
        }
    }

    override fun putNativeFloat(value: Float) {
        advance(4) { dst ->
            dst.writeNativeFloat(value)
        }
    }

    override fun putLeFloat(value: Float) {
        advance(4) { dst ->
            dst.writeLeFloat(value)
        }
    }

    override fun putBeFloat(value: Float) {
        advance(4) { dst ->
            dst.writeBeFloat(value)
        }
    }

    override fun putNativeDouble(value: Double) {
        advance(8) { dst ->
            dst.writeNativeDouble(value)
        }
    }

    override fun putLeDouble(value: Double) {
        advance(8) { dst ->
            dst.writeLeDouble(value)
        }
    }

    override fun putBeDouble(value: Double) {
        advance(8) { dst ->
            dst.writeBeDouble(value)
        }
    }
}
