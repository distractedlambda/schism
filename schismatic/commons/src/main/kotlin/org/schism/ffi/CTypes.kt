package org.schism.ffi

import org.schism.memory.NativeAddress
import java.lang.foreign.MemoryLayout
import java.lang.foreign.ValueLayout.JAVA_BYTE
import java.lang.foreign.ValueLayout.JAVA_DOUBLE
import java.lang.foreign.ValueLayout.JAVA_FLOAT
import java.lang.foreign.ValueLayout.JAVA_INT
import java.lang.foreign.ValueLayout.JAVA_LONG
import java.lang.foreign.ValueLayout.JAVA_SHORT
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotation

internal enum class ScalarFfiType {
    I8,
    I16,
    I32,
    I64,
    F32,
    F64,
    SIGNED_WORD,
    UNSIGNED_WORD,
    SIGNED_C_LONG,
    UNSIGNED_C_LONG;

    val handling: FfiHandling get() = when (this) {
        I8 -> FfiHandling.I8AsByte
        I16 -> FfiHandling.I16AsShort
        I32 -> FfiHandling.I32AsInt
        I64 -> FfiHandling.I64AsLong
        F32 -> FfiHandling.F32AsFloat
        F64 -> FfiHandling.F64AsDouble

        SIGNED_WORD -> when (ADDRESS_TYPE) {
            IntOrLong.INT -> FfiHandling.I32AsSignedLong
            IntOrLong.LONG -> FfiHandling.I64AsLong
        }

        UNSIGNED_WORD -> when (ADDRESS_TYPE) {
            IntOrLong.INT -> FfiHandling.I32AsUnsignedLong
            IntOrLong.LONG -> FfiHandling.I64AsLong
        }

        SIGNED_C_LONG -> when (C_LONG_TYPE) {
            IntOrLong.INT -> FfiHandling.I32AsSignedLong
            IntOrLong.LONG -> FfiHandling.I64AsLong
        }

        UNSIGNED_C_LONG -> when (C_LONG_TYPE) {
            IntOrLong.INT -> FfiHandling.I32AsUnsignedLong
            IntOrLong.LONG -> FfiHandling.I64AsLong
        }
    }
}

internal sealed interface FfiHandling {
    val memoryLayout: MemoryLayout

    val jvmType: Class<*>

    object I8AsByte : FfiHandling {
        override val memoryLayout: MemoryLayout get() {
            return JAVA_BYTE
        }

        override val jvmType: Class<*> get() {
            return Byte::class.java
        }
    }

    object I16AsShort : FfiHandling {
        override val memoryLayout: MemoryLayout get() {
            return JAVA_SHORT
        }

        override val jvmType: Class<*> get() {
            return Short::class.java
        }
    }

    object I32AsInt : FfiHandling {
        override val memoryLayout: MemoryLayout get() {
            return JAVA_INT
        }

        override val jvmType: Class<*> get() {
            return Int::class.java
        }
    }

    object I64AsLong : FfiHandling {
        override val memoryLayout: MemoryLayout get() {
            return JAVA_LONG
        }

        override val jvmType: Class<*> get() {
            return Long::class.java
        }
    }

    object F32AsFloat : FfiHandling {
        override val memoryLayout: MemoryLayout get() {
            return JAVA_FLOAT
        }

        override val jvmType: Class<*> get() {
            return Float::class.java
        }
    }

    object F64AsDouble : FfiHandling {
        override val memoryLayout: MemoryLayout get() {
            return JAVA_DOUBLE
        }

        override val jvmType: Class<*> get() {
            return Double::class.java
        }
    }

    object I32AsSignedLong : FfiHandling {
        override val memoryLayout: MemoryLayout get() {
            return JAVA_INT
        }

        override val jvmType: Class<*> get() {
            return Long::class.java
        }
    }

    object I32AsUnsignedLong : FfiHandling {
        override val memoryLayout: MemoryLayout get() {
            return JAVA_INT
        }

        override val jvmType: Class<*> get() {
            return Long::class.java
        }
    }
}

internal fun ffiHandlingFor(kType: KType): FfiHandling {
    val scalarTypeAnnotations = kType.annotations.mapNotNull {
        it.annotationClass.findAnnotation<ScalarFfiTypeMarker>()
    }

    if (scalarTypeAnnotations.isNotEmpty()) {
        require(scalarTypeAnnotations.size == 1) {
            "Type '$kType' has multiple FFI type annotations"
        }

        val scalarTypeAnnotation = scalarTypeAnnotations.single()

        require(kType.classifier == scalarTypeAnnotation.klass) {
            "FFI type '$scalarTypeAnnotation' cannot be applied to class '${scalarTypeAnnotation.klass}'"
        }

        return scalarTypeAnnotation.ffiType.handling
    }

    return when (kType.classifier) {
        Byte::class, UByte::class -> FfiHandling.I8AsByte
        Short::class, UShort::class -> FfiHandling.I16AsShort
        Int::class, UInt::class -> FfiHandling.I32AsInt
        Long::class, ULong::class -> FfiHandling.I64AsLong
        Float::class -> FfiHandling.F32AsFloat
        Double::class -> FfiHandling.F64AsDouble
        NativeAddress::class -> ScalarFfiType.UNSIGNED_WORD.handling
        else -> throw IllegalArgumentException("Type '$kType' is not FFI-compatible")
    }
}

@Target(AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class ScalarFfiTypeMarker(val ffiType: ScalarFfiType, val klass: KClass<*>)

@ScalarFfiTypeMarker(ScalarFfiType.I8, Byte::class)
@Target(AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
public annotation class CChar

@ScalarFfiTypeMarker(ScalarFfiType.I8, UByte::class)
@Target(AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
public annotation class CUnsignedChar

@ScalarFfiTypeMarker(ScalarFfiType.I16, Short::class)
@Target(AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
public annotation class CShort

@ScalarFfiTypeMarker(ScalarFfiType.I16, UShort::class)
@Target(AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
public annotation class CUnsignedShort

@ScalarFfiTypeMarker(ScalarFfiType.I32, Int::class)
@Target(AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
public annotation class CInt

@ScalarFfiTypeMarker(ScalarFfiType.I32, UInt::class)
@Target(AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
public annotation class CUnsignedInt

@ScalarFfiTypeMarker(ScalarFfiType.SIGNED_C_LONG, Long::class)
@Target(AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
public annotation class CLong

@ScalarFfiTypeMarker(ScalarFfiType.UNSIGNED_C_LONG, ULong::class)
@Target(AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
public annotation class CUnsignedLong

@ScalarFfiTypeMarker(ScalarFfiType.I64, Long::class)
@Target(AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
public annotation class CLongLong

@ScalarFfiTypeMarker(ScalarFfiType.I64, ULong::class)
@Target(AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
public annotation class CUnsignedLongLong

@ScalarFfiTypeMarker(ScalarFfiType.UNSIGNED_WORD, ULong::class)
@Target(AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
public annotation class CSizeT

@ScalarFfiTypeMarker(ScalarFfiType.UNSIGNED_WORD, ULong::class)
@Target(AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
public annotation class CUIntPtrT

@ScalarFfiTypeMarker(ScalarFfiType.SIGNED_WORD, Long::class)
@Target(AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
public annotation class CSSizeT

@ScalarFfiTypeMarker(ScalarFfiType.SIGNED_WORD, Long::class)
@Target(AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
public annotation class CPtrDiffT

@ScalarFfiTypeMarker(ScalarFfiType.SIGNED_WORD, Long::class)
@Target(AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
public annotation class CIntPtrT

@ScalarFfiTypeMarker(ScalarFfiType.F32, Float::class)
@Target(AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
public annotation class CFloat

@ScalarFfiTypeMarker(ScalarFfiType.F64, Double::class)
@Target(AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
public annotation class CDouble
