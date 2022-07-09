package org.schism.memory

import java.lang.ref.Reference.reachabilityFence
import java.nio.BufferUnderflowException

internal class NativeMemoryDecoder(
    private var nextSource: NativeAddress,
    private val end: NativeAddress,
    private val attachment: Any?,
) : MemoryDecoder {
    override val position: Long get() {
        return nextSource.toBits()
    }

    private inline fun <R> advance(count: Long, block: (source: NativeAddress) -> R): R {
        val source = nextSource

        if (count >= end - source) {
            throw BufferUnderflowException()
        }

        val result = try {
            block(source)
        } finally {
            reachabilityFence(attachment)
        }

        nextSource = source + count
        return result
    }

    override fun hasRemaining(): Boolean {
        return nextSource != end
    }

    override fun skip(count: Long) {
        advance(count) {}
    }

    override fun nextBytes(destination: Memory) {
        advance(destination.size) { source ->
            memcpy(destination, source)
        }
    }

    override fun nextByte(): Byte {
        return advance(1) { source ->
            source.readByte()
        }
    }

    override fun nextChar(): Char {
        return advance(2) { source ->
            source.readChar()
        }
    }

    override fun nextLeChar(): Char {
        return advance(2) { source ->
            source.readLeChar()
        }
    }

    override fun nextBeChar(): Char {
        return advance(2) { source ->
            source.readBeChar()
        }
    }

    override fun nextShort(): Short {
        return advance(2) { source ->
            source.readShort()
        }
    }

    override fun nextLeShort(): Short {
        return advance(2) { source ->
            source.readLeShort()
        }
    }

    override fun nextBeShort(): Short {
        return advance(2) { source ->
            source.readBeShort()
        }
    }

    override fun nextInt(): Int {
        return advance(4) { source ->
            source.readInt()
        }
    }

    override fun nextLeInt(): Int {
        return advance(4) { source ->
            source.readLeInt()
        }
    }

    override fun nextBeInt(): Int {
        return advance(4) { source ->
            source.readBeInt()
        }
    }

    override fun nextLong(): Long {
        return advance(8) { source ->
            source.readLong()
        }
    }

    override fun nextLeLong(): Long {
        return advance(8) { source ->
            source.readLeLong()
        }
    }

    override fun nextBeLong(): Long {
        return advance(8) { source ->
            source.readBeLong()
        }
    }

    override fun nextFloat(): Float {
        return advance(4) { source ->
            source.readFloat()
        }
    }

    override fun nextLeFloat(): Float {
        return advance(4) { source ->
            source.readLeFloat()
        }
    }

    override fun nextBeFloat(): Float {
        return advance(4) { source ->
            source.readBeFloat()
        }
    }

    override fun nextDouble(): Double {
        return advance(8) { source ->
            source.readDouble()
        }
    }

    override fun nextLeDouble(): Double {
        return advance(8) { source ->
            source.readLeDouble()
        }
    }

    override fun nextBeDouble(): Double {
        return advance(8) { source ->
            source.readBeDouble()
        }
    }
}
