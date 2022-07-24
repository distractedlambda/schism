package org.schism.math

import org.schism.memory.NativeAddress
import org.schism.memory.getUByte
import org.schism.memory.getUInt
import org.schism.memory.getULong
import org.schism.memory.getUShort
import org.schism.memory.minus
import org.schism.memory.plus
import org.schism.memory.readUByte
import org.schism.memory.readUInt
import org.schism.memory.readULong
import org.schism.memory.readUShort
import java.security.SecureRandom
import java.util.Objects.checkFromIndexSize

/**
 * https://github.com/tkaitchuck/aHash
 */
public object AHash {
    public fun initial(): ULong {
        return INITIAL_STATE
    }

    public fun fold(state: ULong, data: ULong): ULong {
        return (data xor state) foldedMultiply MULTIPLE
    }

    public fun fold(state: ULong, data0: ULong, data1: ULong): ULong {
        val combined = (data0 xor EXTRA_KEYS_0) foldedMultiply (data1 xor EXTRA_KEYS_1)
        return ((state + PAD) xor combined).rotateLeft(ROT)
    }

    public fun fold(state: ULong, data: ByteArray, dataOffset: Int, dataSize: Int): ULong {
        checkFromIndexSize(dataOffset, dataSize, data.size)

        val dataEnd = dataOffset + dataSize
        var newState = (state + dataSize.toUInt().toULong()) * MULTIPLE

        if (dataSize > 8) {
            if (dataSize > 16) {
                newState = fold(newState, data.getULong(dataEnd - 16), data.getULong(dataEnd - 8))
                var nextOffset = dataOffset
                while (dataEnd - nextOffset > 16) {
                    newState = fold(newState, data.getULong(nextOffset), data.getULong(nextOffset + 8))
                    nextOffset += 16
                }
            } else {
                newState = fold(newState, data.getULong(dataOffset), data.getULong(dataEnd - 8))
            }
        } else {
            val part0: ULong
            val part1: ULong

            if (dataSize > 2) {
                if (dataSize > 4) {
                    part0 = data.getUInt(dataOffset).toULong()
                    part1 = data.getUInt(dataEnd - 4).toULong()
                } else {
                    part0 = data.getUShort(dataOffset).toULong()
                    part1 = data.getUByte(dataEnd - 1).toULong()
                }
            } else {
                if (dataSize > 0) {
                    part0 = data.getUByte(dataOffset).toULong()
                    part1 = part0
                } else {
                    part0 = 0u
                    part1 = 0u
                }
            }

            newState = fold(newState, part0, part1)
        }

        return newState
    }

    public fun fold(state: ULong, data: NativeAddress, dataSize: Long): ULong {
        val dataEnd = data + dataSize
        var newState = (state + dataSize.toULong()) * MULTIPLE

        if (dataSize > 8) {
            if (dataSize > 16) {
                newState = fold(newState, (dataEnd - 16).readULong(), (dataEnd - 8).readULong())
                var nextData = data
                while (dataEnd - nextData > 16) {
                    newState = fold(newState, nextData.readULong(), (nextData + 8).readULong())
                    nextData += 16
                }
            } else {
                newState = fold(newState, data.readULong(), (dataEnd - 8).readULong())
            }
        } else {
            val part0: ULong
            val part1: ULong

            if (dataSize > 2) {
                if (dataSize > 4) {
                    part0 = data.readUInt().toULong()
                    part1 = (dataEnd - 4).readUInt().toULong()
                } else {
                    part0 = data.readUShort().toULong()
                    part1 = (dataEnd - 1).readUByte().toULong()
                }
            } else {
                if (dataSize > 0) {
                    part0 = data.readUByte().toULong()
                    part1 = part0
                } else {
                    part0 = 0u
                    part1 = 0u
                }
            }

            newState = fold(newState, part0, part1)
        }

        return newState
    }

    public fun finish(state: ULong): ULong {
        return (state foldedMultiply PAD).rotateLeft((state and 63u).toInt())
    }
}

private const val MULTIPLE = 6364136223846793005UL
private const val ROT = 23

private val RANDOM_SEED = SecureRandom.getInstanceStrong().generateSeed(32)

private val INITIAL_STATE = RANDOM_SEED.getULong(0)
private val PAD = RANDOM_SEED.getULong(8)
private val EXTRA_KEYS_0 = RANDOM_SEED.getULong(16)
private val EXTRA_KEYS_1 = RANDOM_SEED.getULong(24)
