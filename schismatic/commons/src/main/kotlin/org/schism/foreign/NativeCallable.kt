package org.schism.foreign

import org.schism.util.contextual
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker
import java.lang.foreign.MemoryLayout
import java.lang.foreign.MemorySegment
import java.lang.foreign.MemorySession
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodHandles.Lookup
import java.lang.invoke.MethodHandles.filterArguments
import java.lang.invoke.MethodHandles.filterReturnValue
import java.lang.invoke.MethodType.methodType
import java.lang.reflect.Modifier
import kotlin.reflect.KFunction
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.javaMethod

@Suppress("NOTHING_TO_INLINE")
public inline fun MemorySession.nativeCallable(function: KFunction<*>): MemorySegment {
    return nativeCallable(MethodHandles.lookup(), function)
}

public fun MemorySession.nativeCallable(lookup: Lookup, function: KFunction<*>): MemorySegment {
    val javaMethod = function.javaMethod

    require(javaMethod != null && Modifier.isStatic(javaMethod.modifiers)) {
        "$function does not map to a static Java method"
    }

    var target = lookup.unreflect(javaMethod)
    val argumentLayouts = mutableListOf<MemoryLayout>()

    for ((kParam, jType) in function.valueParameters.zip(javaMethod.parameterTypes)) {
        val handling = ffiHandlingFor(kParam.type)
        argumentLayouts.add(handling.memoryLayout)

        require(handling.jvmType == jType)

        when (handling) {
            FfiHandling.I32AsSignedLong -> {
                target = filterArguments(target, kParam.index, INT_TO_LONG_HANDLE)
            }

            FfiHandling.I32AsUnsignedLong -> {
                target = filterArguments(target, kParam.index, INT_TO_UNSIGNED_LONG_HANDLE)
            }

            else -> Unit
        }
    }

    val functionDescriptor = if (function.returnType.classifier == Unit::class) {
        require(javaMethod.returnType == Void.TYPE)
        FunctionDescriptor.ofVoid(*argumentLayouts.toTypedArray())
    } else {
        val handling = ffiHandlingFor(function.returnType)

        require(handling.jvmType == javaMethod.returnType)

        when (handling) {
            FfiHandling.I32AsSignedLong, FfiHandling.I32AsUnsignedLong -> {
                target = filterReturnValue(target, LONG_TO_INT_HANDLE)
            }

            else -> Unit
        }

        FunctionDescriptor.of(handling.memoryLayout, *argumentLayouts.toTypedArray())
    }

    return NATIVE_LINKER.upcallStub(target, functionDescriptor, contextual())
}

private val NATIVE_LINKER = Linker.nativeLinker()

private val INT_TO_LONG_HANDLE = MethodHandles
    .identity(Int::class.java)
    .asType(methodType(Long::class.java, Int::class.java))

private val LONG_TO_INT_HANDLE = MethodHandles.explicitCastArguments(
    MethodHandles.identity(Long::class.java),
    methodType(Int::class.java, Long::class.java),
)

private val INT_TO_UNSIGNED_LONG_HANDLE = MethodHandles.lookup().findStatic(
    Integer::class.java,
    "toUnsignedLong",
    methodType(Long::class.java, Int::class.java)
)
