package org.schism.foreign

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.ClassWriter.COMPUTE_FRAMES
import org.objectweb.asm.ConstantDynamic
import org.objectweb.asm.Handle
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.ACC_FINAL
import org.objectweb.asm.Opcodes.ACC_PRIVATE
import org.objectweb.asm.Opcodes.ACC_PUBLIC
import org.objectweb.asm.Opcodes.ALOAD
import org.objectweb.asm.Opcodes.ARETURN
import org.objectweb.asm.Opcodes.DLOAD
import org.objectweb.asm.Opcodes.DRETURN
import org.objectweb.asm.Opcodes.FLOAD
import org.objectweb.asm.Opcodes.FRETURN
import org.objectweb.asm.Opcodes.GETFIELD
import org.objectweb.asm.Opcodes.GETSTATIC
import org.objectweb.asm.Opcodes.H_INVOKESTATIC
import org.objectweb.asm.Opcodes.I2L
import org.objectweb.asm.Opcodes.ILOAD
import org.objectweb.asm.Opcodes.INVOKEINTERFACE
import org.objectweb.asm.Opcodes.INVOKESPECIAL
import org.objectweb.asm.Opcodes.IRETURN
import org.objectweb.asm.Opcodes.L2I
import org.objectweb.asm.Opcodes.LAND
import org.objectweb.asm.Opcodes.LLOAD
import org.objectweb.asm.Opcodes.LRETURN
import org.objectweb.asm.Opcodes.RETURN
import org.objectweb.asm.Opcodes.V19
import org.schism.invoke.HiddenClassDefiner
import org.schism.math.alignForwardsTo
import org.schism.math.forwardsAlignmentOffsetTo
import org.schism.math.plusExact
import org.schism.math.timesExact
import org.schism.reflect.descriptorString
import org.schism.reflect.internalName
import java.lang.foreign.GroupLayout
import java.lang.foreign.MemoryAddress
import java.lang.foreign.MemoryLayout
import java.lang.foreign.MemoryLayout.paddingLayout
import java.lang.foreign.MemoryLayout.structLayout
import java.lang.foreign.MemorySegment
import java.lang.foreign.MemorySession
import java.lang.foreign.SegmentAllocator
import java.lang.foreign.ValueLayout
import java.lang.invoke.CallSite
import java.lang.invoke.ConstantCallSite
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles.Lookup
import java.lang.invoke.MethodHandles.classData
import java.lang.invoke.MethodType
import java.lang.invoke.MethodType.methodType
import java.util.TreeMap
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.javaGetter
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.javaSetter

public interface Struct {
    public fun type(): StructType<*>

    public fun segment(): MemorySegment
}

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
public annotation class StructField(val index: Int)

public interface StructType<out S : Struct> {
    public val layout: GroupLayout

    public fun wrap(segment: MemorySegment): S
}

public fun Struct.address(): MemoryAddress {
    return segment().address()
}

public fun <S : Struct> MemorySegment.asStruct(type: StructType<S>): S {
    return type.wrap(this)
}

public fun <S : Struct> MemoryAddress.asStruct(type: StructType<S>, session: MemorySession = globalMemorySession()): S {
    return asMemorySegment(type.layout.byteSize(), session).asStruct(type)
}

public fun <S : Struct> SegmentAllocator.allocate(type: StructType<S>): S {
    return type.wrap(allocate(type.layout))
}

public inline fun <reified S : Struct> StructType(): StructType<S> {
    return StructType(S::class, HiddenClassDefiner())
}

@PublishedApi
internal fun <S : Struct> StructType(klass: KClass<S>, definer: HiddenClassDefiner): StructType<S> {
    require(klass.java.isInterface) {
        "$klass is not an interface"
    }

    require(klass.isSubclassOf(Struct::class)) {
        "$klass does not extend ${Struct::class.simpleName}"
    }

    var size = 0L
    var alignment = 1L

    val fields = TreeMap<Int, KProperty1<*, *>>()

    klass.members.forEach { member ->
        val field = (member.findAnnotation<StructField>() ?: return@forEach).index

        require(member is KProperty1<*, *>) {
            "$member is not a non-extension property"
        }

        fields.put(field, member)?.let { otherMember ->
            throw IllegalArgumentException("Field index $field is used by both $member and $otherMember")
        }
    }

    val implName = "${definer.internalNamePrefix}${klass.simpleName}Impl"
    val implWriter = ClassWriter(COMPUTE_FRAMES)
    val memberLayouts = mutableListOf<MemoryLayout>()

    implWriter.visit(V19, ACC_FINAL, implName, null, AbstractStructImpl.INTERNAL_NAME, arrayOf(klass.java.internalName))

    implWriter.visitMethod(ACC_PRIVATE, "<init>", AbstractStructImpl.CONSTRUCTOR_DESCRIPTOR, null, null).apply {
        visitCode()

        visitVarInsn(ALOAD, 0)
        visitVarInsn(ALOAD, 1)
        visitMethodInsn(
            INVOKESPECIAL,
            AbstractStructImpl.INTERNAL_NAME,
            "<init>",
            AbstractStructImpl.CONSTRUCTOR_DESCRIPTOR,
            false,
        )

        visitInsn(RETURN)
        visitMaxs(0, 0)
        visitEnd()
    }

    implWriter.visitMethod(
        ACC_PUBLIC,
        AbstractStructImpl.TYPE_NAME,
        AbstractStructImpl.TYPE_DESCRIPTOR,
        null,
        null,
    ).apply {
        visitCode()
        visitLdcInsn(TYPE_CONSTANT)
        visitInsn(ARETURN)
        visitMaxs(0, 0)
        visitEnd()
    }

    fields.values.forEach { property ->
        val getter = property.javaGetter
        val setter = (property as? KMutableProperty1)?.javaSetter

        requireNotNull(getter) {
            "$property has no getter"
        }

        val handling = ffiHandlingFor(property.returnType)
        val offset = size.alignForwardsTo(handling.memoryLayout.byteAlignment())

        if (offset != size) {
            memberLayouts.add(paddingLayout((offset - size) timesExact 8))
        }

        memberLayouts.add(handling.memoryLayout.withName(property.name))
        size = offset plusExact handling.memoryLayout.byteSize()
        alignment = maxOf(alignment, handling.memoryLayout.byteAlignment())

        implWriter.visitMethod(ACC_PUBLIC, getter.name, getter.descriptorString, null, null).apply {
            visitCode()

            visitVarInsn(ALOAD, 0)
            visitFieldInsn(
                GETFIELD,
                AbstractStructImpl.INTERNAL_NAME,
                AbstractStructImpl.SEGMENT_FIELD_NAME,
                MEMORY_SEGMENT_DESCRIPTOR,
            )

            visitLoadValueLayout(handling)

            visitLdcInsn(offset)

            when (handling) {
                FfiHandling.I8AsByte -> visitMethodInsn(
                    INVOKEINTERFACE,
                    MEMORY_SEGMENT_INTERNAL_NAME,
                    "get",
                    "(${VALUE_LAYOUT_OF_BYTE_DESCRIPTOR}J)B",
                    true,
                )

                FfiHandling.I16AsShort -> visitMethodInsn(
                    INVOKEINTERFACE,
                    MEMORY_SEGMENT_INTERNAL_NAME,
                    "get",
                    "(${VALUE_LAYOUT_OF_SHORT_DESCRIPTOR}J)S",
                    true,
                )

                FfiHandling.I32AsInt, FfiHandling.I32AsSignedLong, FfiHandling.I32AsUnsignedLong -> visitMethodInsn(
                    INVOKEINTERFACE,
                    MEMORY_SEGMENT_INTERNAL_NAME,
                    "get",
                    "(${VALUE_LAYOUT_OF_INT_DESCRIPTOR}J)I",
                    true,
                )

                FfiHandling.I64AsLong -> visitMethodInsn(
                    INVOKEINTERFACE,
                    MEMORY_SEGMENT_INTERNAL_NAME,
                    "get",
                    "(${VALUE_LAYOUT_OF_LONG_DESCRIPTOR}J)L",
                    true,
                )

                FfiHandling.F32AsFloat -> visitMethodInsn(
                    INVOKEINTERFACE,
                    MEMORY_SEGMENT_INTERNAL_NAME,
                    "get",
                    "(${VALUE_LAYOUT_OF_FLOAT_DESCRIPTOR}J)F",
                    true,
                )

                FfiHandling.F64AsDouble -> visitMethodInsn(
                    INVOKEINTERFACE,
                    MEMORY_SEGMENT_INTERNAL_NAME,
                    "get",
                    "(${VALUE_LAYOUT_OF_DOUBLE_DESCRIPTOR}J)D",
                    true,
                )

                FfiHandling.Address -> visitMethodInsn(
                    INVOKEINTERFACE,
                    MEMORY_SEGMENT_INTERNAL_NAME,
                    "get",
                    "(${VALUE_LAYOUT_OF_ADDRESS_DESCRIPTOR}J)$MEMORY_ADDRESS_DESCRIPTOR",
                    true,
                )
            }

            when (handling) {
                FfiHandling.I8AsByte, FfiHandling.I16AsShort, FfiHandling.I32AsInt -> {
                    visitInsn(IRETURN)
                }

                FfiHandling.I64AsLong -> {
                    visitInsn(LRETURN)
                }

                FfiHandling.F32AsFloat -> {
                    visitInsn(FRETURN)
                }

                FfiHandling.F64AsDouble -> {
                    visitInsn(DRETURN)
                }

                FfiHandling.I32AsSignedLong -> {
                    visitInsn(I2L)
                    visitInsn(LRETURN)
                }

                FfiHandling.I32AsUnsignedLong -> {
                    visitInsn(I2L)
                    visitLdcInsn(0xFFFF_FFFFL)
                    visitInsn(LAND)
                    visitInsn(LRETURN)
                }

                FfiHandling.Address -> {
                    visitInsn(ARETURN)
                }
            }

            visitMaxs(0, 0)
            visitEnd()
        }

        if (setter != null) {
            implWriter.visitMethod(ACC_PUBLIC, setter.name, setter.descriptorString, null, null).apply {
                visitCode()

                visitVarInsn(ALOAD, 0)
                visitFieldInsn(
                    GETFIELD,
                    AbstractStructImpl.INTERNAL_NAME,
                    MEMORY_SEGMENT_INTERNAL_NAME,
                    MEMORY_SEGMENT_DESCRIPTOR,
                )

                visitLoadValueLayout(handling)

                visitLdcInsn(offset)

                when (handling) {
                    FfiHandling.I8AsByte, FfiHandling.I16AsShort, FfiHandling.I32AsInt -> {
                        visitVarInsn(ILOAD, 1)
                    }

                    FfiHandling.I64AsLong -> {
                        visitVarInsn(LLOAD, 1)
                    }

                    FfiHandling.F32AsFloat -> {
                        visitVarInsn(FLOAD, 1)
                    }

                    FfiHandling.F64AsDouble -> {
                        visitVarInsn(DLOAD, 1)
                    }

                    FfiHandling.I32AsSignedLong, FfiHandling.I32AsUnsignedLong -> {
                        visitVarInsn(LLOAD, 1)
                        visitInsn(L2I)
                    }

                    FfiHandling.Address -> {
                        visitVarInsn(ALOAD, 1)
                    }
                }

                when (handling) {
                    FfiHandling.I8AsByte -> visitMethodInsn(
                        INVOKEINTERFACE,
                        MEMORY_SEGMENT_INTERNAL_NAME,
                        "set",
                        "(${VALUE_LAYOUT_OF_BYTE_DESCRIPTOR}JB)V",
                        true,
                    )

                    FfiHandling.I16AsShort -> visitMethodInsn(
                        INVOKEINTERFACE,
                        MEMORY_SEGMENT_INTERNAL_NAME,
                        "set",
                        "(${VALUE_LAYOUT_OF_SHORT_DESCRIPTOR}JS)V",
                        true,
                    )

                    FfiHandling.I32AsInt, FfiHandling.I32AsSignedLong, FfiHandling.I32AsUnsignedLong -> visitMethodInsn(
                        INVOKEINTERFACE,
                        MEMORY_SEGMENT_INTERNAL_NAME,
                        "set",
                        "(${VALUE_LAYOUT_OF_INT_DESCRIPTOR}JI)V",
                        true,
                    )

                    FfiHandling.I64AsLong -> visitMethodInsn(
                        INVOKEINTERFACE,
                        MEMORY_SEGMENT_INTERNAL_NAME,
                        "set",
                        "(${VALUE_LAYOUT_OF_LONG_DESCRIPTOR}JJ)V",
                        true,
                    )

                    FfiHandling.F32AsFloat -> visitMethodInsn(
                        INVOKEINTERFACE,
                        MEMORY_SEGMENT_INTERNAL_NAME,
                        "set",
                        "(${VALUE_LAYOUT_OF_FLOAT_DESCRIPTOR}JF)V",
                        true,
                    )

                    FfiHandling.F64AsDouble -> visitMethodInsn(
                        INVOKEINTERFACE,
                        MEMORY_SEGMENT_INTERNAL_NAME,
                        "set",
                        "(${VALUE_LAYOUT_OF_DOUBLE_DESCRIPTOR}JD)V",
                        true,
                    )

                    FfiHandling.Address -> visitMethodInsn(
                        INVOKEINTERFACE,
                        MEMORY_SEGMENT_INTERNAL_NAME,
                        "set",
                        "(${VALUE_LAYOUT_OF_ADDRESS_DESCRIPTOR}J${MEMORY_ADDRESS_DESCRIPTOR})V",
                        true,
                    )
                }

                visitInsn(RETURN)
                visitMaxs(0, 0)
                visitEnd()
            }
        }
    }

    val finalPadding = size.forwardsAlignmentOffsetTo(alignment)

    if (finalPadding != 0L) {
        memberLayouts.add(paddingLayout(finalPadding timesExact 8))
        size += finalPadding
    }

    implWriter.visitEnd()

    val layout = structLayout(*memberLayouts.toTypedArray())

    val implClassData = StructClassData()

    val implLookup = definer.define(implWriter.toByteArray(), classData = implClassData)

    val implConstructor = implLookup
        .findConstructor(implLookup.lookupClass(), methodType(Void.TYPE, MemorySegment::class.java))
        .asType(methodType(Struct::class.java, MemorySegment::class.java))

    val typeImplName = "${definer.internalNamePrefix}${klass.simpleName}TypeImpl"
    val typeImplWriter = ClassWriter(COMPUTE_FRAMES)

    typeImplWriter.visit(V19, ACC_FINAL, typeImplName, null, AbstractStructTypeImpl.INTERNAL_NAME, null)

    typeImplWriter.visitMethod(ACC_PRIVATE, "<init>", "()V", null, null).apply {
        visitCode()

        visitVarInsn(ALOAD, 0)
        visitLdcInsn(LAYOUT_CONSTANT)
        visitMethodInsn(
            INVOKESPECIAL,
            AbstractStructTypeImpl.INTERNAL_NAME,
            "<init>",
            AbstractStructTypeImpl.CONSTRUCTOR_DESCRIPTOR,
            false,
        )

        visitInsn(RETURN)
        visitMaxs(0, 0)
        visitEnd()
    }

    typeImplWriter.visitMethod(
        ACC_PUBLIC,
        AbstractStructTypeImpl.WRAP_SAFE_NAME,
        AbstractStructTypeImpl.WRAP_SAFE_DESCRIPTOR,
        null,
        null,
    ).apply {
        visitCode()
        visitVarInsn(ALOAD, 1)
        visitInvokeDynamicInsn("_", AbstractStructTypeImpl.WRAP_SAFE_DESCRIPTOR, WRAP_BOOTSTRAP)
        visitInsn(ARETURN)
        visitMaxs(0, 0)
        visitEnd()
    }

    typeImplWriter.visitEnd()

    val typeImplLookup = definer.define(
        typeImplWriter.toByteArray(),
        classData = StructTypeClassData(implConstructor, layout),
    )

    @Suppress("UNCHECKED_CAST")
    val type = typeImplLookup
        .findConstructor(typeImplLookup.lookupClass(), methodType(Void.TYPE))
        .invoke() as StructType<S>

    return type.also {
        implClassData.type = it
    }
}

private fun MethodVisitor.visitLoadValueLayout(handling: FfiHandling) {
    when (handling) {
        FfiHandling.I8AsByte -> visitFieldInsn(
            GETSTATIC,
            VALUE_LAYOUT_INTERNAL_NAME,
            "JAVA_BYTE",
            VALUE_LAYOUT_OF_BYTE_DESCRIPTOR,
        )

        FfiHandling.I16AsShort -> visitFieldInsn(
            GETSTATIC,
            VALUE_LAYOUT_INTERNAL_NAME,
            "JAVA_SHORT",
            VALUE_LAYOUT_OF_SHORT_DESCRIPTOR,
        )

        FfiHandling.I32AsInt, FfiHandling.I32AsSignedLong, FfiHandling.I32AsUnsignedLong -> visitFieldInsn(
            GETSTATIC,
            VALUE_LAYOUT_INTERNAL_NAME,
            "JAVA_INT",
            VALUE_LAYOUT_OF_INT_DESCRIPTOR,
        )

        FfiHandling.I64AsLong -> visitFieldInsn(
            GETSTATIC,
            VALUE_LAYOUT_INTERNAL_NAME,
            "JAVA_LONG",
            VALUE_LAYOUT_OF_LONG_DESCRIPTOR,
        )

        FfiHandling.F32AsFloat -> visitFieldInsn(
            GETSTATIC,
            VALUE_LAYOUT_INTERNAL_NAME,
            "JAVA_FLOAT",
            VALUE_LAYOUT_OF_FLOAT_DESCRIPTOR,
        )

        FfiHandling.F64AsDouble -> visitFieldInsn(
            GETSTATIC,
            VALUE_LAYOUT_INTERNAL_NAME,
            "JAVA_DOUBLE",
            VALUE_LAYOUT_OF_DOUBLE_DESCRIPTOR,
        )

        FfiHandling.Address -> visitFieldInsn(
            GETSTATIC,
            VALUE_LAYOUT_INTERNAL_NAME,
            "ADDRESS",
            VALUE_LAYOUT_OF_ADDRESS_DESCRIPTOR,
        )
    }
}

internal abstract class AbstractStructImpl(@JvmField protected val segment: MemorySegment) : Struct {
    final override fun segment(): MemorySegment {
        return segment
    }

    companion object {
        val INTERNAL_NAME = AbstractStructImpl::class.java.internalName

        const val SEGMENT_FIELD_NAME = "segment"

        val CONSTRUCTOR_DESCRIPTOR = AbstractStructImpl::class.java.declaredConstructors.single().descriptorString

        val TYPE_NAME = Struct::type.javaMethod!!.name

        val TYPE_DESCRIPTOR = Struct::type.javaMethod!!.descriptorString
    }
}

internal abstract class AbstractStructTypeImpl<S : Struct>(final override val layout: GroupLayout) : StructType<S> {
    final override fun wrap(segment: MemorySegment): S {
        segment.requireSizedAndAlignedFor(layout)
        return wrapSafe(segment)
    }

    protected abstract fun wrapSafe(segment: MemorySegment): S

    companion object {
        val INTERNAL_NAME = AbstractStructTypeImpl::class.java.internalName

        val CONSTRUCTOR_DESCRIPTOR = AbstractStructTypeImpl::class.java.declaredConstructors.single().descriptorString

        val WRAP_SAFE_NAME = AbstractStructTypeImpl<*>::wrapSafe.javaMethod!!.name

        val WRAP_SAFE_DESCRIPTOR = AbstractStructTypeImpl<*>::wrapSafe.javaMethod!!.descriptorString
    }
}

internal fun wrapBootstrap(lookup: Lookup, name: String, type: MethodType): CallSite {
    return ConstantCallSite(classData(lookup, "_", StructTypeClassData::class.java).instanceConstructor)
}

internal fun layoutBootstrap(lookup: Lookup, name: String, type: Class<GroupLayout>): GroupLayout {
    return classData(lookup, "_", StructTypeClassData::class.java).layout
}

internal fun typeBootstrap(lookup: Lookup, name: String, type: Class<StructType<*>>): StructType<*> {
    return classData(lookup, "_", StructClassData::class.java).type
}

private class StructClassData {
    @Volatile lateinit var type: StructType<*>
}

private data class StructTypeClassData(val instanceConstructor: MethodHandle, val layout: GroupLayout)

private val WRAP_BOOTSTRAP = ::wrapBootstrap.javaMethod!!.let { javaMethod ->
    Handle(
        H_INVOKESTATIC,
        javaMethod.declaringClass.internalName,
        javaMethod.name,
        javaMethod.descriptorString,
        false,
    )
}

private val LAYOUT_CONSTANT = ::layoutBootstrap.javaMethod!!.let { javaMethod ->
    ConstantDynamic(
        "_",
        GroupLayout::class.java.descriptorString(),
        Handle(
            H_INVOKESTATIC,
            javaMethod.declaringClass.internalName,
            javaMethod.name,
            javaMethod.descriptorString,
            false,
        ),
    )
}

private val TYPE_CONSTANT = ::typeBootstrap.javaMethod!!.let { javaMethod ->
    ConstantDynamic(
        "_",
        StructType::class.java.descriptorString(),
        Handle(
            H_INVOKESTATIC,
            javaMethod.declaringClass.internalName,
            javaMethod.name,
            javaMethod.descriptorString,
            false,
        ),
    )
}

private val MEMORY_ADDRESS_DESCRIPTOR = MemoryAddress::class.java.descriptorString()

private val MEMORY_SEGMENT_INTERNAL_NAME = MemorySegment::class.java.internalName

private val MEMORY_SEGMENT_DESCRIPTOR = MemorySegment::class.java.descriptorString()

private val VALUE_LAYOUT_INTERNAL_NAME = ValueLayout::class.java.internalName

private val VALUE_LAYOUT_OF_BYTE_DESCRIPTOR = ValueLayout.OfByte::class.java.descriptorString()

private val VALUE_LAYOUT_OF_SHORT_DESCRIPTOR = ValueLayout.OfShort::class.java.descriptorString()

private val VALUE_LAYOUT_OF_INT_DESCRIPTOR = ValueLayout.OfInt::class.java.descriptorString()

private val VALUE_LAYOUT_OF_LONG_DESCRIPTOR = ValueLayout.OfLong::class.java.descriptorString()

private val VALUE_LAYOUT_OF_FLOAT_DESCRIPTOR = ValueLayout.OfFloat::class.java.descriptorString()

private val VALUE_LAYOUT_OF_DOUBLE_DESCRIPTOR = ValueLayout.OfDouble::class.java.descriptorString()

private val VALUE_LAYOUT_OF_ADDRESS_DESCRIPTOR = ValueLayout.OfAddress::class.java.descriptorString()
