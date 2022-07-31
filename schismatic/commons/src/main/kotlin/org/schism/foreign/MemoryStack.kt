package org.schism.foreign

import org.schism.math.requireValidAlignment
import org.schism.ref.SharedCleaner
import java.lang.foreign.MemorySegment
import java.lang.foreign.MemorySession
import java.lang.foreign.SegmentAllocator
import java.lang.ref.Reference.reachabilityFence
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

public object MemoryStack : SegmentAllocator {
    override fun allocate(bytesSize: Long, bytesAlignment: Long): MemorySegment {
        return currentFrame.get().allocate(bytesSize, bytesAlignment)
    }

    public fun pushFrame() {
        currentFrame.get().pushChildFrame()
    }

    public fun popFrame() {
        currentFrame.get().pop()
    }

    @OptIn(ExperimentalContracts::class)
    public inline fun <R> withFrame(block: MemoryStack.() -> R): R {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }

        pushFrame()

        try {
            return block(this)
        } finally {
            popFrame()
        }
    }
}

private class MemoryStackFrame(
    private val parentFrame: MemoryStackFrame?,
    private val session: MemorySession,
    private val nonCloseableSession: MemorySession,
    private val bottomPointer: Long,
    private var stackPointer: Long,
) {
    fun allocate(bytesSize: Long, bytesAlignment: Long): MemorySegment {
        require(bytesSize >= 0) { "Size cannot be negative" }
        requireValidAlignment(bytesAlignment)
        require(bytesAlignment <= ALIGN) { "Alignments greater than $ALIGN are not supported" }

        val alignedPointer = stackPointer and bytesAlignment.dec()

        if (alignedPointer - bottomPointer < bytesSize) {
            throw IllegalStateException("Insufficient space: ${stackPointer - bottomPointer} bytes available, but " +
                "requested allocation requires ${stackPointer - alignedPointer + bytesSize} bytes"
            )
        }

        stackPointer = alignedPointer - bytesSize
        return stackPointer.toMemoryAddress().asMemorySegment(bytesSize, nonCloseableSession)
    }

    fun pushChildFrame() {
        val newSession = confinedMemorySession()

        session.let { oldSession ->
            newSession.addCloseAction {
                reachabilityFence(oldSession)
            }
        }

        currentFrame.set(
            MemoryStackFrame(
                parentFrame = this,
                session = newSession,
                nonCloseableSession = newSession.asNonCloseable(),
                bottomPointer = bottomPointer,
                stackPointer = stackPointer,
            )
        )
    }

    fun pop() {
        session.close()
        currentFrame.set(parentFrame!!)
    }
}

private val currentFrame = ThreadLocal.withInitial {
    val rootSession = confinedMemorySession(SharedCleaner).asNonCloseable()
    val rootSegment = rootSession.allocate(SIZE, ALIGN)
    MemoryStackFrame(
        parentFrame = null,
        session = rootSession,
        nonCloseableSession = rootSession,
        bottomPointer = rootSegment.address().toRawLongValue(),
        stackPointer = rootSegment.address().toRawLongValue() + SIZE,
    )
}

private const val SIZE = 4L * 1024L * 1024L

private const val ALIGN = 8L
