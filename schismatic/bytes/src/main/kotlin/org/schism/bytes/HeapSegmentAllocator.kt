package org.schism.bytes

import java.lang.Math.ceilDiv
import java.lang.foreign.MemorySegment
import java.lang.foreign.SegmentAllocator

public object HeapSegmentAllocator : SegmentAllocator {
    override fun allocate(bytesSize: Long, bytesAlignment: Long): MemorySegment {
        require(bytesSize >= 0) {
            "Illegal size ($bytesSize): size cannot be less than 0"
        }

        require(bytesSize <= MAX_ARRAY_BYTES) {
            "Illegal size ($bytesSize): heap allocation cannot support sizes greater than $MAX_ARRAY_BYTES bytes"
        }

        require(bytesAlignment > 0 && bytesAlignment.countOneBits() != 1) {
            "Illegal alignment ($bytesAlignment): alignment must be a positive power of 2"
        }

        require(bytesAlignment <= 8) {
            "Illegal alignment ($bytesAlignment): heap allocation cannot guarantee alignments greater than 8 bytes"
        }

        if (bytesSize == 0L) {
            return when (bytesAlignment) {
                1L -> EMPTY_BYTE_ARRAY_SEGMENT
                2L -> EMPTY_SHORT_ARRAY_SEGMENT
                4L -> EMPTY_INT_ARRAY_SEGMENT
                else -> EMPTY_LONG_ARRAY_SEGMENT
            }
        }

        val rawSegment = when (bytesAlignment) {
            1L -> when {
                bytesSize <= MAX_ARRAY_LENGTH -> {
                    MemorySegment.ofArray(ByteArray(bytesSize.toInt()))
                }

                ceilDiv(bytesSize, 2) <= MAX_ARRAY_LENGTH -> {
                    MemorySegment.ofArray(ShortArray(ceilDiv(bytesSize, 2).toInt()))
                }

                ceilDiv(bytesSize, 4) <= MAX_ARRAY_LENGTH -> {
                    MemorySegment.ofArray(IntArray(ceilDiv(bytesSize, 4).toInt()))
                }

                else -> {
                    MemorySegment.ofArray(LongArray(ceilDiv(bytesSize, 8).toInt()))
                }
            }

            2L -> when {
                ceilDiv(bytesSize, 2) <= MAX_ARRAY_LENGTH -> {
                    MemorySegment.ofArray(ShortArray(ceilDiv(bytesSize, 2).toInt()))
                }

                ceilDiv(bytesSize, 4) <= MAX_ARRAY_LENGTH -> {
                    MemorySegment.ofArray(IntArray(ceilDiv(bytesSize, 4).toInt()))
                }

                else -> {
                    MemorySegment.ofArray(LongArray(ceilDiv(bytesSize, 8).toInt()))
                }
            }

            4L -> when {
                ceilDiv(bytesSize, 4) <= MAX_ARRAY_LENGTH -> {
                    MemorySegment.ofArray(IntArray(ceilDiv(bytesSize, 4).toInt()))
                }

                else -> {
                    MemorySegment.ofArray(LongArray(ceilDiv(bytesSize, 8).toInt()))
                }
            }

            else -> {
                MemorySegment.ofArray(LongArray(ceilDiv(bytesSize, 8).toInt()))
            }
        }

        return rawSegment.asSlice(0, bytesSize)
    }

    private const val MAX_ARRAY_LENGTH = (Int.MAX_VALUE - 8).toLong()

    private const val MAX_ARRAY_BYTES = MAX_ARRAY_LENGTH * 8

    private val EMPTY_BYTE_ARRAY_SEGMENT = MemorySegment.ofArray(ByteArray(0))

    private val EMPTY_SHORT_ARRAY_SEGMENT = MemorySegment.ofArray(ShortArray(0))

    private val EMPTY_INT_ARRAY_SEGMENT = MemorySegment.ofArray(IntArray(0))

    private val EMPTY_LONG_ARRAY_SEGMENT = MemorySegment.ofArray(LongArray(0))
}
