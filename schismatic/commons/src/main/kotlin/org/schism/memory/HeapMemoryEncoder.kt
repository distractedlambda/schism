package org.schism.memory

import java.nio.BufferOverflowException

internal class HeapMemoryEncoder(
    private val array: ByteArray,
    private var intPosition: Int,
    private val limit: Int,
) : MemoryEncoder {
    override val position: Long get() {
        return intPosition.toLong()
    }

    private inline fun advancing(count: Long, block: (offset: Int) -> Unit) {
        val offset = intPosition

        if (count > limit - offset) {
            throw BufferOverflowException()
        }

        block(offset)
        intPosition = offset + count.toInt()
    }

    override fun skip(count: Long) {
        advancing(count) {}
    }

    override fun putByte(value: Byte) {
        advancing(1) { offset ->
            array[offset] = value
        }
    }

    override fun putChar(value: Char) {
        advancing(2) { offset ->
            array.setChar(value, offset)
        }
    }

    override fun putLeChar(value: Char) {
        advancing(2) { offset ->
            array.setLeChar(value, offset)
        }
    }

    override fun putBeChar(value: Char) {
        advancing(2) { offset ->
            array.setBeChar(value, offset)
        }
    }

    override fun putShort(value: Short) {
        advancing(2) { offset ->
            array.setShort(value, offset)
        }
    }

    override fun putLeShort(value: Short) {
        advancing(2) { offset ->
            array.setLeShort(value, offset)
        }
    }

    override fun putBeShort(value: Short) {
        advancing(2) { offset ->
            array.setBeShort(value, offset)
        }
    }

    override fun putInt(value: Int) {
        advancing(4) { offset ->
            array.setInt(value, offset)
        }
    }

    override fun putLeInt(value: Int) {
        advancing(4) { offset ->
            array.setLeInt(value, offset)
        }
    }

    override fun putBeInt(value: Int) {
        advancing(4) { offset ->
            array.setBeInt(value, offset)
        }
    }

    override fun putLong(value: Long) {
        advancing(8) { offset ->
            array.setLong(value, offset)
        }
    }

    override fun putLeLong(value: Long) {
        advancing(8) { offset ->
            array.setLeLong(value, offset)
        }
    }

    override fun putBeLong(value: Long) {
        advancing(8) { offset ->
            array.setBeLong(value, offset)
        }
    }

    override fun putFloat(value: Float) {
        advancing(4) { offset ->
            array.setFloat(value, offset)
        }
    }

    override fun putLeFloat(value: Float) {
        advancing(4) { offset ->
            array.setLeFloat(value, offset)
        }
    }

    override fun putBeFloat(value: Float) {
        advancing(4) { offset ->
            array.setBeFloat(value, offset)
        }
    }

    override fun putDouble(value: Double) {
        advancing(8) { offset ->
            array.setDouble(value, offset)
        }
    }

    override fun putLeDouble(value: Double) {
        advancing(8) { offset ->
            array.setLeDouble(value, offset)
        }
    }

    override fun putBeDouble(value: Double) {
        advancing(8) { offset ->
            array.setBeDouble(value, offset)
        }
    }
}
