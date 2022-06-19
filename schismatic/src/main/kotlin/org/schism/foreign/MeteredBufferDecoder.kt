package org.schism.foreign

import java.lang.Math.addExact

interface MeteredBufferDecoder : BufferDecoder {
    val bytesRead: Long
}

fun BufferDecoder.metered(): MeteredBufferDecoder {
    return MeteredBufferDecoderWrapper(this)
}

private class MeteredBufferDecoderWrapper(private val inner: BufferDecoder) : MeteredBufferDecoder {
    override var bytesRead = 0L
        private set

    private fun accumulateRead(amount: Long) {
        bytesRead = addExact(bytesRead, amount)
    }

    override fun discardNext(count: Long) {
        inner.discardNext(count).also { accumulateRead(count) }
    }

    override fun nextByte(): Byte {
        return inner.nextByte().also { accumulateRead(1) }
    }

    override fun nextLeShort(): Short {
        return inner.nextLeShort().also { accumulateRead(2) }
    }

    override fun nextBeShort(): Short {
        return inner.nextBeShort().also { accumulateRead(2) }
    }

    override fun nextLeInt(): Int {
        return inner.nextLeInt().also { accumulateRead(4) }
    }

    override fun nextBeInt(): Int {
        return inner.nextBeInt().also { accumulateRead(4) }
    }

    override fun nextLeLong(): Long {
        return inner.nextLeLong().also { accumulateRead(8) }
    }

    override fun nextBeLong(): Long {
        return inner.nextBeLong().also { accumulateRead(8) }
    }

    override fun nextLeFloat(): Float {
        return inner.nextLeFloat().also { accumulateRead(4) }
    }

    override fun nextBeFloat(): Float {
        return inner.nextBeFloat().also { accumulateRead(4) }
    }

    override fun nextLeDouble(): Double {
        return inner.nextLeDouble().also { accumulateRead(8) }
    }

    override fun nextBeDouble(): Double {
        return inner.nextBeDouble().also { accumulateRead(8) }
    }
}
