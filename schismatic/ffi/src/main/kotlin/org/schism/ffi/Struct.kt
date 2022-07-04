package org.schism.ffi

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.ClassWriter.COMPUTE_FRAMES
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.ACC_FINAL
import org.objectweb.asm.Opcodes.ACC_PRIVATE
import org.objectweb.asm.Opcodes.ALOAD
import org.objectweb.asm.Opcodes.ARETURN
import org.objectweb.asm.Opcodes.DLOAD
import org.objectweb.asm.Opcodes.DRETURN
import org.objectweb.asm.Opcodes.DUP
import org.objectweb.asm.Opcodes.FLOAD
import org.objectweb.asm.Opcodes.FRETURN
import org.objectweb.asm.Opcodes.GETFIELD
import org.objectweb.asm.Opcodes.I2L
import org.objectweb.asm.Opcodes.ILOAD
import org.objectweb.asm.Opcodes.INVOKEINTERFACE
import org.objectweb.asm.Opcodes.INVOKESPECIAL
import org.objectweb.asm.Opcodes.IRETURN
import org.objectweb.asm.Opcodes.L2I
import org.objectweb.asm.Opcodes.LAND
import org.objectweb.asm.Opcodes.LLOAD
import org.objectweb.asm.Opcodes.LRETURN
import org.objectweb.asm.Opcodes.PUTFIELD
import org.objectweb.asm.Opcodes.V19
import org.objectweb.asm.Type.getDescriptor
import org.objectweb.asm.Type.getInternalName
import org.objectweb.asm.Type.getMethodDescriptor
import org.schism.math.alignForwardsTo
import org.schism.math.isAlignedTo
import org.schism.memory.Memory
import org.schism.memory.NativeAddress
import org.schism.memory.nativeMemory
import java.lang.constant.ConstantDescs
import java.lang.invoke.CallSite
import java.lang.invoke.ConstantCallSite
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodHandles.Lookup
import java.lang.invoke.MethodType
import java.lang.invoke.MethodType.methodType
import java.lang.reflect.Modifier
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaGetter
import kotlin.reflect.jvm.javaSetter
import kotlin.reflect.typeOf

public interface Struct {
    public fun memory(): Memory

    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    public annotation class FieldOrder(vararg val value: String)

    public companion object
}

public interface StructType<out S : Struct> {
    public val size: Long

    public val alignment: Long

    public operator fun invoke(memory: Memory): S

    public companion object
}

internal object GeneratedStructTypes : ClassValue<StructType<*>>() {
    override fun computeValue(type: Class<*>): StructType<*> {
        TODO("Not yet implemented")
    }
}

internal fun structConstructorBootstrap(lookup: Lookup, name: String, type: MethodType): CallSite {
    val constructorHandle = MethodHandles.classData(lookup, ConstantDescs.DEFAULT_NAME, MethodHandle::class.java)
    return ConstantCallSite(constructorHandle)
}

internal abstract class AbstractStructImpl(@JvmField protected val memory: Memory) : Struct {
    final override fun memory(): Memory {
        return memory
    }
}

internal abstract class AbstractStructTypeImpl<S : Struct>(
    final override val size: Long,
    final override val alignment: Long,
) : StructType<S> {
    final override fun invoke(memory: Memory): S {
        require(memory.size == size)
        require(!memory.isNative || memory.startAddress.toBits().isAlignedTo(alignment))
        return createStruct(memory)
    }

    protected abstract fun createStruct(memory: Memory): S
}

private val LOOKUP = MethodHandles.lookup()

@Suppress("NOTHING_TO_INLINE")
public inline operator fun <S : Struct> StructType<S>.invoke(address: NativeAddress): S {
    return invoke(nativeMemory(address, size))
}

public inline fun <reified S : Struct> createStructType(vararg members: KProperty1<in S, *>): StructType<S> {
    return createStructType(MethodHandles.lookup(), S::class, *members)
}

public inline fun <reified S : Struct> createStructType(members: List<KProperty1<in S, *>>): StructType<S> {
    return createStructType(MethodHandles.lookup(), S::class, members)
}

public fun <S : Struct> createStructType(
    lookup: Lookup,
    klass: KClass<S>,
    vararg members: KProperty1<in S, *>,
): StructType<S> {
    return createStructType(lookup, klass, members.asList())
}

public fun <S : Struct> createStructType(
    lookup: Lookup,
    klass: KClass<S>,
    members: List<KProperty1<in S, *>>,
): StructType<S> {
    klass.memberProperties

    require(klass.java.isInterface) {
        "$klass is not an interface type"
    }

    require(klass.isSubclassOf(Struct::class)) {
        "$klass is not a subtype of ${Struct::class.simpleName}"
    }

    var size = 0L
    var alignment = 1L

    val packagePrefix = lookup.lookupPackagePrefix()

    val implInternalName = "${packagePrefix}StructImpl"

    val implWriter = ClassWriter(COMPUTE_FRAMES)

    implWriter.visit(V19, ACC_FINAL, implInternalName, null, "java/lang/Object", arrayOf(getInternalName(klass.java)))

    implWriter.visitField(ACC_PRIVATE or ACC_FINAL, "memory", MEMORY_DESCRIPTOR, null, null).apply {
        visitEnd()
    }

    implWriter.visitMethod(ACC_PRIVATE, "<init>", STRUCT_IMPL_CONSTRUCTOR_DESCRIPTOR, null, null).apply {
        visitCode()
        visitVarInsn(ALOAD, 0)
        visitInsn(DUP)
        visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false)
        visitVarInsn(ALOAD, 1)
        visitFieldInsn(PUTFIELD, implInternalName, "memory", MEMORY_DESCRIPTOR)
        visitEnd()
    }

    implWriter.visitMethod(0, "memory", "()$MEMORY_DESCRIPTOR", null, null).apply {
        visitCode()
        visitVarInsn(ALOAD, 0)
        visitFieldInsn(GETFIELD, implInternalName, "memory", MEMORY_DESCRIPTOR)
        visitInsn(ARETURN)
    }

    for (member in members) {
        val getter = member.javaGetter
        val setter = (member as? KMutableProperty1)?.javaSetter

        if (getter == null) {
            throw UnsupportedOperationException("Member $member has no getter")
        }

        if (!Modifier.isAbstract(getter.modifiers)) {
            throw UnsupportedOperationException("Member $member has a non-abstract getter")
        }

        if (setter != null && !Modifier.isAbstract(setter.modifiers)) {
            throw UnsupportedOperationException("Member $member has a non-abstract setter")
        }

        val offset: Long
        val visitGetterInvokeAndReturn: MethodVisitor.() -> Unit
        val visitSetterArgLoad: MethodVisitor.() -> Unit
        val visitSetterInvoke: MethodVisitor.() -> Unit

        val type = member.returnType

        when {
            type == typeOf<Byte>() || type == typeOf<UByte>() -> {
                offset = size
                size = offset + 1

                visitGetterInvokeAndReturn = {
                    visitMethodInsn(INVOKEINTERFACE, MEMORY_INTERNAL_NAME, "getByte", "(J)B", true)
                    visitInsn(IRETURN)
                }

                visitSetterArgLoad = {
                    visitVarInsn(ILOAD, 1)
                }

                visitSetterInvoke = {
                    visitMethodInsn(INVOKEINTERFACE, MEMORY_INTERNAL_NAME, "setByte", "(BJ)V", true)
                }
            }

            type == typeOf<Short>() || type == typeOf<UShort>() -> {
                offset = size.alignForwardsTo(2)
                size = offset + 2
                alignment = maxOf(alignment, 2)

                visitGetterInvokeAndReturn = {
                    visitMethodInsn(INVOKEINTERFACE, MEMORY_INTERNAL_NAME, "getShort", "(J)S", true)
                    visitInsn(IRETURN)
                }

                visitSetterArgLoad = {
                    visitVarInsn(ILOAD, 1)
                }

                visitSetterInvoke = {
                    visitMethodInsn(INVOKEINTERFACE, MEMORY_INTERNAL_NAME, "setShort", "(SJ)V", true)
                }
            }

            type == typeOf<Int>() || type == typeOf<UInt>() -> {
                offset = size.alignForwardsTo(4)
                size = offset + 4
                alignment = maxOf(alignment, 4)

                visitGetterInvokeAndReturn = {
                    visitMethodInsn(INVOKEINTERFACE, MEMORY_INTERNAL_NAME, "getInt", "(J)I", true)
                    visitInsn(IRETURN)
                }

                visitSetterArgLoad = {
                    visitVarInsn(ILOAD, 1)
                }

                visitSetterInvoke = {
                    visitMethodInsn(INVOKEINTERFACE, MEMORY_INTERNAL_NAME, "setInt", "(IJ)V", true)
                }
            }

            type == typeOf<Long>() || type == typeOf<ULong>()
                || (!C_LONG_IS_4_BYTES
                    && (type == typeOf<CLong>() || type == typeOf<CUnsignedLong>()))
                || (!ADDRESS_IS_4_BYTES
                    && (type == typeOf<CSizeT>() || type == typeOf<CPtrDiffT>() || type == typeOf<NativeAddress>()))
            -> {
                offset = size.alignForwardsTo(8)
                size = offset + 8
                alignment = maxOf(alignment, 8)

                visitGetterInvokeAndReturn = {
                    visitMethodInsn(INVOKEINTERFACE, MEMORY_INTERNAL_NAME, "getLong", "(J)J", true)
                    visitInsn(LRETURN)
                }

                visitSetterArgLoad = {
                    visitVarInsn(LLOAD, 1)
                }

                visitSetterInvoke = {
                    visitMethodInsn(INVOKEINTERFACE, MEMORY_INTERNAL_NAME, "setLong", "(JJ)V", true)
                }
            }

            type == typeOf<Float>() -> {
                offset = size.alignForwardsTo(4)
                size = offset + 4
                alignment = maxOf(alignment, 4)

                visitGetterInvokeAndReturn = {
                    visitMethodInsn(INVOKEINTERFACE, MEMORY_INTERNAL_NAME, "getFloat", "(J)F", true)
                    visitInsn(FRETURN)
                }

                visitSetterArgLoad = {
                    visitVarInsn(FLOAD, 1)
                }

                visitSetterInvoke = {
                    visitMethodInsn(INVOKEINTERFACE, MEMORY_INTERNAL_NAME, "setFloat", "(FJ)V", true)
                }
            }

            type == typeOf<Double>() -> {
                offset = size.alignForwardsTo(8)
                size = offset + 8
                alignment = maxOf(alignment, 8)

                visitGetterInvokeAndReturn = {
                    visitMethodInsn(INVOKEINTERFACE, MEMORY_INTERNAL_NAME, "getDouble", "(J)D", true)
                    visitInsn(DRETURN)
                }

                visitSetterArgLoad = {
                    visitVarInsn(DLOAD, 1)
                }

                visitSetterInvoke = {
                    visitMethodInsn(INVOKEINTERFACE, MEMORY_INTERNAL_NAME, "setDouble", "(DJ)V", true)
                }
            }

            (C_LONG_IS_4_BYTES && type == typeOf<CLong>()) || (ADDRESS_IS_4_BYTES && type == typeOf<CPtrDiffT>()) -> {
                offset = size.alignForwardsTo(4)
                size = offset + 4
                alignment = maxOf(alignment, 4)

                visitGetterInvokeAndReturn = {
                    visitMethodInsn(INVOKEINTERFACE, MEMORY_INTERNAL_NAME, "getInt", "(J)I", true)
                    visitInsn(I2L)
                    visitInsn(LRETURN)
                }

                visitSetterArgLoad = {
                    visitVarInsn(LLOAD, 1)
                    visitInsn(L2I)
                }

                visitSetterInvoke = {
                    visitMethodInsn(INVOKEINTERFACE, MEMORY_INTERNAL_NAME, "setInt", "(IJ)V", true)
                }
            }

            (C_LONG_IS_4_BYTES && type == typeOf<CUnsignedLong>())
                || (ADDRESS_IS_4_BYTES && (type == typeOf<CSizeT>() || type == typeOf<NativeAddress>()))
            -> {
                offset = size.alignForwardsTo(4)
                size = offset + 4
                alignment = maxOf(alignment, 4)

                visitGetterInvokeAndReturn = {
                    visitMethodInsn(INVOKEINTERFACE, MEMORY_INTERNAL_NAME, "getInt", "(J)I", true)
                    visitInsn(I2L)
                    visitLdcInsn(0xFFFF_FFFFL)
                    visitInsn(LAND)
                    visitInsn(LRETURN)
                }

                visitSetterArgLoad = {
                    visitVarInsn(LLOAD, 1)
                    visitInsn(L2I)
                }

                visitSetterInvoke = {
                    visitMethodInsn(INVOKEINTERFACE, MEMORY_INTERNAL_NAME, "setInt", "(IJ)V", true)
                }
            }

            else -> {
                throw UnsupportedOperationException("Unsupported property type: $type")
            }
        }

        implWriter.visitMethod(0, getter.name, getMethodDescriptor(getter), null, null).apply {
            visitCode()
            visitVarInsn(ALOAD, 0)
            visitFieldInsn(GETFIELD, implInternalName, "memory", MEMORY_DESCRIPTOR)
            visitLdcInsn(offset)
            visitGetterInvokeAndReturn()
            visitEnd()
        }

        if (setter != null) {
            implWriter.visitMethod(0, setter.name, getMethodDescriptor(setter), null, null).apply {
                visitCode()
                visitVarInsn(ALOAD, 0)
                visitFieldInsn(GETFIELD, implInternalName, "memory", MEMORY_DESCRIPTOR)
                visitSetterArgLoad()
                visitLdcInsn(offset)
                visitSetterInvoke()
                visitEnd()
            }
        }
    }

    size = size.alignForwardsTo(alignment)

    implWriter.visitEnd()

    val implLookup = lookup.defineHiddenClass(implWriter.toByteArray(), false)

    val implConstructor = implLookup.findConstructor(
        implLookup.lookupClass(),
        methodType(Void.TYPE, Memory::class.java),
    )

    val typeImplInternalName = "${packagePrefix}StructTypeImpl"
    val typeImplWriter = ClassWriter(COMPUTE_FRAMES)

    typeImplWriter.visit(
        V19,
        ACC_FINAL,
        typeImplInternalName,
        null,
        "java/lang/Object",
        arrayOf(STRUCT_TYPE_INTERNAL_NAME),
    )

    typeImplWriter.visitMethod(ACC_PRIVATE, "<init>", "()V", null, null).apply {
        visitCode()
        visitInsn(ALOAD)
        visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false)
        visitEnd()
    }

    typeImplWriter.visitMethod(0, "getSize", "()J", null, null).apply {
        visitCode()
        visitLdcInsn(size)
        visitInsn(LRETURN)
        visitEnd()
    }

    typeImplWriter.visitMethod(0, "getAlignment", "()J", null, null).apply {
        visitCode()
        visitLdcInsn(alignment)
        visitInsn(LRETURN)
        visitEnd()
    }

    typeImplWriter.visitMethod(0, "invoke", STRUCT_TYPE_INVOKE_DESCRIPTOR, null, null).apply {
        visitCode()
        visitVarInsn(ALOAD, 1)
        // visitInvokeDynamicInsn("invoke", "()")
        visitInsn(ARETURN)
    }

    typeImplWriter.visitEnd()
    val typeImplLookup = lookup.defineHiddenClassWithClassData(typeImplWriter.toByteArray(), implConstructor, false)
    val typeImplConstructor = typeImplLookup.findConstructor(typeImplLookup.lookupClass(), methodType(Void.TYPE))
    return (@Suppress("UNCHECKED_CAST") (typeImplConstructor.invoke() as StructType<S>))
}

private val MEMORY_INTERNAL_NAME = getInternalName(Memory::class.java)

private val MEMORY_DESCRIPTOR = getDescriptor(Memory::class.java)

private val STRUCT_TYPE_INVOKE_DESCRIPTOR = "($MEMORY_DESCRIPTOR)${getDescriptor(Struct::class.java)}"

private val STRUCT_TYPE_INTERNAL_NAME = getInternalName(StructType::class.java)

private val STRUCT_IMPL_CONSTRUCTOR_DESCRIPTOR = "($MEMORY_DESCRIPTOR)V"
