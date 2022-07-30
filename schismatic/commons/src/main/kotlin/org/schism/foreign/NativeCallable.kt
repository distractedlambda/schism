package org.schism.foreign

import org.schism.util.contextual
import java.lang.foreign.Addressable
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker
import java.lang.foreign.MemoryAddress
import java.lang.foreign.MemoryLayout
import java.lang.foreign.MemorySegment
import java.lang.foreign.MemorySession
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType.methodType
import java.lang.reflect.Modifier
import kotlin.reflect.KFunction
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.typeOf

@Suppress("NOTHING_TO_INLINE")
public inline fun MemorySession.nativeCallable(function: KFunction<*>): MemorySegment {
    return nativeCallable(function, MethodHandles.lookup())
}

@PublishedApi
internal fun MemorySession.nativeCallable(function: KFunction<*>, lookup: MethodHandles.Lookup): MemorySegment {
    val javaMethod = function.javaMethod

    require(javaMethod != null && Modifier.isStatic(javaMethod.modifiers)) {
        "$function does not map to a static Java method"
    }

    var target = lookup.unreflect(javaMethod)
    val argumentLayouts = mutableListOf<MemoryLayout>()

    for ((kParam, jvmType) in function.valueParameters.zip(javaMethod.parameterTypes)) {
        val abiClass = AbiClass.fromType(kParam.type, lookup)

        if (jvmType != abiClass.jvmType) {
            throw UnsupportedOperationException("Unexpected JVM type for $kParam: expected ${abiClass.jvmType}, " +
                "found $jvmType")
        }

        argumentLayouts.add(abiClass.layout)

        val filter = when (abiClass) {
            AbiClass.JvmByteNativeI8,
            AbiClass.JvmShortNativeI16,
            AbiClass.JvmIntNativeI32,
            AbiClass.JvmLongNativeI64,
            AbiClass.JvmFloatNativeF32,
            AbiClass.JvmDoubleNativeF64,
            AbiClass.JvmMemoryAddressNativeAddress -> null

            AbiClass.JvmAddressableNativeAddress -> MEMORY_ADDRESS_TO_ADDRESSABLE_HANDLE

            AbiClass.JvmLongNativeI32Sext -> INT_TO_LONG_HANDLE

            AbiClass.JvmLongNativeI32Zext -> INT_TO_UNSIGNED_LONG_HANDLE

            is AbiClass.StructByValue -> MethodHandles.explicitCastArguments(
                STRUCT_TYPE_WRAP_HANDLE.bindTo(abiClass.type),
                methodType(abiClass.clazz, MemorySegment::class.java),
            )
        }

        filter?.let {
            target = MethodHandles.filterArguments(target, kParam.index, it)
        }
    }

    val functionDescriptor = when (val returnType = function.returnType) {
        typeOf<Unit>() -> {
            target = MethodHandles.dropReturn(target)
            FunctionDescriptor.ofVoid(*argumentLayouts.toTypedArray())
        }

        else -> {
            val returnAbiClass = AbiClass.fromType(returnType, lookup)

            if (javaMethod.returnType != returnAbiClass.jvmType) {
                throw UnsupportedOperationException("Unexpected JVM return type for $function: expected " +
                    "${returnAbiClass.jvmType}, found ${javaMethod.returnType}"
                )
            }

            val returnFilter = when (returnAbiClass) {
                AbiClass.JvmByteNativeI8,
                AbiClass.JvmShortNativeI16,
                AbiClass.JvmIntNativeI32,
                AbiClass.JvmLongNativeI64,
                AbiClass.JvmFloatNativeF32,
                AbiClass.JvmDoubleNativeF64,
                AbiClass.JvmAddressableNativeAddress -> null

                AbiClass.JvmMemoryAddressNativeAddress -> MEMORY_ADDRESS_TO_ADDRESSABLE_HANDLE

                AbiClass.JvmLongNativeI32Sext,
                AbiClass.JvmLongNativeI32Zext -> LONG_TO_INT_HANDLE

                is AbiClass.StructByValue -> STRUCT_SEGMENT_HANDLE
            }

            returnFilter?.let {
                target = MethodHandles.filterReturnValue(target, it)
            }

            FunctionDescriptor.of(returnAbiClass.layout, *argumentLayouts.toTypedArray())
        }
    }

    return NATIVE_LINKER.upcallStub(target, functionDescriptor, contextual())
}

private val NATIVE_LINKER = Linker.nativeLinker()

private val INT_TO_LONG_HANDLE = MethodHandles
    .identity(Int::class.java)
    .asType(methodType(Long::class.java, Int::class.java))

private val LONG_TO_INT_HANDLE = MethodHandles
    .explicitCastArguments(
        MethodHandles.identity(Long::class.java),
        methodType(Int::class.java, Long::class.java),
    )

private val INT_TO_UNSIGNED_LONG_HANDLE = MethodHandles
    .lookup()
    .findStatic(Integer::class.java, "toUnsignedLong", methodType(Long::class.java, Int::class.java))

private val MEMORY_ADDRESS_TO_ADDRESSABLE_HANDLE = MethodHandles
    .identity(MemoryAddress::class.java)
    .asType(methodType(Addressable::class.java, MemoryAddress::class.java))

private val STRUCT_TYPE_WRAP_HANDLE = MethodHandles
    .lookup()
    .unreflect(StructType<*>::wrap.javaMethod)

private val STRUCT_SEGMENT_HANDLE = MethodHandles
    .lookup()
    .unreflect(Struct::segment.javaMethod)
