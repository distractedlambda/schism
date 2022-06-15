package org.schism.foreign

import java.lang.Math.addExact
import java.lang.foreign.ValueLayout
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class StructLayoutCalculator {
    private var size = 0L
    private var alignment = 1L

    val calculatedLayout: NativeLayout get() = NativeLayout(size, alignment)

    fun <L : ValueLayout> member(layout: L): NativeMember<L> {
        size = size.alignForwardsTo(layout.byteAlignment())
        alignment = maxOf(alignment, layout.byteAlignment())
        return NativeMember(layout, size.byteOffset).also {
            size += layout.byteSize()
        }
    }

    fun skip(byteCount: Long) {
        size = addExact(size, byteCount)
    }
}

@OptIn(ExperimentalContracts::class)
inline fun calculateStructLayout(body: StructLayoutCalculator.() -> Unit): NativeLayout {
    contract {
        callsInPlace(body, InvocationKind.EXACTLY_ONCE)
    }

    return StructLayoutCalculator().apply(body).calculatedLayout
}

@OptIn(ExperimentalContracts::class)
inline fun calculateCStructLayout(body: StructLayoutCalculator.() -> Unit): NativeLayout {
    contract {
        callsInPlace(body, InvocationKind.EXACTLY_ONCE)
    }

    return calculateStructLayout(body).legalizeForC()
}
