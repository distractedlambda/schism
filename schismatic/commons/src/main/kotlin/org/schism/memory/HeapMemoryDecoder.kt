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
            array[offset]
        }
    }

    override fun nextChar(): Char {
        return advancing(2) { offset ->
            array.getChar(offset)
        }
    }

    override fun nextLeChar(): Char {
        return advancing(2) { offset ->
            array.getLeChar(offset)
        }
    }

    override fun nextBeChar(): Char {
        return advancing(2) { offset ->
            array.getBeChar(offset)
        }
    }

    override fun nextShort(): Short {
        return advancing(2) { offset ->
            array.getShort(offset)
        }
    }

    override fun nextLeShort(): Short {
        return advancing(2) { offset ->
            array.getLeShort(offset)
        }
    }

    override fun nextBeShort(): Short {
        return advancing(2) { offset ->
            array.getBeShort(offset)
        }
    }

    override fun nextInt(): Int {
        return advancing(4) { offset ->
            array.getInt(offset)
        }
    }

    override fun nextLeInt(): Int {
        return advancing(4) { offset ->
            array.getLeInt(offset)
        }
    }

    override fun nextBeInt(): Int {
        return advancing(4) { offset ->
            array.getBeInt(offset)
        }
    }

    override fun nextLong(): Long {
        return advancing(8) { offset ->
            array.getLong(offset)
        }
    }

    override fun nextLeLong(): Long {
        return advancing(8) { offset ->
            array.getLeLong(offset)
        }
    }

    override fun nextBeLong(): Long {
        return advancing(8) { offset ->
            array.getBeLong(offset)
        }
    }

    override fun nextFloat(): Float {
        return advancing(4) { offset ->
            array.getFloat(offset)
        }
    }

    override fun nextLeFloat(): Float {
        return advancing(4) { offset ->
            array.getLeFloat(offset)
        }
    }

    override fun nextBeFloat(): Float {
        return advancing(4) { offset ->
            array.getBeFloat(offset)
        }
    }

    override fun nextDouble(): Double {
        return advancing(8) { offset ->
            array.getDouble(offset)
        }
    }

    override fun nextLeDouble(): Double {
        return advancing(8) { offset ->
            array.getLeDouble(offset)
        }
    }

    override fun nextBeDouble(): Double {
        return advancing(8) { offset ->
            array.getBeDouble(offset)
        }
    }
}
