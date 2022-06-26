@file:Suppress("NOTHING_TO_INLINE")

package org.schism.foreign

import org.schism.collections.listBy
import java.lang.Math.multiplyExact
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker.nativeLinker
import java.lang.foreign.MemoryAddress
import java.lang.foreign.MemoryLayout
import java.lang.foreign.MemorySegment
import java.lang.foreign.MemorySession
import java.lang.foreign.ValueLayout
import java.lang.foreign.ValueLayout.ADDRESS
import java.lang.foreign.ValueLayout.JAVA_BYTE
import java.lang.foreign.ValueLayout.JAVA_DOUBLE
import java.lang.foreign.ValueLayout.JAVA_FLOAT
import java.lang.foreign.ValueLayout.JAVA_INT
import java.lang.foreign.ValueLayout.JAVA_LONG
import java.lang.foreign.ValueLayout.JAVA_SHORT
import java.lang.invoke.MethodHandle
import java.lang.ref.Cleaner
import java.lang.ref.Reference.reachabilityFence
import java.nio.ByteOrder.BIG_ENDIAN
import java.nio.ByteOrder.LITTLE_ENDIAN
import java.util.Objects.checkFromIndexSize
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

// FIXME: is the getAtIndex stuff consistent with element "strides" with over-aligned types?
class NativeBuffer private constructor(
    val start: NativeAddress,
    val size: Long,
    private val attachment: Any? = this,
) {
    val isUnmanaged: Boolean
        get() = attachment == null

    val isManaged: Boolean
        get() = attachment != null

    fun asDanglingSegment(): MemorySegment {
        return MemorySegment.ofAddress(start.toMemoryAddress(), size, MemorySession.global())
    }

    fun asSegment(session: MemorySession): MemorySegment {
        val attachment = attachment

        if (attachment != null) {
            session.addCloseAction {
                reachabilityFence(attachment)
            }
        }

        return MemorySegment.ofAddress(start.toMemoryAddress(), size, session)
    }

    fun slice(offset: ByteOffset = 0L.byteOffset, size: Long = this.size - offset.value): NativeBuffer {
        checkFromIndexSize(offset.value, size, this.size)
        return NativeBuffer(start + offset, size, attachment)
    }

    fun slice(layout: NativeLayout, offset: ByteOffset = 0L.byteOffset): NativeBuffer {
        return slice(offset, layout.size).also {
            it.start.requireAlignedTo(layout.alignment)
        }
    }

    fun slice(layout: MemoryLayout, offset: ByteOffset = 0L.byteOffset): NativeBuffer {
        return slice(layout.toNativeLayout(), offset)
    }

    fun slice(layout: NativeLayout, index: Index): NativeBuffer {
        return slice(index.toByteOffset(layout.stride), layout.size).also {
            it.start.requireAlignedTo(layout.alignment)
        }
    }

    fun slice(layout: MemoryLayout, index: Index): NativeBuffer {
        return slice(layout.toNativeLayout(), index)
    }

    fun sliceList(elementLayout: NativeLayout): List<NativeBuffer> {
        start.requireAlignedTo(elementLayout.alignment)
        require(size % elementLayout.stride == 0L)

        val sliceCount = size / elementLayout.stride
        require(sliceCount <= Int.MAX_VALUE)

        return listBy(sliceCount.toInt()) {
            slice(elementLayout, it.toLong().index)
        }
    }

    fun sliceList(elementLayout: MemoryLayout): List<NativeBuffer> {
        return sliceList(elementLayout.toNativeLayout())
    }

    fun fill(value: Byte) {
        keepAlive {
            asDanglingSegment().fill(value)
        }
    }

    fun copyFrom(source: MemorySegment) {
        keepAlive {
            asDanglingSegment().copyFrom(source)
        }
    }

    fun copyFrom(source: NativeBuffer) {
        source.keepAlive {
            copyFrom(source.asDanglingSegment())
        }
    }

    fun copyInto(destination: MemorySegment) {
        keepAlive {
            destination.copyFrom(asDanglingSegment())
        }
    }

    fun copyInto(destination: NativeBuffer) {
        destination.keepAlive {
            copyInto(destination.asDanglingSegment())
        }
    }

    inline operator fun get(layout: ValueLayout.OfByte, offset: ByteOffset = 0L.byteOffset): Byte {
        return keepAlive {
            asDanglingSegment()[layout, offset.value]
        }
    }

    inline operator fun set(layout: ValueLayout.OfByte, offset: ByteOffset = 0L.byteOffset, value: Byte) {
        keepAlive {
            asDanglingSegment().set(layout, offset.value, value)
        }
    }

    inline operator fun get(member: NativeMember<ValueLayout.OfByte>): Byte {
        return get(member.layout, member.offset)
    }

    inline operator fun set(member: NativeMember<ValueLayout.OfByte>, value: Byte) {
        set(member.layout, member.offset, value)
    }

    inline operator fun get(layout: ValueLayout.OfShort, offset: ByteOffset = 0L.byteOffset): Short {
        return keepAlive {
            asDanglingSegment()[layout, offset.value]
        }
    }

    inline operator fun set(layout: ValueLayout.OfShort, offset: ByteOffset = 0L.byteOffset, value: Short) {
        keepAlive {
            asDanglingSegment().set(layout, offset.value, value)
        }
    }

    inline operator fun get(layout: ValueLayout.OfShort, index: Index): Short {
        return keepAlive {
            asDanglingSegment().getAtIndex(layout, index.value)
        }
    }

    inline operator fun set(layout: ValueLayout.OfShort, index: Index, value: Short) {
        keepAlive {
            asDanglingSegment().setAtIndex(layout, index.value, value)
        }
    }

    inline operator fun get(member: NativeMember<ValueLayout.OfShort>): Short {
        return get(member.layout, member.offset)
    }

    inline operator fun set(member: NativeMember<ValueLayout.OfShort>, value: Short) {
        set(member.layout, member.offset, value)
    }

    inline operator fun get(layout: ValueLayout.OfChar, offset: ByteOffset = 0L.byteOffset): Char {
        return keepAlive {
            asDanglingSegment()[layout, offset.value]
        }
    }

    inline operator fun set(layout: ValueLayout.OfChar, offset: ByteOffset = 0L.byteOffset, value: Char) {
        keepAlive {
            asDanglingSegment().set(layout, offset.value, value)
        }
    }

    inline operator fun get(layout: ValueLayout.OfChar, index: Index): Char {
        return keepAlive {
            asDanglingSegment().getAtIndex(layout, index.value)
        }
    }

    inline operator fun set(layout: ValueLayout.OfChar, index: Index, value: Char) {
        keepAlive {
            asDanglingSegment().setAtIndex(layout, index.value, value)
        }
    }

    inline operator fun get(member: NativeMember<ValueLayout.OfChar>): Char {
        return get(member.layout, member.offset)
    }

    inline operator fun set(member: NativeMember<ValueLayout.OfChar>, value: Char) {
        set(member.layout, member.offset, value)
    }

    inline operator fun get(layout: ValueLayout.OfInt, offset: ByteOffset = 0L.byteOffset): Int {
        return keepAlive {
            asDanglingSegment()[layout, offset.value]
        }
    }

    inline operator fun set(layout: ValueLayout.OfInt, offset: ByteOffset = 0L.byteOffset, value: Int) {
        keepAlive {
            asDanglingSegment().set(layout, offset.value, value)
        }
    }

    inline operator fun get(layout: ValueLayout.OfInt, index: Index): Int {
        return keepAlive {
            asDanglingSegment().getAtIndex(layout, index.value)
        }
    }

    inline operator fun set(layout: ValueLayout.OfInt, index: Index, value: Int) {
        keepAlive {
            asDanglingSegment().setAtIndex(layout, index.value, value)
        }
    }

    inline operator fun get(member: NativeMember<ValueLayout.OfInt>): Int {
        return get(member.layout, member.offset)
    }

    inline operator fun set(member: NativeMember<ValueLayout.OfInt>, value: Int) {
        set(member.layout, member.offset, value)
    }

    inline operator fun get(layout: ValueLayout.OfLong, offset: ByteOffset = 0L.byteOffset): Long {
        return keepAlive {
            asDanglingSegment()[layout, offset.value]
        }
    }

    inline operator fun set(layout: ValueLayout.OfLong, offset: ByteOffset = 0L.byteOffset, value: Long) {
        keepAlive {
            asDanglingSegment().set(layout, offset.value, value)
        }
    }

    inline operator fun get(layout: ValueLayout.OfLong, index: Index): Long {
        return keepAlive {
            asDanglingSegment().getAtIndex(layout, index.value)
        }
    }

    inline operator fun set(layout: ValueLayout.OfLong, index: Index, value: Long) {
        keepAlive {
            asDanglingSegment().setAtIndex(layout, index.value, value)
        }
    }

    inline operator fun get(member: NativeMember<ValueLayout.OfLong>): Long {
        return get(member.layout, member.offset)
    }

    inline operator fun set(member: NativeMember<ValueLayout.OfLong>, value: Long) {
        set(member.layout, member.offset, value)
    }

    inline operator fun get(layout: ValueLayout.OfFloat, offset: ByteOffset = 0L.byteOffset): Float {
        return keepAlive {
            asDanglingSegment()[layout, offset.value]
        }
    }

    inline operator fun set(layout: ValueLayout.OfFloat, offset: ByteOffset = 0L.byteOffset, value: Float) {
        keepAlive {
            asDanglingSegment().set(layout, offset.value, value)
        }
    }

    inline operator fun get(layout: ValueLayout.OfFloat, index: Index): Float {
        return keepAlive {
            asDanglingSegment().getAtIndex(layout, index.value)
        }
    }

    inline operator fun set(layout: ValueLayout.OfFloat, index: Index, value: Float) {
        keepAlive {
            asDanglingSegment().setAtIndex(layout, index.value, value)
        }
    }

    inline operator fun get(member: NativeMember<ValueLayout.OfFloat>): Float {
        return get(member.layout, member.offset)
    }

    inline operator fun set(member: NativeMember<ValueLayout.OfFloat>, value: Float) {
        set(member.layout, member.offset, value)
    }

    inline operator fun get(layout: ValueLayout.OfDouble, offset: ByteOffset = 0L.byteOffset): Double {
        return keepAlive {
            asDanglingSegment()[layout, offset.value]
        }
    }

    inline operator fun set(layout: ValueLayout.OfDouble, offset: ByteOffset = 0L.byteOffset, value: Double) {
        keepAlive {
            asDanglingSegment().set(layout, offset.value, value)
        }
    }

    inline operator fun get(layout: ValueLayout.OfDouble, index: Index): Double {
        return keepAlive {
            asDanglingSegment().getAtIndex(layout, index.value)
        }
    }

    inline operator fun set(layout: ValueLayout.OfDouble, index: Index, value: Double) {
        keepAlive {
            asDanglingSegment().setAtIndex(layout, index.value, value)
        }
    }

    inline operator fun get(member: NativeMember<ValueLayout.OfDouble>): Double {
        return get(member.layout, member.offset)
    }

    inline operator fun set(member: NativeMember<ValueLayout.OfDouble>, value: Double) {
        set(member.layout, member.offset, value)
    }

    inline operator fun get(layout: ValueLayout.OfAddress, offset: ByteOffset = 0L.byteOffset): NativeAddress {
        return keepAlive {
            asDanglingSegment()[layout, offset.value].nativeAddress()
        }
    }

    inline operator fun set(layout: ValueLayout.OfAddress, offset: ByteOffset = 0L.byteOffset, value: NativeAddress) {
        keepAlive {
            asDanglingSegment().set(layout, offset.value, value.toMemoryAddress())
        }
    }

    inline operator fun get(layout: ValueLayout.OfAddress, index: Index): NativeAddress {
        return keepAlive {
            asDanglingSegment().getAtIndex(layout, index.value).nativeAddress()
        }
    }

    inline operator fun set(layout: ValueLayout.OfAddress, index: Index, value: NativeAddress) {
        keepAlive {
            asDanglingSegment().setAtIndex(layout, index.value, value.toMemoryAddress())
        }
    }

    inline operator fun get(member: NativeMember<ValueLayout.OfAddress>): NativeAddress {
        return get(member.layout, member.offset)
    }

    inline operator fun set(member: NativeMember<ValueLayout.OfAddress>, value: NativeAddress) {
        set(member.layout, member.offset, value)
    }

    @OptIn(ExperimentalContracts::class)
    inline fun <R> keepAlive(block: (NativeBuffer) -> R): R {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }

        try {
            return block(this)
        } finally {
            keepAlive()
        }
    }

    fun keepAlive() {
        reachabilityFence(attachment)
    }

    fun freeUnmanaged() {
        require(isUnmanaged)
        nativeFree(start.toMemoryAddress())
    }

    fun encoder(): BufferEncoder {
        return Encoder(start.toMemoryAddress(), size, attachment)
    }

    fun decoder(): BufferDecoder {
        return Decoder(start.toMemoryAddress(), size, attachment)
    }

    override fun equals(other: Any?): Boolean {
        return other is NativeBuffer && start == other.start && size == other.size
    }

    override fun hashCode(): Int {
        return 31 * start.hashCode() + size.hashCode()
    }

    override fun toString(): String {
        return "NativeBuffer(start=$start, size=$size)"
    }

    private class Encoder(
        private val address: MemoryAddress,
        private val limit: Long,
        private val attachment: Any?,
    ) : BufferEncoder {
        override var position = 0L
            private set

        @OptIn(ExperimentalContracts::class)
        private inline fun write(size: Long, block: (offset: Long) -> Unit) {
            contract {
                callsInPlace(block, InvocationKind.EXACTLY_ONCE)
            }

            val offset = position

            checkFromIndexSize(offset, size, limit)

            try {
                block(offset)
            } finally {
                reachabilityFence(attachment)
            }

            position = offset + size
        }

        override fun skip(count: Long) {
            write(count) {}
        }

        override fun putByte(value: Byte) {
            write(1) { offset ->
                address[JAVA_BYTE, offset] = value
            }
        }

        override fun putLeShort(value: Short) {
            write(2) { offset ->
                address[unalignedLeShort, offset] = value
            }
        }

        override fun putBeShort(value: Short) {
            write(2) { offset ->
                address[unalignedBeShort, offset] = value
            }
        }

        override fun putNativeShort(value: Short) {
            write(2) { offset ->
                address[unalignedNativeShort, offset] = value
            }
        }

        override fun putLeInt(value: Int) {
            write(4) { offset ->
                address[unalignedLeInt, offset] = value
            }
        }

        override fun putBeInt(value: Int) {
            write(4) { offset ->
                address[unalignedBeInt, offset] = value
            }
        }

        override fun putNativeInt(value: Int) {
            write(4) { offset ->
                address[unalignedNativeInt, offset] = value
            }
        }

        override fun putLeLong(value: Long) {
            write(8) { offset ->
                address[unalignedLeLong, offset] = value
            }
        }

        override fun putBeLong(value: Long) {
            write(8) { offset ->
                address[unalignedBeLong, offset] = value
            }
        }

        override fun putNativeLong(value: Long) {
            write(8) { offset ->
                address[unalignedNativeLong, offset] = value
            }
        }

        override fun putLeFloat(value: Float) {
            write(4) { offset ->
                address[unalignedLeFloat, offset] = value
            }
        }

        override fun putBeFloat(value: Float) {
            write(4) { offset ->
                address[unalignedBeFloat, offset] = value
            }
        }

        override fun putNativeFloat(value: Float) {
            write(4) { offset ->
                address[unalignedNativeFloat, offset] = value
            }
        }

        override fun putLeDouble(value: Double) {
            write(8) { offset ->
                address[unalignedLeDouble, offset] = value
            }
        }

        override fun putBeDouble(value: Double) {
            write(8) { offset ->
                address[unalignedBeDouble, offset] = value
            }
        }

        override fun putNativeDouble(value: Double) {
            write(8) { offset ->
                address[unalignedNativeDouble, offset] = value
            }
        }
    }

    private class Decoder(
        private val address: MemoryAddress,
        private val limit: Long,
        private val attachment: Any?,
    ) : BufferDecoder {
        override var position = 0L
            private set

        @OptIn(ExperimentalContracts::class)
        private inline fun <R> read(size: Long, block: (offset: Long) -> R): R {
            contract {
                callsInPlace(block, InvocationKind.EXACTLY_ONCE)
            }

            val offset = position

            checkFromIndexSize(offset, size, limit)

            val result = try {
                block(offset)
            } finally {
                reachabilityFence(attachment)
            }

            position = offset + size
            return result
        }

        override fun skip(count: Long) {
            read(count) {}
        }

        override fun nextByte(): Byte {
            return read(1) { offset ->
                address[JAVA_BYTE, offset]
            }
        }

        override fun nextLeShort(): Short {
            return read(2) { offset ->
                address[unalignedLeShort, offset]
            }
        }

        override fun nextBeShort(): Short {
            return read(2) { offset ->
                address[unalignedBeShort, offset]
            }
        }

        override fun nextNativeShort(): Short {
            return read(2) { offset ->
                address[unalignedNativeShort, offset]
            }
        }

        override fun nextLeInt(): Int {
            return read(4) { offset ->
                address[unalignedLeInt, offset]
            }
        }

        override fun nextBeInt(): Int {
            return read(4) { offset ->
                address[unalignedBeInt, offset]
            }
        }

        override fun nextNativeInt(): Int {
            return read(4) { offset ->
                address[unalignedNativeInt, offset]
            }
        }

        override fun nextLeLong(): Long {
            return read(8) { offset ->
                address[unalignedLeLong, offset]
            }
        }

        override fun nextBeLong(): Long {
            return read(8) { offset ->
                address[unalignedBeLong, offset]
            }
        }

        override fun nextNativeLong(): Long {
            return read(8) { offset ->
                address[unalignedNativeLong, offset]
            }
        }

        override fun nextLeFloat(): Float {
            return read(4) { offset ->
                address[unalignedLeFloat, offset]
            }
        }

        override fun nextBeFloat(): Float {
            return read(4) { offset ->
                address[unalignedBeFloat, offset]
            }
        }

        override fun nextNativeFloat(): Float {
            return read(4) { offset ->
                address[unalignedNativeFloat, offset]
            }
        }

        override fun nextLeDouble(): Double {
            return read(8) { offset ->
                address[unalignedLeDouble, offset]
            }
        }

        override fun nextBeDouble(): Double {
            return read(8) { offset ->
                address[unalignedBeDouble, offset]
            }
        }

        override fun nextNativeDouble(): Double {
            return read(8) { offset ->
                address[unalignedNativeDouble, offset]
            }
        }
    }

    companion object {
        val empty = NativeBuffer(NativeAddress.NULL, 0, null)

        fun unmanaged(start: NativeAddress, size: Long): NativeBuffer {
            require(size >= 0L)
            return NativeBuffer(start, size, null)
        }

        fun unmanagedArray(start: NativeAddress, element: NativeLayout, elementCount: Long): NativeBuffer {
            require(elementCount >= 0L)
            return NativeBuffer(start, multiplyExact(element.stride, elementCount), null)
        }

        fun unmanagedArray(start: NativeAddress, element: MemoryLayout, elementCount: Long): NativeBuffer {
            return unmanagedArray(start, element.toNativeLayout(), elementCount)
        }

        fun unmanagedArrayElements(
            start: NativeAddress,
            element: NativeLayout,
            elementCount: Int,
        ): List<NativeBuffer> {
            return unmanagedArray(start, element, elementCount.toLong()).sliceList(element)
        }

        fun unmanagedArrayElements(
            start: NativeAddress,
            element: MemoryLayout,
            elementCount: Int,
        ): List<NativeBuffer> {
            return unmanagedArrayElements(start, element.toNativeLayout(), elementCount)
        }

        fun allocateUnmanagedUninitialized(size: Long): NativeBuffer {
            require(size > 0)

            val start = (nativeMalloc(size) as MemoryAddress).nativeAddress()

            if (start.isNULL()) {
                throw OutOfMemoryError("malloc() returned NULL")
            }

            return NativeBuffer(start, size, null)
        }

        fun allocateUnmanagedUninitialized(layout: NativeLayout): NativeBuffer {
            require(layout.alignment <= ADDRESS.byteAlignment())
            return allocateUnmanagedUninitialized(layout.size)
        }

        fun allocateUnmanagedUninitialized(layout: MemoryLayout): NativeBuffer {
            return allocateUnmanagedUninitialized(layout.toNativeLayout())
        }

        fun allocateUnmanaged(size: Long): NativeBuffer {
            return allocateUnmanagedUninitialized(size).apply { fill(0) }
        }

        fun allocateUnmanaged(layout: NativeLayout): NativeBuffer {
            return allocateUnmanagedUninitialized(layout).apply { fill(0) }
        }

        fun allocateUnmanaged(layout: MemoryLayout): NativeBuffer {
            return allocateUnmanagedUninitialized(layout).apply { fill(0) }
        }

        @OptIn(ExperimentalContracts::class)
        inline fun <R> withUnmanagedUninitialized(size: Long, block: (NativeBuffer) -> R): R {
            contract {
                callsInPlace(block, InvocationKind.EXACTLY_ONCE)
            }

            val buffer = allocateUnmanagedUninitialized(size)

            try {
                return block(buffer)
            } finally {
                buffer.freeUnmanaged()
            }
        }

        @OptIn(ExperimentalContracts::class)
        inline fun <R> withUnmanagedUninitialized(layout: NativeLayout, block: (NativeBuffer) -> R): R {
            contract {
                callsInPlace(block, InvocationKind.EXACTLY_ONCE)
            }

            val buffer = allocateUnmanagedUninitialized(layout)

            try {
                return block(buffer)
            } finally {
                buffer.freeUnmanaged()
            }
        }

        @OptIn(ExperimentalContracts::class)
        inline fun <R> withUnmanagedUninitialized(layout: MemoryLayout, block: (NativeBuffer) -> R): R {
            contract {
                callsInPlace(block, InvocationKind.EXACTLY_ONCE)
            }

            val buffer = allocateUnmanagedUninitialized(layout)

            try {
                return block(buffer)
            } finally {
                buffer.freeUnmanaged()
            }
        }

        @OptIn(ExperimentalContracts::class)
        inline fun <R> withUnmanaged(size: Long, block: (NativeBuffer) -> R): R {
            contract {
                callsInPlace(block, InvocationKind.EXACTLY_ONCE)
            }

            val buffer = allocateUnmanaged(size)

            try {
                return block(buffer)
            } finally {
                buffer.freeUnmanaged()
            }
        }

        @OptIn(ExperimentalContracts::class)
        inline fun <R> withUnmanaged(layout: NativeLayout, block: (NativeBuffer) -> R): R {
            contract {
                callsInPlace(block, InvocationKind.EXACTLY_ONCE)
            }

            val buffer = allocateUnmanaged(layout)

            try {
                return block(buffer)
            } finally {
                buffer.freeUnmanaged()
            }
        }

        @OptIn(ExperimentalContracts::class)
        inline fun <R> withUnmanaged(layout: MemoryLayout, block: (NativeBuffer) -> R): R {
            contract {
                callsInPlace(block, InvocationKind.EXACTLY_ONCE)
            }

            val buffer = allocateUnmanaged(layout)

            try {
                return block(buffer)
            } finally {
                buffer.freeUnmanaged()
            }
        }

        fun allocateUninitialized(size: Long): NativeBuffer {
            require(size > 0)

            val start = (nativeMalloc(size) as MemoryAddress).nativeAddress()

            if (start.isNULL()) {
                throw OutOfMemoryError("malloc() returned NULL")
            }

            return NativeBuffer(start, size).also {
                freeCleaner.register(it, Free(start))
            }
        }

        fun allocateUninitialized(layout: NativeLayout): NativeBuffer {
            require(layout.alignment <= ADDRESS.byteAlignment())
            return allocateUninitialized(layout.alignment)
        }

        fun allocateUninitialized(layout: MemoryLayout): NativeBuffer {
            return allocateUninitialized(layout.toNativeLayout())
        }

        fun allocate(size: Long): NativeBuffer {
            return allocateUninitialized(size).apply { fill(0) }
        }

        fun allocate(layout: NativeLayout): NativeBuffer {
            return allocateUninitialized(layout).apply { fill(0) }
        }

        fun allocate(layout: MemoryLayout): NativeBuffer {
            return allocateUninitialized(layout).apply { fill(0) }
        }

        private class Free(private val address: NativeAddress) : Runnable {
            override fun run() {
                nativeFree(address.toMemoryAddress())
            }
        }

        private val freeCleaner = Cleaner.create {
            Thread.ofPlatform().name("NativeBuffer free() cleaner").unstarted(it)
        }

        private val nativeMalloc: MethodHandle
        private val nativeFree: MethodHandle

        init {
            val linker = nativeLinker()
            val lookup = linker.defaultLookup()

            nativeMalloc = linker.downcallHandle(
                lookup.lookup("malloc").orElseThrow(),
                FunctionDescriptor.of(ADDRESS, JAVA_LONG), // FIXME: handle 32-bit?
            )

            nativeFree = linker.downcallHandle(
                lookup.lookup("free").orElseThrow(),
                FunctionDescriptor.ofVoid(ADDRESS),
            )
        }
    }
}

private val unalignedNativeShort = JAVA_SHORT.withBitAlignment(8)
private val unalignedLeShort = unalignedNativeShort.withOrder(LITTLE_ENDIAN)
private val unalignedBeShort = unalignedNativeShort.withOrder(BIG_ENDIAN)

private val unalignedNativeInt = JAVA_INT.withBitAlignment(8)
private val unalignedLeInt = unalignedNativeInt.withOrder(LITTLE_ENDIAN)
private val unalignedBeInt = unalignedNativeInt.withOrder(BIG_ENDIAN)

private val unalignedNativeLong = JAVA_LONG.withBitAlignment(8)
private val unalignedLeLong = unalignedNativeLong.withOrder(LITTLE_ENDIAN)
private val unalignedBeLong = unalignedNativeLong.withOrder(BIG_ENDIAN)

private val unalignedNativeFloat = JAVA_FLOAT.withBitAlignment(8)
private val unalignedLeFloat = unalignedNativeFloat.withOrder(LITTLE_ENDIAN)
private val unalignedBeFloat = unalignedNativeFloat.withOrder(BIG_ENDIAN)

private val unalignedNativeDouble = JAVA_DOUBLE.withBitAlignment(8)
private val unalignedLeDouble = unalignedNativeDouble.withOrder(LITTLE_ENDIAN)
private val unalignedBeDouble = unalignedNativeDouble.withOrder(BIG_ENDIAN)
