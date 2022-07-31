package org.schism.foreign

import kotlinx.atomicfu.locks.withLock
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
import org.objectweb.asm.Opcodes.INVOKEVIRTUAL
import org.objectweb.asm.Opcodes.IRETURN
import org.objectweb.asm.Opcodes.L2I
import org.objectweb.asm.Opcodes.LAND
import org.objectweb.asm.Opcodes.LLOAD
import org.objectweb.asm.Opcodes.LRETURN
import org.objectweb.asm.Opcodes.RETURN
import org.objectweb.asm.Opcodes.V19
import org.schism.invoke.internalNamePrefix
import org.schism.math.alignForwardsTo
import org.schism.math.forwardsAlignmentOffsetTo
import org.schism.math.plusExact
import org.schism.math.timesExact
import org.schism.reflect.descriptorString
import org.schism.reflect.internalName
import org.schism.util.asUnchecked
import org.schism.util.coerceAs
import java.lang.foreign.Addressable
import java.lang.foreign.GroupLayout
import java.lang.foreign.MemoryAddress
import java.lang.foreign.MemoryLayout
import java.lang.foreign.MemoryLayout.PathElement.groupElement
import java.lang.foreign.MemoryLayout.paddingLayout
import java.lang.foreign.MemoryLayout.structLayout
import java.lang.foreign.MemorySegment
import java.lang.foreign.MemorySession
import java.lang.foreign.SegmentAllocator
import java.lang.foreign.ValueLayout
import java.lang.invoke.CallSite
import java.lang.invoke.ConstantCallSite
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.lang.invoke.MethodType.methodType
import java.lang.invoke.VarHandle
import java.lang.reflect.Method
import java.util.TreeMap
import java.util.WeakHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaGetter
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.javaSetter

public interface Struct {
    public fun type(): StructType<*>

    public fun segment(): MemorySegment

    public companion object {
        internal val INTERNAL_NAME = Struct::class.java.internalName

        internal val DESCRIPTOR = Struct::class.java.descriptorString()

        internal val TYPE_METHOD_NAME = Struct::type.javaMethod!!.name

        internal val TYPE_METHOD_DESCRIPTOR = Struct::type.javaMethod!!.descriptorString

        internal val SEGMENT_METHOD_NAME = Struct::segment.javaMethod!!.name

        internal val SEGMENT_METHOD_DESCRIPTOR = Struct::segment.javaMethod!!.descriptorString
    }
}

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
public annotation class StructField(val index: Int)

public interface StructType<out S : Struct> {
    public val layout: GroupLayout

    public fun wrap(segment: MemorySegment): S

    public companion object {
        internal val INTERNAL_NAME = StructType::class.java.internalName

        internal val DESCRIPTOR = StructType::class.java.descriptorString()
    }
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
    return StructType(S::class, MethodHandles.lookup())
}

@PublishedApi
internal fun <S : Struct> StructType(klass: KClass<S>, lookup: MethodHandles.Lookup): StructType<S> {
    structTypeResolvingLock.withLock {
        resolvedStructTypes[klass.java]?.let {
            return it.asUnchecked()
        }

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

        val implName = "${lookup.internalNamePrefix()}${klass.simpleName}Impl"
        val implWriter = ClassWriter(COMPUTE_FRAMES)
        val memberLayouts = mutableListOf<MemoryLayout>()

        implWriter.visit(
            V19,
            ACC_FINAL,
            implName,
            null,
            AbstractStructImpl.INTERNAL_NAME,
            arrayOf(klass.java.internalName),
        )

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

        implWriter.visitMethod(ACC_PUBLIC, Struct.TYPE_METHOD_NAME, Struct.TYPE_METHOD_DESCRIPTOR, null, null).apply {
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

            val abiClass = AbiClass.fromType(property.returnType, lookup)

            if (abiClass !is AbiClass.Scalar) {
                TODO("Implement struct members")
            }

            val offset = size.alignForwardsTo(abiClass.layout.byteAlignment())

            if (offset != size) {
                memberLayouts.add(paddingLayout((offset - size) timesExact 8))
            }

            memberLayouts.add(abiClass.layout.withName(property.name))
            size = offset plusExact abiClass.layout.byteSize()
            alignment = maxOf(alignment, abiClass.layout.byteAlignment())

            val nativeVarHandleConstant = ConstantDynamic(
                property.name,
                VAR_HANDLE_INTERNAL_NAME,
                NATIVE_MEMBER_BOOTSTRAP,
            )

            implWriter.visitMethod(ACC_PUBLIC, getter.name, getter.descriptorString, null, null).apply {
                visitCode()

                visitLdcInsn(nativeVarHandleConstant)

                visitVarInsn(ALOAD, 0)

                visitFieldInsn(
                    GETFIELD,
                    AbstractStructImpl.INTERNAL_NAME,
                    AbstractStructImpl.SEGMENT_FIELD_NAME,
                    AbstractStructImpl.SEGMENT_FIELD_DESCRIPTOR,
                )

                val nativeGetDescriptor = when (abiClass) {
                    AbiClass.JvmByteNativeI8 -> {
                        GET_BYTE_DESCRIPTOR
                    }

                    AbiClass.JvmShortNativeI16 -> {
                        GET_SHORT_DESCRIPTOR
                    }

                    AbiClass.JvmIntNativeI32,
                    AbiClass.JvmLongNativeI32Sext,
                    AbiClass.JvmLongNativeI32Zext -> {
                        GET_INT_DESCRIPTOR
                    }

                    AbiClass.JvmLongNativeI64 -> {
                        GET_LONG_DESCRIPTOR
                    }

                    AbiClass.JvmFloatNativeF32 -> {
                        GET_FLOAT_DESCRIPTOR
                    }

                    AbiClass.JvmDoubleNativeF64 -> {
                        GET_DOUBLE_DESCRIPTOR
                    }

                    AbiClass.JvmAddressableNativeAddress,
                    AbiClass.JvmMemoryAddressNativeAddress -> {
                        GET_ADDRESS_DESCRIPTOR
                    }
                }

                visitMethodInsn(INVOKEVIRTUAL, VAR_HANDLE_INTERNAL_NAME, "get", nativeGetDescriptor, false)

                when (abiClass) {
                    AbiClass.JvmByteNativeI8,
                    AbiClass.JvmShortNativeI16,
                    AbiClass.JvmIntNativeI32 -> {
                        visitInsn(IRETURN)
                    }

                    AbiClass.JvmLongNativeI64 -> {
                        visitInsn(LRETURN)
                    }

                    AbiClass.JvmFloatNativeF32 -> {
                        visitInsn(FRETURN)
                    }

                    AbiClass.JvmDoubleNativeF64 -> {
                        visitInsn(DRETURN)
                    }

                    AbiClass.JvmLongNativeI32Sext -> {
                        visitInsn(I2L)
                        visitInsn(LRETURN)
                    }

                    AbiClass.JvmLongNativeI32Zext -> {
                        visitInsn(I2L)
                        visitLdcInsn(0xFFFF_FFFFL)
                        visitInsn(LAND)
                        visitInsn(LRETURN)
                    }

                    AbiClass.JvmAddressableNativeAddress,
                    AbiClass.JvmMemoryAddressNativeAddress -> {
                        visitInsn(ARETURN)
                    }
                }

                visitMaxs(0, 0)
                visitEnd()
            }

            if (setter != null) {
                implWriter.visitMethod(ACC_PUBLIC, setter.name, setter.descriptorString, null, null).apply {
                    visitCode()

                    visitLdcInsn(nativeVarHandleConstant)

                    visitVarInsn(ALOAD, 0)

                    visitFieldInsn(
                        GETFIELD,
                        AbstractStructImpl.INTERNAL_NAME,
                        AbstractStructImpl.SEGMENT_FIELD_NAME,
                        AbstractStructImpl.SEGMENT_FIELD_DESCRIPTOR
                    )

                    when (abiClass) {
                        AbiClass.JvmByteNativeI8,
                        AbiClass.JvmShortNativeI16,
                        AbiClass.JvmIntNativeI32 -> {
                            visitVarInsn(ILOAD, 1)
                        }

                        AbiClass.JvmLongNativeI64 -> {
                            visitVarInsn(LLOAD, 1)
                        }

                        AbiClass.JvmFloatNativeF32 -> {
                            visitVarInsn(FLOAD, 1)
                        }

                        AbiClass.JvmDoubleNativeF64 -> {
                            visitVarInsn(DLOAD, 1)
                        }

                        AbiClass.JvmLongNativeI32Sext,
                        AbiClass.JvmLongNativeI32Zext -> {
                            visitVarInsn(LLOAD, 1)
                            visitInsn(L2I)
                        }

                        AbiClass.JvmAddressableNativeAddress -> {
                            visitVarInsn(ALOAD, 1)

                            visitMethodInsn(
                                INVOKEINTERFACE,
                                ADDRESSABLE_INTERNAL_NAME,
                                ADDRESSABLE_ADDRESS_METHOD_NAME,
                                ADDRESSABLE_ADDRESS_METHOD_DESCRIPTOR,
                                true,
                            )
                        }

                        AbiClass.JvmMemoryAddressNativeAddress -> {
                            visitVarInsn(ALOAD, 1)
                        }
                    }

                    val nativeSetDescriptor = when (abiClass) {
                        AbiClass.JvmByteNativeI8 -> {
                            SET_BYTE_DESCRIPTOR
                        }

                        AbiClass.JvmShortNativeI16 -> {
                            SET_SHORT_DESCRIPTOR
                        }

                        AbiClass.JvmIntNativeI32,
                        AbiClass.JvmLongNativeI32Sext,
                        AbiClass.JvmLongNativeI32Zext -> {
                            SET_INT_DESCRIPTOR
                        }

                        AbiClass.JvmLongNativeI64 -> {
                            SET_LONG_DESCRIPTOR
                        }

                        AbiClass.JvmFloatNativeF32 -> {
                            SET_FLOAT_DESCRIPTOR
                        }

                        AbiClass.JvmDoubleNativeF64 -> {
                            SET_DOUBLE_DESCRIPTOR
                        }

                        AbiClass.JvmAddressableNativeAddress,
                        AbiClass.JvmMemoryAddressNativeAddress -> {
                            SET_ADDRESS_DESCRIPTOR
                        }
                    }

                    visitMethodInsn(INVOKEVIRTUAL, VAR_HANDLE_INTERNAL_NAME, "set", nativeSetDescriptor, false)

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

        val implLookup = lookup.defineHiddenClassWithClassData(implWriter.toByteArray(), implClassData, false)

        val implConstructor = implLookup
            .findConstructor(implLookup.lookupClass(), methodType(Void.TYPE, MemorySegment::class.java))
            .asType(methodType(Struct::class.java, MemorySegment::class.java))

        val typeImplName = "${lookup.internalNamePrefix()}${klass.simpleName}TypeImpl"
        val typeImplWriter = ClassWriter(COMPUTE_FRAMES)

        typeImplWriter.visit(V19, ACC_FINAL, typeImplName, null, AbstractStructTypeImpl::class.java.internalName, null)

        typeImplWriter.visitMethod(
            ACC_PRIVATE,
            "<init>",
            AbstractStructImpl::class.java.constructors.single().descriptorString,
            null,
            null,
        ).apply {
            visitCode()

            visitVarInsn(ALOAD, 0)
            visitLdcInsn(LAYOUT_CONSTANT)
            visitMethodInsn(
                INVOKESPECIAL,
                AbstractStructTypeImpl::class.java.internalName,
                "<init>",
                AbstractStructTypeImpl::class.java.constructors.single().descriptorString,
                false,
            )

            visitInsn(RETURN)
            visitMaxs(0, 0)
            visitEnd()
        }

        typeImplWriter.visitMethod(
            ACC_PUBLIC,
            AbstractStructTypeImpl::wrap
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

        val type = typeImplLookup
            .findConstructor(typeImplLookup.lookupClass(), methodType(Void.TYPE))
            .invoke() as StructType<S>

        return type.also {
            implClassData.type = it
        }
    }
}

private val structTypeResolvingLock = ReentrantLock()

private val resolvedStructTypes = WeakHashMap<Class<out Struct>, StructType<*>>()

internal abstract class AbstractStructImpl(@JvmField protected val segment: MemorySegment) : Struct {
    final override fun segment(): MemorySegment {
        return segment
    }

    companion object {
        val INTERNAL_NAME = AbstractStructImpl::class.java.internalName

        val CONSTRUCTOR_DESCRIPTOR = AbstractStructImpl::class.java.constructors.single().descriptorString

        val SEGMENT_FIELD_NAME = coerceAs<KProperty<*>>(AbstractStructImpl::segment).javaField!!.name

        val SEGMENT_FIELD_DESCRIPTOR = MemorySegment::class.java.descriptorString()
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

        val CONSTRUCTOR_DESCRIPTOR = AbstractStructTypeImpl::class.java.constructors.single().descriptorString
    }
}

private class StructClassData {
    @Volatile lateinit var type: StructType<*>
}

private data class StructTypeClassData(val instanceConstructor: MethodHandle, val layout: GroupLayout)

internal fun nativeMemberBootstrap(lookup: MethodHandles.Lookup, name: String, type: Class<VarHandle>): VarHandle {
    return MethodHandles.classData(lookup, "_", StructClassData::class.java)
        .type
        .layout
        .varHandle(groupElement(name))
}

internal fun wrapBootstrap(lookup: MethodHandles.Lookup, name: String, type: MethodType): CallSite {
    return ConstantCallSite(MethodHandles.classData(lookup, "_", StructTypeClassData::class.java).instanceConstructor)
}

internal fun layoutBootstrap(lookup: MethodHandles.Lookup, name: String, type: Class<GroupLayout>): GroupLayout {
    return MethodHandles.classData(lookup, "_", StructTypeClassData::class.java).layout
}

internal fun typeBootstrap(lookup: MethodHandles.Lookup, name: String, type: Class<StructType<*>>): StructType<*> {
    return MethodHandles.classData(lookup, "_", StructClassData::class.java).type
}

private fun Method.asBootstrapHandle(): Handle {
    return Handle(
        H_INVOKESTATIC,
        declaringClass.internalName,
        name,
        descriptorString,
        false,
    )
}

private fun methodDescriptor(returnType: Class<*>, vararg argumentTypes: Class<*>): String {
    return methodType(returnType, argumentTypes).descriptorString()
}

private val VAR_HANDLE_INTERNAL_NAME = VarHandle::class.java.internalName

private val VAR_HANDLE_DESCRIPTOR = VarHandle::class.java.descriptorString()

private val MEMORY_SEGMENT_INTERNAL_NAME = MemorySegment::class.java.internalName

private val MEMORY_SEGMENT_DESCRIPTOR = MemorySegment::class.java.descriptorString()

private val ADDRESSABLE_INTERNAL_NAME = Addressable::class.java.internalName

private val ADDRESSABLE_ADDRESS_METHOD_NAME = Addressable::address.javaMethod!!.name

private val ADDRESSABLE_ADDRESS_METHOD_DESCRIPTOR = Addressable::address.javaMethod!!.descriptorString

private val GET_BYTE_DESCRIPTOR = methodDescriptor(
    Byte::class.java,
    MemorySegment::class.java,
)

private val GET_SHORT_DESCRIPTOR = methodDescriptor(
    Short::class.java,
    MemorySegment::class.java,
)

private val GET_INT_DESCRIPTOR = methodDescriptor(
    Int::class.java,
    MemorySegment::class.java,
)

private val GET_LONG_DESCRIPTOR = methodDescriptor(
    Long::class.java,
    MemorySegment::class.java,
)

private val GET_FLOAT_DESCRIPTOR = methodDescriptor(
    Float::class.java,
    MemorySegment::class.java,
)

private val GET_DOUBLE_DESCRIPTOR = methodDescriptor(
    Double::class.java,
    MemorySegment::class.java,
)

private val GET_ADDRESS_DESCRIPTOR = methodDescriptor(
    MemoryAddress::class.java,
    MemorySegment::class.java,
)

private val SET_BYTE_DESCRIPTOR = methodDescriptor(
    Void.TYPE,
    MemorySegment::class.java,
    Byte::class.java,
)

private val SET_SHORT_DESCRIPTOR = methodDescriptor(
    Void.TYPE,
    MemorySegment::class.java,
    Short::class.java,
)

private val SET_INT_DESCRIPTOR = methodDescriptor(
    Void.TYPE,
    MemorySegment::class.java,
    Int::class.java,
)

private val SET_LONG_DESCRIPTOR = methodDescriptor(
    Void.TYPE,
    MemorySegment::class.java,
    Long::class.java,
)

private val SET_FLOAT_DESCRIPTOR = methodDescriptor(
    Void.TYPE,
    MemorySegment::class.java,
    Float::class.java,
)

private val SET_DOUBLE_DESCRIPTOR = methodDescriptor(
    Void.TYPE,
    MemorySegment::class.java,
    Double::class.java,
)

private val SET_ADDRESS_DESCRIPTOR = methodDescriptor(
    Void.TYPE,
    MemorySegment::class.java,
    MemoryAddress::class.java,
)

private val NATIVE_MEMBER_BOOTSTRAP = ::nativeMemberBootstrap.javaMethod!!.asBootstrapHandle()

private val WRAP_BOOTSTRAP = ::wrapBootstrap.javaMethod!!.asBootstrapHandle()

private val LAYOUT_CONSTANT = ::layoutBootstrap.javaMethod!!.asBootstrapHandle()

private val TYPE_CONSTANT = ConstantDynamic(
    "_",
    StructType.DESCRIPTOR,
    ::typeBootstrap.javaMethod!!.asBootstrapHandle(),
)
