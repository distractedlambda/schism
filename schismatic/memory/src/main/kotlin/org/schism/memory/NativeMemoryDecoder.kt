package org.schism.memory

import java.lang.ref.Reference.reachabilityFence
import java.nio.BufferUnderflowException

internal class NativeMemoryDecoder(
    private var nextSrc: NativeAddress,
    private val end: NativeAddress,
    private val attachment: Any?,
) : MemoryDecoder {
    override val position: Long get() {
        return nextSrc.numericValue
    }

    private inline fun <R> advance(count: Long, block: (src: NativeAddress) -> R): R {
        val src = nextSrc

        if (count >= end - src) {
            throw BufferUnderflowException()
        }

        val result = try {
            block(src)
        } finally {
            reachabilityFence(attachment)
        }

        nextSrc = src + count
        return result
    }

    override fun skip(count: Long) {
        advance(count) {}
    }

    override fun nextByte(): Byte {
        return advance(1) { src ->
            src.readByte()
        }
    }

    override fun nextNativeChar(): Char {
        return advance(2) { src ->
            src.readNativeChar()
        }
    }

    override fun nextLeChar(): Char {
        return advance(2) { src ->
            src.readLeChar()
        }
    }

    override fun nextBeChar(): Char {
        return advance(2) { src ->
            src.readBeChar()
        }
    }

    override fun nextNativeShort(): Short {
        return advance(2) { src ->
            src.readNativeShort()
        }
    }

    override fun nextLeShort(): Short {
        return advance(2) { src ->
            src.readLeShort()
        }
    }

    override fun nextBeShort(): Short {
        return advance(2) { src ->
            src.readBeShort()
        }
    }

    override fun nextNativeInt(): Int {
        return advance(4) { src ->
            src.readNativeInt()
        }
    }

    override fun nextLeInt(): Int {
        return advance(4) { src ->
            src.readLeInt()
        }
    }

    override fun nextBeInt(): Int {
        return advance(4) { src ->
            src.readBeInt()
        }
    }

    override fun nextNativeLong(): Long {
        return advance(8) { src ->
            src.readNativeLong()
        }
    }

    override fun nextLeLong(): Long {
        return advance(8) { src ->
            src.readLeLong()
        }
    }

    override fun nextBeLong(): Long {
        return advance(8) { src ->
            src.readBeLong()
        }
    }

    override fun nextNativeFloat(): Float {
        return advance(4) { src ->
            src.readNativeFloat()
        }
    }

    override fun nextLeFloat(): Float {
        return advance(4) { src ->
            src.readLeFloat()
        }
    }

    override fun nextBeFloat(): Float {
        return advance(4) { src ->
            src.readBeFloat()
        }
    }

    override fun nextNativeDouble(): Double {
        return advance(8) { src ->
            src.readNativeDouble()
        }
    }

    override fun nextLeDouble(): Double {
        return advance(8) { src ->
            src.readLeDouble()
        }
    }

    override fun nextBeDouble(): Double {
        return advance(8) { src ->
            src.readBeDouble()
        }
    }
}
