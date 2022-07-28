@file:Suppress("NOTHING_TO_INLINE")

package org.schism.foreign

import java.lang.foreign.MemorySegment

public class MemoryDecoder(public val segment: MemorySegment, public var offset: Long = 0) {
    public val remaining: Long get() {
        return segment.byteSize() - offset
    }

    public val hasRemaining: Boolean get() {
        return remaining != 0L
    }

    public fun advance(count: Long) {
        offset += count
    }

    public inline fun nextByte(): Byte {
        return segment.getByte(offset).also {
            advance(1)
        }
    }

    public fun nextBytes(destination: MemorySegment): MemorySegment {
        destination.copyFrom(segment.asSlice(offset, destination.byteSize()))
        advance(destination.byteSize())
        return destination
    }

    public inline fun nextUByte(): UByte {
        return segment.getUByte(offset).also {
            advance(1)
        }
    }

    public inline fun nextShort(): Short {
        return segment.getShort(offset).also {
            advance(2)
        }
    }

    public inline fun nextLeShort(): Short {
        return segment.getLeShort(offset).also {
            advance(2)
        }
    }

    public inline fun nextBeShort(): Short {
        return segment.getBeShort(offset).also {
            advance(2)
        }
    }

    public inline fun nextUShort(): UShort {
        return segment.getUShort(offset).also {
            advance(2)
        }
    }

    public inline fun nextLeUShort(): UShort {
        return segment.getLeUShort(offset).also {
            advance(2)
        }
    }

    public inline fun nextBeUShort(): UShort {
        return segment.getBeUShort(offset).also {
            advance(2)
        }
    }

    public inline fun nextChar(): Char {
        return segment.getChar(offset).also {
            advance(2)
        }
    }

    public inline fun nextLeChar(): Char {
        return segment.getLeChar(offset).also {
            advance(2)
        }
    }

    public inline fun nextBeChar(): Char {
        return segment.getBeChar(offset).also {
            advance(2)
        }
    }

    public inline fun nextInt(): Int {
        return segment.getInt(offset).also {
            advance(4)
        }
    }

    public inline fun nextLeInt(): Int {
        return segment.getLeInt(offset).also {
            advance(4)
        }
    }

    public inline fun nextBeInt(): Int {
        return segment.getBeInt(offset).also {
            advance(4)
        }
    }

    public inline fun nextUInt(): UInt {
        return segment.getUInt(offset).also {
            advance(4)
        }
    }

    public inline fun nextLeUInt(): UInt {
        return segment.getLeUInt(offset).also {
            advance(4)
        }
    }

    public inline fun nextBeUInt(): UInt {
        return segment.getBeUInt(offset).also {
            advance(4)
        }
    }

    public inline fun nextLong(): Long {
        return segment.getLong(offset).also {
            advance(8)
        }
    }

    public inline fun nextLeLong(): Long {
        return segment.getLeLong(offset).also {
            advance(8)
        }
    }

    public inline fun nextBeLong(): Long {
        return segment.getBeLong(offset).also {
            advance(8)
        }
    }

    public inline fun nextULong(): ULong {
        return segment.getULong(offset).also {
            advance(8)
        }
    }

    public inline fun nextLeULong(): ULong {
        return segment.getLeULong(offset).also {
            advance(8)
        }
    }

    public inline fun nextBeULong(): ULong {
        return segment.getBeULong(offset).also {
            advance(8)
        }
    }

    public inline fun nextFloat(): Float {
        return segment.getFloat(offset).also {
            advance(4)
        }
    }

    public inline fun nextLeFloat(): Float {
        return segment.getLeFloat(offset).also {
            advance(4)
        }
    }

    public inline fun nextBeFloat(): Float {
        return segment.getBeFloat(offset).also {
            advance(4)
        }
    }

    public inline fun nextDouble(): Double {
        return segment.getDouble(offset).also {
            advance(8)
        }
    }

    public inline fun nextLeDouble(): Double {
        return segment.getLeDouble(offset).also {
            advance(8)
        }
    }

    public inline fun nextBeDouble(): Double {
        return segment.getBeDouble(offset).also {
            advance(8)
        }
    }
}

public fun MemorySegment.decoder(offset: Long = 0): MemoryDecoder {
    return MemoryDecoder(this, offset)
}
