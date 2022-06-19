package org.schism.foreign

import java.lang.Math.addExact

interface MeteredBufferEncoder : BufferEncoder {
    val bytesWritten: Long
}

fun BufferEncoder.metered(): MeteredBufferEncoder {
    return MeteredBufferEncoderWrapper(this)
}

private class MeteredBufferEncoderWrapper(private val inner: BufferEncoder) : MeteredBufferEncoder {
    override var bytesWritten = 0L
        private set

    private fun accumulateWritten(amount: Long) {
        bytesWritten = addExact(bytesWritten, amount)
    }

    override fun putUndefined(count: Long) {
        inner.putUndefined(count).also { accumulateWritten(count) }
    }

    override fun putByte(value: Byte) {
        inner.putByte(value).also { accumulateWritten(1) }
    }

    override fun putLeShort(value: Short) {
        inner.putLeShort(value).also { accumulateWritten(2) }
    }

    override fun putBeShort(value: Short) {
        inner.putBeShort(value).also { accumulateWritten(2) }
    }

    override fun putLeInt(value: Int) {
        inner.putLeInt(value).also { accumulateWritten(4) }
    }

    override fun putBeInt(value: Int) {
        inner.putBeInt(value).also { accumulateWritten(4) }
    }

    override fun putLeLong(value: Long) {
        inner.putLeLong(value).also { accumulateWritten(8) }
    }

    override fun putBeLong(value: Long) {
        inner.putBeLong(value).also { accumulateWritten(8) }
    }

    override fun putLeFloat(value: Float) {
        inner.putLeFloat(value).also { accumulateWritten(4) }
    }

    override fun putBeFloat(value: Float) {
        inner.putBeFloat(value).also { accumulateWritten(4) }
    }

    override fun putLeDouble(value: Double) {
        inner.putLeDouble(value).also { accumulateWritten(8) }
    }

    override fun putBeDouble(value: Double) {
        inner.putBeDouble(value).also { accumulateWritten(8) }
    }
}
