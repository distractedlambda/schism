package org.schism.memory

import java.nio.BufferUnderflowException

internal class HeapMemoryDecoder(
    private val array: ByteArray,
    private var intPosition: Int,
    private val limit: Int,
) : MemoryDecoder {
    override val position: Long get() {
        return intPosition.toLong()
    }

    private inline fun <R> advancing(count: Long, block: (offset: Int) -> R): R {
        val offset = intPosition

        if (count > limit - offset) {
            throw BufferUnderflowException()
        }

        return block(offset).also {
            intPosition = offset + count.toInt()
        }
    }

    override fun skip(count: Long) {
        advancing(count) {}
    }

    override fun nextByte(): Byte {
        return advancing(1) { offset ->
            array.readByte(offset)
        }
    }

    override fun nextNativeChar(): Char {
        return advancing(2) { offset ->
            array.readNativeChar(offset)
        }
    }

    override fun nextLeChar(): Char {
        return advancing(2) { offset ->
            array.readLeChar(offset)
        }
    }

    override fun nextBeChar(): Char {
        return advancing(2) { offset ->
            array.readBeChar(offset)
        }
    }

    override fun nextNativeShort(): Short {
        return advancing(2) { offset ->
            array.readNativeShort(offset)
        }
    }

    override fun nextLeShort(): Short {
        return advancing(2) { offset ->
            array.readLeShort(offset)
        }
    }

    override fun nextBeShort(): Short {
        return advancing(2) { offset ->
            array.readBeShort(offset)
        }
    }

    override fun nextNativeInt(): Int {
        return advancing(4) { offset ->
            array.readNativeInt(offset)
        }
    }

    override fun nextLeInt(): Int {
        return advancing(4) { offset ->
            array.readLeInt(offset)
        }
    }

    override fun nextBeInt(): Int {
        return advancing(4) { offset ->
            array.readBeInt(offset)
        }
    }

    override fun nextNativeLong(): Long {
        return advancing(8) { offset ->
            array.readNativeLong(offset)
        }
    }

    override fun nextLeLong(): Long {
        return advancing(8) { offset ->
            array.readLeLong(offset)
        }
    }

    override fun nextBeLong(): Long {
        return advancing(8) { offset ->
            array.readBeLong(offset)
        }
    }

    override fun nextNativeFloat(): Float {
        return advancing(4) { offset ->
            array.readNativeFloat(offset)
        }
    }

    override fun nextLeFloat(): Float {
        return advancing(4) { offset ->
            array.readLeFloat(offset)
        }
    }

    override fun nextBeFloat(): Float {
        return advancing(4) { offset ->
            array.readBeFloat(offset)
        }
    }

    override fun nextNativeDouble(): Double {
        return advancing(8) { offset ->
            array.readNativeDouble(offset)
        }
    }

    override fun nextLeDouble(): Double {
        return advancing(8) { offset ->
            array.readLeDouble(offset)
        }
    }

    override fun nextBeDouble(): Double {
        return advancing(8) { offset ->
            array.readBeDouble(offset)
        }
    }
}
