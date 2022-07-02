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
            array.writeByte(value, offset)
        }
    }

    override fun putNativeChar(value: Char) {
        advancing(2) { offset ->
            array.writeNativeChar(value, offset)
        }
    }

    override fun putLeChar(value: Char) {
        advancing(2) { offset ->
            array.writeLeChar(value, offset)
        }
    }

    override fun putBeChar(value: Char) {
        advancing(2) { offset ->
            array.writeBeChar(value, offset)
        }
    }

    override fun putNativeShort(value: Short) {
        advancing(2) { offset ->
            array.writeNativeShort(value, offset)
        }
    }

    override fun putLeShort(value: Short) {
        advancing(2) { offset ->
            array.writeLeShort(value, offset)
        }
    }

    override fun putBeShort(value: Short) {
        advancing(2) { offset ->
            array.writeBeShort(value, offset)
        }
    }

    override fun putNativeInt(value: Int) {
        advancing(4) { offset ->
            array.writeNativeInt(value, offset)
        }
    }

    override fun putLeInt(value: Int) {
        advancing(4) { offset ->
            array.writeLeInt(value, offset)
        }
    }

    override fun putBeInt(value: Int) {
        advancing(4) { offset ->
            array.writeBeInt(value, offset)
        }
    }

    override fun putNativeLong(value: Long) {
        advancing(8) { offset ->
            array.writeNativeLong(value, offset)
        }
    }

    override fun putLeLong(value: Long) {
        advancing(8) { offset ->
            array.writeLeLong(value, offset)
        }
    }

    override fun putBeLong(value: Long) {
        advancing(8) { offset ->
            array.writeBeLong(value, offset)
        }
    }

    override fun putNativeFloat(value: Float) {
        advancing(4) { offset ->
            array.writeNativeFloat(value, offset)
        }
    }

    override fun putLeFloat(value: Float) {
        advancing(4) { offset ->
            array.writeLeFloat(value, offset)
        }
    }

    override fun putBeFloat(value: Float) {
        advancing(4) { offset ->
            array.writeBeFloat(value, offset)
        }
    }

    override fun putNativeDouble(value: Double) {
        advancing(8) { offset ->
            array.writeNativeDouble(value, offset)
        }
    }

    override fun putLeDouble(value: Double) {
        advancing(8) { offset ->
            array.writeLeDouble(value, offset)
        }
    }

    override fun putBeDouble(value: Double) {
        advancing(8) { offset ->
            array.writeBeDouble(value, offset)
        }
    }
}
