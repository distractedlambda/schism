package org.schism.ffi

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.ClassWriter.COMPUTE_FRAMES
import org.objectweb.asm.Handle
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.ACC_FINAL
import org.objectweb.asm.Opcodes.ACC_PRIVATE
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
import org.objectweb.asm.Opcodes.V19
import org.objectweb.asm.Type.getConstructorDescriptor
import org.objectweb.asm.Type.getDescriptor
import org.objectweb.asm.Type.getInternalName
import org.objectweb.asm.Type.getMethodDescriptor
import org.schism.math.alignForwardsTo
import org.schism.math.isAlignedTo
import org.schism.memory.Memory
import org.schism.memory.NativeAddress
import org.schism.memory.allocateNativeMemory
import org.schism.memory.nativeMemory
import org.schism.memory.withNativeMemory
import java.lang.invoke.CallSite
import java.lang.invoke.ConstantCallSite
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodHandles.Lookup
import java.lang.invoke.MethodHandles.Lookup.ClassOption.NESTMATE
import java.lang.invoke.MethodHandles.classData
import java.lang.invoke.MethodType
import java.lang.invoke.MethodType.methodType
import java.lang.reflect.Modifier
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaGetter
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.javaSetter
import kotlin.reflect.typeOf

public interface Struct {
    public fun memory(): Memory

    public interface Type<out S : Struct> {
        public val size: Long

        public val alignment: Long

        public operator fun invoke(memory: Memory): S

        public companion object
    }

    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    public annotation class Fields(vararg val value: String)

    public companion object {
        public fun <S : Struct> type(clazz: Class<S>): Type<S> {
            return (@Suppress("UNCHECKED_CAST") (GeneratedStructTypes[clazz] as Type<S>))
        }

        public inline fun <reified S : Struct> type(): Type<S> {
            return type(S::class.java)
        }
    }
}

public operator fun <S : Struct> Struct.Type<S>.invoke(address: NativeAddress): S {
    return invoke(nativeMemory(address, size))
}

public fun <S : Struct> Struct.Type<S>.allocate(): S {
    return invoke(allocateNativeMemory(size))
}

@OptIn(ExperimentalContracts::class)
public inline fun <S : Struct, R> Struct.Type<S>.withAllocated(block: (S) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return withNativeMemory(size) { memory ->
        block(invoke(memory))
    }
}

private object GeneratedStructTypes : ClassValue<Struct.Type<*>>() {
    override fun computeValue(type: Class<*>): Struct.Type<*> {
        return generateStructType(type)
    }
}

private fun generateStructType(clazz: Class<*>): Struct.Type<*> {
    val klass = clazz.kotlin

    require(clazz.isInterface) {
        "$klass is not an interface type"
    }

    require(klass.isSubclassOf(Struct::class)) {
        "$klass does not extend ${Struct::class.simpleName}"
    }

    val fieldNames = requireNotNull(klass.findAnnotation<Struct.Fields>()?.value) {
        "$klass is missing a ${Struct.Fields::class.simpleName} annotation"
    }

    var size = 0L
    var alignment = 1L

    val implWriter = ClassWriter(COMPUTE_FRAMES)

    implWriter.visit(
        V19,
        ACC_FINAL,
        "org/schism/ffi/StructImpl",
        null,
        AbstractStructImpl.INTERNAL_NAME,
        arrayOf(getInternalName(clazz)),
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

        visitEnd()
    }

    for (fieldName in fieldNames) {
        val member = requireNotNull(klass.memberProperties.firstOrNull { it.name == fieldName }) {
            "$klass does not have a member property named '$fieldName'"
        }

        val getter = member.javaGetter
        val setter = (member as? KMutableProperty1)?.javaSetter

        requireNotNull(getter) {
            "$member has no getter"
        }

        require(Modifier.isAbstract(getter.modifiers)) {
            "$member has a non-abstract getter"
        }

        require(setter == null || Modifier.isAbstract(setter.modifiers)) {
            "$member has a non-abstract setter"
        }

        val offset: Long
        val visitGetterInvokeAndReturn: MethodVisitor.() -> Unit
        val visitSetterArgLoad: MethodVisitor.() -> Unit
        val visitSetterInvoke: MethodVisitor.() -> Unit

        // FIXME: clean the duplicated code
        when (member.returnType) {
            typeOf<Byte>(), typeOf<UByte>() -> {
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

            typeOf<Short>(), typeOf<UShort>() -> {
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

            typeOf<Int>(), typeOf<UInt>() -> {
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

            typeOf<Long>(), typeOf<ULong>() -> {
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

            typeOf<Float>() -> {
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

            typeOf<Double>() -> {
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

            typeOf<CLong>() -> {
                if (C_LONG_IS_4_BYTES) {
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
                } else {
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
            }

            typeOf<CUnsignedLong>() -> {
                if (C_LONG_IS_4_BYTES) {
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
                } else {
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
            }

            typeOf<CPtrDiffT>() -> {
                if (ADDRESS_IS_4_BYTES) {
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
                } else {
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
            }

            typeOf<CSizeT>(), typeOf<NativeAddress>() -> {
                if (ADDRESS_IS_4_BYTES) {
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
                } else {
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
            }

            else -> {
                throw UnsupportedOperationException("Unsupported field type: ${member.returnType}")
            }
        }

        implWriter.visitMethod(0, getter.name, getMethodDescriptor(getter), null, null).apply {
            visitCode()
            visitVarInsn(ALOAD, 0)
            visitFieldInsn(GETFIELD, AbstractStructImpl.INTERNAL_NAME, "memory", MEMORY_DESCRIPTOR)
            visitLdcInsn(offset)
            visitGetterInvokeAndReturn()
            visitEnd()
        }

        if (setter != null) {
            implWriter.visitMethod(0, setter.name, getMethodDescriptor(setter), null, null).apply {
                visitCode()
                visitVarInsn(ALOAD, 0)
                visitFieldInsn(GETFIELD, AbstractStructImpl.INTERNAL_NAME, "memory", MEMORY_DESCRIPTOR)
                visitSetterArgLoad()
                visitLdcInsn(offset)
                visitSetterInvoke()
                visitEnd()
            }
        }
    }

    size = size.alignForwardsTo(alignment)

    implWriter.visitEnd()

    val implLookup = LOOKUP.defineHiddenClass(implWriter.toByteArray(), false, NESTMATE)

    val implConstructor = implLookup.findConstructor(
        implLookup.lookupClass(),
        methodType(Void.TYPE, Memory::class.java),
    )

    val typeImplWriter = ClassWriter(COMPUTE_FRAMES)

    typeImplWriter.visit(
        V19,
        ACC_FINAL,
        "org/schism/ffi/StructTypeImpl",
        null,
        AbstractStructTypeImpl.INTERNAL_NAME,
        null,
    )

    typeImplWriter.visitMethod(ACC_PRIVATE, "<init>", "()V", null, null).apply {
        visitCode()

        visitInsn(ALOAD)
        visitLdcInsn(size)
        visitLdcInsn(alignment)
        visitMethodInsn(
            INVOKESPECIAL,
            AbstractStructTypeImpl.INTERNAL_NAME,
            "<init>",
            AbstractStructTypeImpl.CONSTRUCTOR_DESCRIPTOR,
            false,
        )

        visitEnd()
    }

    typeImplWriter.visitMethod(
        0,
        AbstractStructTypeImpl.CREATE_STRUCT_NAME,
        AbstractStructTypeImpl.CREATE_STRUCT_DESCRIPTOR,
        null,
        null,
    ).apply {
        visitCode()
        visitVarInsn(ALOAD, 1)
        visitInvokeDynamicInsn("_", implConstructor.type().descriptorString(), STRUCT_FACTORY_BOOTSTRAP_HANDLE)
        visitInsn(ARETURN)
    }

    typeImplWriter.visitEnd()

    val typeImplLookup = LOOKUP.defineHiddenClassWithClassData(
        typeImplWriter.toByteArray(),
        implConstructor,
        false,
        NESTMATE,
    )

    val typeImplConstructor = typeImplLookup.findConstructor(typeImplLookup.lookupClass(), methodType(Void.TYPE))

    return (@Suppress("UNCHECKED_CAST") (typeImplConstructor.invoke() as Struct.Type<*>))
}

private abstract class AbstractStructImpl(@JvmField protected val memory: Memory) : Struct {
    final override fun memory(): Memory {
        return memory
    }

    companion object {
        val INTERNAL_NAME = getInternalName(AbstractStructImpl::class.java)!!

        val CONSTRUCTOR_DESCRIPTOR = getConstructorDescriptor(
            AbstractStructImpl::class.java.declaredConstructors.single()
        )!!
    }
}

private abstract class AbstractStructTypeImpl<S : Struct>(
    final override val size: Long,
    final override val alignment: Long,
) : Struct.Type<S> {
    final override fun invoke(memory: Memory): S {
        require(!memory.isNative || memory.startAddress.toBits().isAlignedTo(alignment))
        return createStruct(memory.slice(size = size))
    }

    protected abstract fun createStruct(memory: Memory): S

    companion object {
        val INTERNAL_NAME = getInternalName(AbstractStructTypeImpl::class.java)!!

        val CONSTRUCTOR_DESCRIPTOR = getConstructorDescriptor(
            AbstractStructTypeImpl::class.java.declaredConstructors.single()
        )!!

        val CREATE_STRUCT_NAME = AbstractStructTypeImpl<*>::createStruct.javaMethod!!.name!!

        val CREATE_STRUCT_DESCRIPTOR = getMethodDescriptor(AbstractStructTypeImpl<*>::createStruct.javaMethod!!)!!
    }
}

private fun structFactoryBootstrap(lookup: Lookup, name: String, type: MethodType): CallSite {
    val constructorHandle = classData(lookup, "_", MethodHandle::class.java)
    return ConstantCallSite(constructorHandle)
}

private val STRUCT_FACTORY_BOOTSTRAP_HANDLE = ::structFactoryBootstrap.javaMethod!!.let { javaMethod ->
    Handle(
        H_INVOKESTATIC,
        getInternalName(javaMethod.declaringClass),
        javaMethod.name,
        getMethodDescriptor(javaMethod),
        false,
    )
}

private val LOOKUP = MethodHandles.lookup()

private val MEMORY_INTERNAL_NAME = getInternalName(Memory::class.java)

private val MEMORY_DESCRIPTOR = getDescriptor(Memory::class.java)
