package org.schism.foreign

import org.schism.util.asUnchecked
import java.lang.System.getProperty
import java.lang.foreign.Addressable
import java.lang.foreign.MemoryAddress
import java.lang.foreign.MemoryLayout
import java.lang.foreign.ValueLayout
import java.lang.invoke.MethodHandles
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.typeOf

internal enum class IntOrLong {
    INT,
    LONG;
}

internal sealed class AbiClass(val layout: MemoryLayout, val jvmType: Class<*>) {
    object JvmByteNativeI8 :
        AbiClass(ValueLayout.JAVA_BYTE, Byte::class.java),
        Scalar,
        StructMemberAllowable

    object JvmShortNativeI16 :
        AbiClass(ValueLayout.JAVA_SHORT, Short::class.java),
        Scalar,
        StructMemberAllowable

    object JvmIntNativeI32 :
        AbiClass(ValueLayout.JAVA_INT, Int::class.java),
        Scalar,
        StructMemberAllowable

    object JvmLongNativeI64 :
        AbiClass(ValueLayout.JAVA_LONG, Long::class.java),
        Scalar,
        StructMemberAllowable

    object JvmLongNativeI32Sext :
        AbiClass(ValueLayout.JAVA_INT, Long::class.java),
        Scalar,
        StructMemberAllowable

    object JvmLongNativeI32Zext :
        AbiClass(ValueLayout.JAVA_INT, Long::class.java),
        Scalar,
        StructMemberAllowable

    object JvmFloatNativeF32 :
        AbiClass(ValueLayout.JAVA_FLOAT, Float::class.java),
        Scalar,
        StructMemberAllowable

    object JvmDoubleNativeF64 :
        AbiClass(ValueLayout.JAVA_DOUBLE, Double::class.java),
        Scalar,
        StructMemberAllowable

    object JvmAddressableNativeAddress :
        AbiClass(ValueLayout.ADDRESS, Addressable::class.java),
        Scalar

    object JvmMemoryAddressNativeAddress :
        AbiClass(ValueLayout.ADDRESS, MemoryAddress::class.java),
        Scalar,
        StructMemberAllowable

    data class StructByValue(val clazz: Class<*>, val type: StructType<*>) :
        AbiClass(type.layout, clazz),
        StructMemberAllowable

    sealed interface Scalar

    sealed interface StructMemberAllowable

    companion object {
        fun fromType(type: KType, lookup: MethodHandles.Lookup): AbiClass = when {
            type == typeOf<Byte>() || type == typeOf<UByte>() -> {
                JvmByteNativeI8
            }

            type == typeOf<Short>() || type == typeOf<UShort>() -> {
                JvmShortNativeI16
            }

            type == typeOf<Int>() || type == typeOf<UInt>() -> {
                JvmIntNativeI32
            }

            type == typeOf<Long>() || type == typeOf<ULong>() -> {
                JvmLongNativeI64
            }

            type == typeOf<Float>() -> {
                JvmFloatNativeF32
            }

            type == typeOf<Double>() -> {
                JvmDoubleNativeF64
            }

            type == typeOf<CLong>() -> when (C_LONG_TYPE) {
                IntOrLong.INT -> JvmLongNativeI32Sext
                IntOrLong.LONG -> JvmLongNativeI64
            }

            type == typeOf<CUnsignedLong>() -> when (C_LONG_TYPE) {
                IntOrLong.INT -> JvmLongNativeI32Zext
                IntOrLong.LONG -> JvmLongNativeI64
            }

            type == typeOf<CPtrDiffT>() -> when (ADDRESS_TYPE) {
                IntOrLong.INT -> JvmLongNativeI32Sext
                IntOrLong.LONG -> JvmLongNativeI64
            }

            type == typeOf<CSizeT>() -> when (ADDRESS_TYPE) {
                IntOrLong.INT -> JvmLongNativeI32Zext
                IntOrLong.LONG -> JvmLongNativeI64
            }

            type == typeOf<Addressable>() -> {
                JvmAddressableNativeAddress
            }

            type == typeOf<MemoryAddress>() -> {
                JvmMemoryAddressNativeAddress
            }

            type.isSubtypeOf(typeOf<Struct>()) -> {
                val klass = type.classifier.asUnchecked<KClass<out Struct>>()
                StructByValue(klass.java, StructType(klass, lookup))
            }

            else -> {
                throw UnsupportedOperationException("Type $type cannot be transacted with native code")
            }
        }
    }
}

internal val ADDRESS_TYPE = when (ValueLayout.ADDRESS.bitSize()) {
    32L -> IntOrLong.INT
    64L -> IntOrLong.LONG
    else -> throw UnsupportedOperationException("Unexpected native address bit size: ${ValueLayout.ADDRESS.bitSize()}")
}

internal val C_LONG_TYPE = when (ADDRESS_TYPE) {
    IntOrLong.INT -> IntOrLong.INT
    IntOrLong.LONG -> when {
        getProperty("os.name").startsWith("Windows") -> IntOrLong.INT
        else -> IntOrLong.LONG
    }
}
