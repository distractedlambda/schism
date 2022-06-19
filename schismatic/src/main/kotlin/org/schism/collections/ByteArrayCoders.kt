@file:Suppress("NOTHING_TO_INLINE", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package org.schism.collections

import org.schism.foreign.BufferDecoder
import org.schism.foreign.BufferEncoder
import org.schism.math.toIntExact
import java.lang.invoke.MethodHandles.byteArrayViewVarHandle
import java.nio.ByteOrder.BIG_ENDIAN
import java.nio.ByteOrder.LITTLE_ENDIAN
import java.nio.ByteOrder.nativeOrder
import java.util.Objects.checkFromIndexSize
import java.util.Objects.checkFromToIndex
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.internal.InlineOnly

fun ByteArray.encoder(startIndex: Int, endIndex: Int = size): BufferEncoder {
    return ByteArrayEncoder(this, startIndex, endIndex)
}

fun ByteArray.decoder(startIndex: Int = 0, endIndex: Int = size): BufferDecoder {
    return ByteArrayDecoder(this, startIndex, endIndex)
}

@ExperimentalUnsignedTypes @InlineOnly
inline fun UByteArray.encoder(startIndex: Int = 0, endIndex: Int = size): BufferEncoder {
    return asByteArray().encoder(startIndex, endIndex)
}

@ExperimentalUnsignedTypes @InlineOnly
inline fun UByteArray.decoder(startIndex: Int = 0, endIndex: Int = size): BufferDecoder {
    return asByteArray().decoder(startIndex, endIndex)
}

private class ByteArrayDecoder(
    private val array: ByteArray,
    private var offset: Int,
    private val limit: Int,
) : BufferDecoder {
    init {
        checkFromToIndex(offset, limit, array.size)
    }

    override val position get() = offset.toLong()

    @OptIn(ExperimentalContracts::class)
    private inline fun <R> read(size: Int, block: (offset: Int) -> R): R {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }

        val offset = this.offset
        checkFromIndexSize(offset, size, limit)

        val result = block(offset)
        this.offset = offset + size

        return result
    }

    override fun skip(count: Long) {
        val offset = offset
        checkFromIndexSize(offset.toLong(), count, limit.toLong())
        this.offset = offset + count.toInt()
    }

    override fun nextByte(): Byte {
        return read(1) { offset ->
            array[offset]
        }
    }

    override fun nextLeShort(): Short {
        return read(2) { offset ->
            vhLeShort[array, offset] as Short
        }
    }

    override fun nextBeShort(): Short {
        return read(2) { offset ->
            vhBeShort[array, offset] as Short
        }
    }

    override fun nextNativeShort(): Short {
        return read(2) { offset ->
            vhNativeShort[array, offset] as Short
        }
    }

    override fun nextLeInt(): Int {
        return read(4) { offset ->
            vhLeInt[array, offset] as Int
        }
    }

    override fun nextBeInt(): Int {
        return read(4) { offset ->
            vhBeInt[array, offset] as Int
        }
    }

    override fun nextNativeInt(): Int {
        return read(4) { offset ->
            vhNativeInt[array, offset] as Int
        }
    }

    override fun nextLeLong(): Long {
        return read(8) { offset ->
            vhLeLong[array, offset] as Long
        }
    }

    override fun nextBeLong(): Long {
        return read(8) { offset ->
            vhBeLong[array, offset] as Long
        }
    }

    override fun nextNativeLong(): Long {
        return read(8) { offset ->
            vhNativeLong[array, offset] as Long
        }
    }

    override fun nextLeFloat(): Float {
        return read(4) { offset ->
            vhLeFloat[array, offset] as Float
        }
    }

    override fun nextBeFloat(): Float {
        return read(4) { offset ->
            vhBeFloat[array, offset] as Float
        }
    }

    override fun nextNativeFloat(): Float {
        return read(4) { offset ->
            vhNativeFloat[array, offset] as Float
        }
    }

    override fun nextLeDouble(): Double {
        return read(8) { offset ->
            vhLeDouble[array, offset] as Double
        }
    }

    override fun nextBeDouble(): Double {
        return read(8) { offset ->
            vhBeDouble[array, offset] as Double
        }
    }

    override fun nextNativeDouble(): Double {
        return read(8) { offset ->
            vhNativeDouble[array, offset] as Double
        }
    }
}

private class ByteArrayEncoder(
    private val array: ByteArray,
    private var offset: Int,
    private val limit: Int,
) : BufferEncoder {
    init {
        checkFromToIndex(offset, limit, array.size)
    }

    override val position get() = offset.toLong()

    @OptIn(ExperimentalContracts::class)
    private inline fun write(size: Int, block: (offset: Int) -> Unit) {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }

        val offset = this.offset
        checkFromIndexSize(offset, size, limit)

        block(offset)
        this.offset = offset + size
    }

    override fun skip(count: Long) {
        write(count.toIntExact()) {}
    }

    override fun putByte(value: Byte) {
        write(1) { offset ->
            array[offset] = value
        }
    }

    override fun putLeShort(value: Short) {
        write(2) { offset ->
            vhLeShort.set(array, offset, value)
        }
    }

    override fun putBeShort(value: Short) {
        write(2) { offset ->
            vhBeShort.set(array, offset, value)
        }
    }

    override fun putNativeShort(value: Short) {
        write(2) { offset ->
            vhNativeShort.set(array, offset, value)
        }
    }

    override fun putLeInt(value: Int) {
        write(4) { offset ->
            vhLeInt.set(array, offset, value)
        }
    }

    override fun putBeInt(value: Int) {
        write(4) { offset ->
            vhBeInt.set(array, offset, value)
        }
    }

    override fun putNativeInt(value: Int) {
        write(4) { offset ->
            vhNativeInt.set(array, offset, value)
        }
    }

    override fun putLeLong(value: Long) {
        write(8) { offset ->
            vhLeLong.set(array, offset, value)
        }
    }

    override fun putBeLong(value: Long) {
        write(8) { offset ->
            vhBeLong.set(array, offset, value)
        }
    }

    override fun putNativeLong(value: Long) {
        write(8) { offset ->
            vhNativeLong.set(array, offset, value)
        }
    }

    override fun putLeFloat(value: Float) {
        write(4) { offset ->
            vhLeFloat.set(array, offset, value)
        }
    }

    override fun putBeFloat(value: Float) {
        write(4) { offset ->
            vhBeFloat.set(array, offset, value)
        }
    }

    override fun putNativeFloat(value: Float) {
        write(4) { offset ->
            vhNativeFloat.set(array, offset, value)
        }
    }

    override fun putLeDouble(value: Double) {
        write(8) { offset ->
            vhLeDouble.set(array, offset, value)
        }
    }

    override fun putBeDouble(value: Double) {
        write(8) { offset ->
            vhBeDouble.set(array, offset, value)
        }
    }

    override fun putNativeDouble(value: Double) {
        write(8) { offset ->
            vhNativeDouble.set(array, offset, value)
        }
    }
}

private val vhLeShort = byteArrayViewVarHandle(Short::class.java, LITTLE_ENDIAN)
private val vhBeShort = byteArrayViewVarHandle(Short::class.java, BIG_ENDIAN)
private val vhNativeShort = byteArrayViewVarHandle(Short::class.java, nativeOrder())

private val vhLeInt = byteArrayViewVarHandle(Int::class.java, LITTLE_ENDIAN)
private val vhBeInt = byteArrayViewVarHandle(Int::class.java, BIG_ENDIAN)
private val vhNativeInt = byteArrayViewVarHandle(Int::class.java, nativeOrder())

private val vhLeLong = byteArrayViewVarHandle(Long::class.java, LITTLE_ENDIAN)
private val vhBeLong = byteArrayViewVarHandle(Long::class.java, BIG_ENDIAN)
private val vhNativeLong = byteArrayViewVarHandle(Long::class.java, nativeOrder())

private val vhLeFloat = byteArrayViewVarHandle(Float::class.java, LITTLE_ENDIAN)
private val vhBeFloat = byteArrayViewVarHandle(Float::class.java, BIG_ENDIAN)
private val vhNativeFloat = byteArrayViewVarHandle(Float::class.java, nativeOrder())

private val vhLeDouble = byteArrayViewVarHandle(Double::class.java, LITTLE_ENDIAN)
private val vhBeDouble = byteArrayViewVarHandle(Double::class.java, BIG_ENDIAN)
private val vhNativeDouble = byteArrayViewVarHandle(Double::class.java, nativeOrder())
