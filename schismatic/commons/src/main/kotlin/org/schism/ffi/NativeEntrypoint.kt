package org.schism.ffi

import org.schism.memory.NativeAddress
import org.schism.memory.toNativeAddress
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker
import java.lang.foreign.MemoryLayout
import java.lang.foreign.MemorySession
import java.lang.foreign.ValueLayout.JAVA_BYTE
import java.lang.foreign.ValueLayout.JAVA_DOUBLE
import java.lang.foreign.ValueLayout.JAVA_FLOAT
import java.lang.foreign.ValueLayout.JAVA_INT
import java.lang.foreign.ValueLayout.JAVA_LONG
import java.lang.foreign.ValueLayout.JAVA_SHORT
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodHandles.Lookup
import java.lang.invoke.MethodHandles.filterArguments
import java.lang.invoke.MethodHandles.filterReturnValue
import java.lang.invoke.MethodType.methodType
import java.lang.reflect.Modifier
import kotlin.reflect.KFunction
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.typeOf

public interface NativeEntrypoint {
    public val address: NativeAddress
}

@Suppress("NOTHING_TO_INLINE")
public inline fun nativeEntrypoint(function: KFunction<*>): NativeEntrypoint {
    return nativeEntrypoint(MethodHandles.lookup(), function)
}

public fun nativeEntrypoint(lookup: Lookup, function: KFunction<*>): NativeEntrypoint {
    val javaMethod = function.javaMethod

    require(javaMethod != null && Modifier.isStatic(javaMethod.modifiers)) {
        "$function does not map to a static Java method"
    }

    var handle = lookup.unreflect(javaMethod)
    val argumentLayouts = mutableListOf<MemoryLayout>()

    for ((kParam, jParam) in function.valueParameters.zip(javaMethod.parameterTypes)) when (kParam.type) {
        typeOf<Byte>(), typeOf<UByte>() -> {
            require(jParam == Byte::class.java)
            argumentLayouts.add(JAVA_BYTE)
        }

        typeOf<Short>(), typeOf<UShort>() -> {
            require(jParam == Short::class.java)
            argumentLayouts.add(JAVA_SHORT)
        }

        typeOf<Int>(), typeOf<UInt>() -> {
            require(jParam == Int::class.java)
            argumentLayouts.add(JAVA_INT)
        }

        typeOf<Long>(), typeOf<ULong>() -> {
            require(jParam == Long::class.java)
            argumentLayouts.add(JAVA_LONG)
        }

        typeOf<Float>() -> {
            require(jParam == Float::class.java)
            argumentLayouts.add(JAVA_FLOAT)
        }

        typeOf<Double>() -> {
            require(jParam == Double::class.java)
            argumentLayouts.add(JAVA_DOUBLE)
        }

        typeOf<CLong>() -> {
            require(jParam == Long::class.java)
            if (C_LONG_IS_4_BYTES) {
                argumentLayouts.add(JAVA_INT)
                handle = filterArguments(handle, kParam.index, INT_TO_LONG_HANDLE)
            } else {
                argumentLayouts.add(JAVA_LONG)
            }
        }

        typeOf<CUnsignedLong>() -> {
            require(jParam == Long::class.java)
            if (C_LONG_IS_4_BYTES) {
                argumentLayouts.add(JAVA_INT)
                handle = filterArguments(handle, kParam.index, INT_TO_UNSIGNED_LONG_HANDLE)
            } else {
                argumentLayouts.add(JAVA_LONG)
            }
        }

        typeOf<CPtrDiffT>() -> {
            require(jParam == Long::class.java)
            if (ADDRESS_IS_4_BYTES) {
                argumentLayouts.add(JAVA_INT)
                handle = filterArguments(handle, kParam.index, INT_TO_LONG_HANDLE)
            } else {
                argumentLayouts.add(JAVA_LONG)
            }
        }

        typeOf<CSizeT>(), typeOf<NativeAddress>() -> {
            require(jParam == Long::class.java)
            if (ADDRESS_IS_4_BYTES) {
                argumentLayouts.add(JAVA_INT)
                handle = filterArguments(handle, kParam.index, INT_TO_UNSIGNED_LONG_HANDLE)
            } else {
                argumentLayouts.add(JAVA_LONG)
            }
        }

        else -> {
            throw UnsupportedOperationException("Unsupported parameter type: ${kParam.type}")
        }
    }

    val returnLayout: MemoryLayout?

    when (function.returnType) {
        typeOf<Byte>(), typeOf<UByte>() -> {
            require(javaMethod.returnType == Byte::class.java)
            returnLayout = JAVA_BYTE
        }

        typeOf<Short>(), typeOf<UShort>() -> {
            require(javaMethod.returnType == Short::class.java)
            returnLayout = JAVA_SHORT
        }

        typeOf<Int>(), typeOf<UInt>() -> {
            require(javaMethod.returnType == Int::class.java)
            returnLayout = JAVA_INT
        }

        typeOf<Long>(), typeOf<ULong>() -> {
            require(javaMethod.returnType == Long::class.java)
            returnLayout = JAVA_LONG
        }

        typeOf<Float>() -> {
            require(javaMethod.returnType == Float::class.java)
            returnLayout = JAVA_FLOAT
        }

        typeOf<Double>() -> {
            require(javaMethod.returnType == Double::class.java)
            returnLayout = JAVA_DOUBLE
        }

        typeOf<CLong>(), typeOf<CUnsignedLong>() -> {
            require(javaMethod.returnType == Long::class.java)
            if (C_LONG_IS_4_BYTES) {
                returnLayout = JAVA_INT
                handle = filterReturnValue(handle, LONG_TO_INT_HANDLE)
            } else {
                returnLayout = JAVA_LONG
            }
        }

        typeOf<CPtrDiffT>(), typeOf<CSizeT>(), typeOf<NativeAddress>() -> {
            require(javaMethod.returnType == Long::class.java)
            if (ADDRESS_IS_4_BYTES) {
                returnLayout = JAVA_INT
                handle = filterReturnValue(handle, LONG_TO_INT_HANDLE)
            } else {
                returnLayout = JAVA_LONG
            }
        }

        typeOf<Unit>() -> {
            require(javaMethod.returnType == Void.TYPE)
            returnLayout = null
        }

        else -> {
            throw UnsupportedOperationException("Unsupported return type: ${function.returnType}")
        }
    }

    val argumentLayoutsArray = argumentLayouts.toTypedArray()

    val functionDescriptor = if (returnLayout != null) {
        FunctionDescriptor.of(returnLayout, *argumentLayoutsArray)
    } else {
        FunctionDescriptor.ofVoid(*argumentLayoutsArray)
    }

    val memorySession = MemorySession.openImplicit()

    val address = NATIVE_LINKER
        .upcallStub(handle, functionDescriptor, memorySession)
        .address()
        .toNativeAddress()

    return NativeEntrypointImpl(address, memorySession)
}

private class NativeEntrypointImpl(
    override val address: NativeAddress,
    private val session: MemorySession,
) : NativeEntrypoint

private val NATIVE_LINKER = Linker.nativeLinker()

private val INT_TO_LONG_HANDLE = MethodHandles
    .identity(Int::class.java)
    .asType(methodType(Long::class.java, Int::class.java))

private val LONG_TO_INT_HANDLE = MethodHandles
    .identity(Long::class.java)
    .asType(methodType(Int::class.java, Long::class.java))

private val INT_TO_UNSIGNED_LONG_HANDLE = MethodHandles
    .lookup()
    .unreflect(Integer::toUnsignedLong.javaMethod)
