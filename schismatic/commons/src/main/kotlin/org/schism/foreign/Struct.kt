package org.schism.foreign

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.ClassWriter.COMPUTE_FRAMES
import org.objectweb.asm.ConstantDynamic
import org.objectweb.asm.Handle
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
import org.schism.math.requireAlignedTo
import org.schism.reflect.descriptorString
import org.schism.reflect.internalName
import java.lang.foreign.GroupLayout
import java.lang.foreign.MemoryAddress
import java.lang.foreign.MemorySegment
import java.lang.foreign.MemorySession
import java.lang.foreign.SegmentAllocator
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

public fun <S : Struct> MemoryAddress.asStruct(type: StructType<S>, session: MemorySession): S {
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

    fields.values.forEach { property ->
        val getter = property.javaGetter
        val setter = (property as? KMutableProperty1)?.javaSetter

        requireNotNull(getter) {
            "$property has no getter"
        }

        val handling = ffiHandlingFor(property.returnType)

        val offset = size.alignForwardsTo(handling.memoryLayout.byteAlignment())
        size = offset + handling.memoryLayout.byteSize()
        alignment = maxOf(alignment, handling.memoryLayout.byteAlignment())

        implWriter.visitMethod(ACC_PUBLIC, getter.name, getter.descriptorString, null, null).apply {
            visitCode()
            visitVarInsn(ALOAD, 0)
            visitFieldInsn(GETFIELD, AbstractStructImpl.INTERNAL_NAME, "memory", MEMORY_SEGMENT_DESCRIPTOR)
            visitLdcInsn(offset)

            when (handling) {
                FfiHandling.I8AsByte -> {
                    visitMethodInsn(INVOKEINTERFACE, MEMORY_SEGMENT_INTERNAL_NAME, "getByte", "(J)B", true)
                    visitInsn(IRETURN)
                }

                FfiHandling.I16AsShort -> {
                    visitMethodInsn(INVOKEINTERFACE, MEMORY_SEGMENT_INTERNAL_NAME, "getShort", "(J)S", true)
                    visitInsn(IRETURN)
                }

                FfiHandling.I32AsInt -> {
                    visitMethodInsn(INVOKEINTERFACE, MEMORY_SEGMENT_INTERNAL_NAME, "getInt", "(J)I", true)
                    visitInsn(IRETURN)
                }

                FfiHandling.I64AsLong -> {
                    visitMethodInsn(INVOKEINTERFACE, MEMORY_SEGMENT_INTERNAL_NAME, "getLong", "(J)J", true)
                    visitInsn(LRETURN)
                }

                FfiHandling.F32AsFloat -> {
                    visitMethodInsn(INVOKEINTERFACE, MEMORY_SEGMENT_INTERNAL_NAME, "getFloat", "(J)F", true)
                    visitInsn(FRETURN)
                }

                FfiHandling.F64AsDouble -> {
                    visitMethodInsn(INVOKEINTERFACE, MEMORY_SEGMENT_INTERNAL_NAME, "getDouble", "(J)D", true)
                    visitInsn(DRETURN)
                }

                FfiHandling.I32AsSignedLong -> {
                    visitMethodInsn(INVOKEINTERFACE, MEMORY_SEGMENT_INTERNAL_NAME, "getInt", "(J)I", true)
                    visitInsn(I2L)
                    visitInsn(LRETURN)
                }

                FfiHandling.I32AsUnsignedLong -> {
                    visitMethodInsn(INVOKEINTERFACE, MEMORY_SEGMENT_INTERNAL_NAME, "getInt", "(J)I", true)
                    visitInsn(I2L)
                    visitLdcInsn(0xFFFF_FFFFL)
                    visitInsn(LAND)
                    visitInsn(LRETURN)
                }
            }

            visitMaxs(0, 0)
            visitEnd()
        }

        if (setter != null) {
            implWriter.visitMethod(ACC_PUBLIC, setter.name, setter.descriptorString, null, null).apply {
                visitCode()
                visitVarInsn(ALOAD, 0)
                visitFieldInsn(GETFIELD, AbstractStructImpl.INTERNAL_NAME, "memory", MEMORY_SEGMENT_DESCRIPTOR)

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
                }

                visitLdcInsn(offset)

                when (handling) {
                    FfiHandling.I8AsByte -> {
                        visitMethodInsn(INVOKEINTERFACE, MEMORY_SEGMENT_INTERNAL_NAME, "setByte", "(BJ)V", true)
                    }

                    FfiHandling.I16AsShort -> {
                        visitMethodInsn(INVOKEINTERFACE, MEMORY_SEGMENT_INTERNAL_NAME, "setShort", "(SJ)V", true)
                    }

                    FfiHandling.I32AsInt, FfiHandling.I32AsSignedLong, FfiHandling.I32AsUnsignedLong -> {
                        visitMethodInsn(INVOKEINTERFACE, MEMORY_SEGMENT_INTERNAL_NAME, "setInt", "(IJ)V", true)
                    }

                    FfiHandling.I64AsLong -> {
                        visitMethodInsn(INVOKEINTERFACE, MEMORY_SEGMENT_INTERNAL_NAME, "setLong", "(JJ)V", true)
                    }

                    FfiHandling.F32AsFloat -> {
                        visitMethodInsn(INVOKEINTERFACE, MEMORY_SEGMENT_INTERNAL_NAME, "setFloat", "(FJ)V", true)
                    }

                    FfiHandling.F64AsDouble -> {
                        visitMethodInsn(INVOKEINTERFACE, MEMORY_SEGMENT_INTERNAL_NAME, "setDouble", "(DJ)V", true)
                    }
                }

                visitInsn(RETURN)
                visitMaxs(0, 0)
                visitEnd()
            }
        }
    }

    size = size.alignForwardsTo(alignment)

    implWriter.visitEnd()

    val implLookup = definer.define(implWriter.toByteArray())

    val implConstructor = implLookup
        .findConstructor(implLookup.lookupClass(), methodType(Void.TYPE, MemorySegment::class.java))
        .asType(methodType(Struct::class.java, MemorySegment::class.java))

    val typeImplName = "${definer.internalNamePrefix}${klass.simpleName}TypeImpl"
    val typeImplWriter = ClassWriter(COMPUTE_FRAMES)

    typeImplWriter.visit(V19, ACC_FINAL, typeImplName, null, AbstractStructTypeImpl.INTERNAL_NAME, null)

    typeImplWriter.visitMethod(ACC_PRIVATE, "<init>", "()V", null, null).apply {
        visitCode()

        visitVarInsn(ALOAD, 0)
        visitLdcInsn(STRUCT_LAYOUT_CONSTANT)
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
        visitInvokeDynamicInsn("_", AbstractStructTypeImpl.WRAP_SAFE_DESCRIPTOR, STRUCT_WRAP_BOOTSTRAP)
        visitInsn(ARETURN)
        visitMaxs(0, 0)
        visitEnd()
    }

    val structClassData = StructClassData()

    typeImplWriter.visitEnd()

    val typeImplLookup = definer.define(
        typeImplWriter.toByteArray(),
        classData = StructTypeClassData(implConstructor, layout),
    )

    @Suppress("UNCHECKED_CAST")
    val type =  typeImplLookup
        .findConstructor(typeImplLookup.lookupClass(), methodType(Void.TYPE))
        .invoke() as StructType<S>

    return type.also {
        structClassData.type = it
    }
}

internal abstract class AbstractStructImpl(@JvmField protected val segment: MemorySegment) : Struct {
    final override fun segment(): MemorySegment {
        return segment
    }

    companion object {
        val INTERNAL_NAME = AbstractStructImpl::class.java.internalName

        val CONSTRUCTOR_DESCRIPTOR = AbstractStructImpl::class.java.declaredConstructors.single().descriptorString
    }
}

internal abstract class AbstractStructTypeImpl<S : Struct>(final override val layout: GroupLayout) : StructType<S> {
    final override fun wrap(segment: MemorySegment): S {
        if (segment.isNative) {
            segment.address().toRawLongValue().requireAlignedTo(layout.byteAlignment())
        }

        return wrapSafe(segment.asSlice(0, layout.byteSize()))
    }

    protected abstract fun wrapSafe(segment: MemorySegment): S

    companion object {
        val INTERNAL_NAME = AbstractStructTypeImpl::class.java.internalName

        val CONSTRUCTOR_DESCRIPTOR = AbstractStructTypeImpl::class.java.declaredConstructors.single().descriptorString

        val WRAP_SAFE_NAME = AbstractStructTypeImpl<*>::wrapSafe.javaMethod!!.name

        val WRAP_SAFE_DESCRIPTOR = AbstractStructTypeImpl<*>::wrapSafe.javaMethod!!.descriptorString
    }
}

internal fun structTypeBootstrap(lookup: Lookup, name: String, type: Class<StructType<*>>): StructType<*> {
    return classData(lookup, "_", StructClassData::class.java).type
}

internal fun structTypeWrapBootstrap(lookup: Lookup, name: String, type: MethodType): CallSite {
    return ConstantCallSite(classData(lookup, "_", StructTypeClassData::class.java).instanceConstructor)
}

internal fun structTypeLayoutBootstrap(lookup: Lookup, name: String, type: Class<GroupLayout>): GroupLayout {
    return classData(lookup, "_", StructTypeClassData::class.java).layout
}

private class StructClassData {
    @Volatile lateinit var type: StructType<*>
}

private data class StructTypeClassData(val instanceConstructor: MethodHandle, val layout: GroupLayout)

private val STRUCT_WRAP_BOOTSTRAP = ::structTypeWrapBootstrap.javaMethod!!.let { javaMethod ->
    Handle(
        H_INVOKESTATIC,
        javaMethod.declaringClass.internalName,
        javaMethod.name,
        javaMethod.descriptorString,
        false,
    )
}

private val STRUCT_LAYOUT_CONSTANT = ::structTypeLayoutBootstrap.javaMethod!!.let { javaMethod ->
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

private val STRUCT_TYPE_CONSTANT = ::structTypeBootstrap.javaMethod!!.let { javaMethod ->
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

private val MEMORY_SEGMENT_INTERNAL_NAME = MemorySegment::class.java.internalName

private val MEMORY_SEGMENT_DESCRIPTOR = MemorySegment::class.java.descriptorString()
