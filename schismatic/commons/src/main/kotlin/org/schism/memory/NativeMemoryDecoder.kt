package org.schism.memory

import java.lang.ref.Reference.reachabilityFence
import java.nio.BufferUnderflowException

internal class NativeMemoryDecoder(
    private var nextSrc: NativeAddress,
    private val end: NativeAddress,
    private val attachment: Any?,
) : MemoryDecoder {
    override val position: Long get() {
        return nextSrc.toBits()
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

    override fun nextChar(): Char {
        return advance(2) { src ->
            src.readChar()
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

    override fun nextShort(): Short {
        return advance(2) { src ->
            src.readShort()
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

    override fun nextInt(): Int {
        return advance(4) { src ->
            src.readInt()
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

    override fun nextLong(): Long {
        return advance(8) { src ->
            src.readLong()
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

    override fun nextFloat(): Float {
        return advance(4) { src ->
            src.readFloat()
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

    override fun nextDouble(): Double {
        return advance(8) { src ->
            src.readDouble()
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
