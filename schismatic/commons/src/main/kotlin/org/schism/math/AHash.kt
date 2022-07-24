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
    private fun fold(state: ULong, data0: ULong, data1: ULong): ULong {
        val combined = (data0 xor EXTRA_KEYS_0) foldedMultiply (data1 xor EXTRA_KEYS_1)
        return ((state + PAD) xor combined).rotateLeft(ROT)
    }

    private fun finish(state: ULong): Int {
        val longHash = (state foldedMultiply PAD).rotateLeft((state and 63u).toInt())
        return longHash.toInt() xor (longHash shr 32).toInt()
    }

    public fun hashCode(data: ByteArray, offset: Int, size: Int): Int {
        checkFromIndexSize(offset, size, data.size)

        val end = offset + size
        var state = (INITIAL_STATE + size.toUInt().toULong()) * MULTIPLE

        if (size > 8) {
            if (size > 16) {
                state = fold(state, data.getULong(end - 16), data.getULong(end - 8))
                var nextOffset = offset
                while (end - nextOffset > 16) {
                    state = fold(state, data.getULong(nextOffset), data.getULong(nextOffset + 8))
                    nextOffset += 16
                }
            } else {
                state = fold(state, data.getULong(offset), data.getULong(end - 8))
            }
        } else {
            val part0: ULong
            val part1: ULong

            if (size > 2) {
                if (size > 4) {
                    part0 = data.getUInt(offset).toULong()
                    part1 = data.getUInt(end - 4).toULong()
                } else {
                    part0 = data.getUShort(offset).toULong()
                    part1 = data.getUByte(end - 1).toULong()
                }
            } else {
                if (size > 0) {
                    part0 = data.getUByte(offset).toULong()
                    part1 = part0
                } else {
                    part0 = 0u
                    part1 = 0u
                }
            }

            state = fold(state, part0, part1)
        }

        return finish(state)
    }

    public fun hashCode(data: NativeAddress, size: Long): Int {
        val end = data + size
        var state = (INITIAL_STATE + size.toULong()) * MULTIPLE

        if (size > 8) {
            if (size > 16) {
                state = fold(state, (end - 16).readULong(), (end - 8).readULong())
                var nextData = data
                while (end - nextData > 16) {
                    state = fold(state, nextData.readULong(), (nextData + 8).readULong())
                    nextData += 16
                }
            } else {
                state = fold(state, data.readULong(), (end - 8).readULong())
            }
        } else {
            val part0: ULong
            val part1: ULong

            if (size > 2) {
                if (size > 4) {
                    part0 = data.readUInt().toULong()
                    part1 = (end - 4).readUInt().toULong()
                } else {
                    part0 = data.readUShort().toULong()
                    part1 = (end - 1).readUByte().toULong()
                }
            } else {
                if (size > 0) {
                    part0 = data.readUByte().toULong()
                    part1 = part0
                } else {
                    part0 = 0u
                    part1 = 0u
                }
            }

            state = fold(state, part0, part1)
        }

        return finish(state)
    }
}

private const val MULTIPLE = 6364136223846793005UL
private const val ROT = 23

private val RANDOM_SEED = SecureRandom.getInstanceStrong().generateSeed(32)

private val INITIAL_STATE = RANDOM_SEED.getULong(0)
private val PAD = RANDOM_SEED.getULong(8)
private val EXTRA_KEYS_0 = RANDOM_SEED.getULong(16)
private val EXTRA_KEYS_1 = RANDOM_SEED.getULong(24)
