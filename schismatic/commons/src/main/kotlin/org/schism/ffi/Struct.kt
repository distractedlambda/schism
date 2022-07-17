package org.schism.ffi

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.ClassWriter.COMPUTE_FRAMES
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
import org.objectweb.asm.Type.getDescriptor
import org.objectweb.asm.Type.getInternalName
import org.schism.invoke.HiddenClassDefiner
import org.schism.math.alignForwardsTo
import org.schism.memory.Memory
import org.schism.memory.NativeAddress
import org.schism.memory.allocateNativeMemory
import org.schism.memory.nativeMemory
import org.schism.memory.requireAlignedTo
import org.schism.memory.withNativeMemory
import org.schism.reflect.descriptorString
import org.schism.reflect.internalName
import java.lang.invoke.CallSite
import java.lang.invoke.ConstantCallSite
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles.Lookup
import java.lang.invoke.MethodHandles.classData
import java.lang.invoke.MethodType
import java.lang.invoke.MethodType.methodType
import java.util.TreeMap
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.javaGetter
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.javaSetter

public interface Struct {
    public fun memory(): Memory
}

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
public annotation class StructField(val index: Int)

public interface StructType<out S : Struct> {
    public val size: Long

    public val alignment: Long

    public fun wrap(memory: Memory): S
}

public fun Struct.isNative(): Boolean {
    return memory().isNative
}

public fun Struct.size(): Long {
    return memory().size
}

public fun Struct.isReadable(): Boolean {
    return memory().isReadable
}

public fun Struct.isWritable(): Boolean {
    return memory().isWritable
}

public fun Struct.address(): NativeAddress {
    return memory().startAddress
}

public fun <S : Struct> StructType<S>.wrap(address: NativeAddress): S {
    return wrap(nativeMemory(address, size))
}

public fun <S : Struct> allocateNativeStruct(type: StructType<S>): S {
    return type.wrap(allocateNativeMemory(type.size))
}

@OptIn(ExperimentalContracts::class)
public inline fun <S : Struct, R> withNativeStruct(type: StructType<S>, block: (S) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return withNativeMemory(type.size) {
        block(type.wrap(it))
    }
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
            visitFieldInsn(GETFIELD, AbstractStructImpl.INTERNAL_NAME, "memory", MEMORY_DESCRIPTOR)
            visitLdcInsn(offset)

            when (handling) {
                FfiHandling.I8AsByte -> {
                    visitMethodInsn(INVOKEINTERFACE, MEMORY_INTERNAL_NAME, "getByte", "(J)B", true)
                    visitInsn(IRETURN)
                }

                FfiHandling.I16AsShort -> {
                    visitMethodInsn(INVOKEINTERFACE, MEMORY_INTERNAL_NAME, "getShort", "(J)S", true)
                    visitInsn(IRETURN)
                }

                FfiHandling.I32AsInt -> {
                    visitMethodInsn(INVOKEINTERFACE, MEMORY_INTERNAL_NAME, "getInt", "(J)I", true)
                    visitInsn(IRETURN)
                }

                FfiHandling.I64AsLong -> {
                    visitMethodInsn(INVOKEINTERFACE, MEMORY_INTERNAL_NAME, "getLong", "(J)J", true)
                    visitInsn(LRETURN)
                }

                FfiHandling.F32AsFloat -> {
                    visitMethodInsn(INVOKEINTERFACE, MEMORY_INTERNAL_NAME, "getFloat", "(J)F", true)
                    visitInsn(FRETURN)
                }

                FfiHandling.F64AsDouble -> {
                    visitMethodInsn(INVOKEINTERFACE, MEMORY_INTERNAL_NAME, "getDouble", "(J)D", true)
                    visitInsn(DRETURN)
                }

                FfiHandling.I32AsSignedLong -> {
                    visitMethodInsn(INVOKEINTERFACE, MEMORY_INTERNAL_NAME, "getInt", "(J)I", true)
                    visitInsn(I2L)
                    visitInsn(LRETURN)
                }

                FfiHandling.I32AsUnsignedLong -> {
                    visitMethodInsn(INVOKEINTERFACE, MEMORY_INTERNAL_NAME, "getInt", "(J)I", true)
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
                visitFieldInsn(GETFIELD, AbstractStructImpl.INTERNAL_NAME, "memory", MEMORY_DESCRIPTOR)

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
                        visitMethodInsn(INVOKEINTERFACE, MEMORY_INTERNAL_NAME, "setByte", "(BJ)V", true)
                    }

                    FfiHandling.I16AsShort -> {
                        visitMethodInsn(INVOKEINTERFACE, MEMORY_INTERNAL_NAME, "setShort", "(SJ)V", true)
                    }

                    FfiHandling.I32AsInt, FfiHandling.I32AsSignedLong, FfiHandling.I32AsUnsignedLong -> {
                        visitMethodInsn(INVOKEINTERFACE, MEMORY_INTERNAL_NAME, "setInt", "(IJ)V", true)
                    }

                    FfiHandling.I64AsLong -> {
                        visitMethodInsn(INVOKEINTERFACE, MEMORY_INTERNAL_NAME, "setLong", "(JJ)V", true)
                    }

                    FfiHandling.F32AsFloat -> {
                        visitMethodInsn(INVOKEINTERFACE, MEMORY_INTERNAL_NAME, "setFloat", "(FJ)V", true)
                    }

                    FfiHandling.F64AsDouble -> {
                        visitMethodInsn(INVOKEINTERFACE, MEMORY_INTERNAL_NAME, "setDouble", "(DJ)V", true)
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
        .findConstructor(implLookup.lookupClass(), methodType(Void.TYPE, Memory::class.java))
        .asType(methodType(Struct::class.java, Memory::class.java))

    val typeImplName = "${definer.internalNamePrefix}${klass.simpleName}TypeImpl"
    val typeImplWriter = ClassWriter(COMPUTE_FRAMES)

    typeImplWriter.visit(V19, ACC_FINAL, typeImplName, null, AbstractStructTypeImpl.INTERNAL_NAME, null)

    typeImplWriter.visitMethod(ACC_PRIVATE, "<init>", "()V", null, null).apply {
        visitCode()

        visitVarInsn(ALOAD, 0)
        visitLdcInsn(size)
        visitLdcInsn(alignment)
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
        visitInvokeDynamicInsn("_", AbstractStructTypeImpl.WRAP_SAFE_DESCRIPTOR, STRUCT_FACTORY_BOOTSTRAP_HANDLE)
        visitInsn(ARETURN)
        visitMaxs(0, 0)
        visitEnd()
    }

    typeImplWriter.visitEnd()

    val typeImplLookup = definer.define(typeImplWriter.toByteArray(), classData = implConstructor)

    @Suppress("UNCHECKED_CAST")
    return typeImplLookup
        .findConstructor(typeImplLookup.lookupClass(), methodType(Void.TYPE))
        .invoke() as StructType<S>
}

internal abstract class AbstractStructImpl(@JvmField protected val memory: Memory) : Struct {
    final override fun memory(): Memory {
        return memory
    }

    companion object {
        val INTERNAL_NAME = AbstractStructImpl::class.java.internalName

        val CONSTRUCTOR_DESCRIPTOR = AbstractStructImpl::class.java.declaredConstructors.single().descriptorString
    }
}

internal abstract class AbstractStructTypeImpl<S : Struct>(
    final override val size: Long,
    final override val alignment: Long,
) : StructType<S> {
    final override fun wrap(memory: Memory): S {
        memory.requireAlignedTo(alignment)
        return wrapSafe(memory.slice(size = size))
    }

    protected abstract fun wrapSafe(memory: Memory): S

    companion object {
        val INTERNAL_NAME = AbstractStructTypeImpl::class.java.internalName

        val CONSTRUCTOR_DESCRIPTOR = AbstractStructTypeImpl::class.java.declaredConstructors.single().descriptorString

        val WRAP_SAFE_NAME = AbstractStructTypeImpl<*>::wrapSafe.javaMethod!!.name

        val WRAP_SAFE_DESCRIPTOR = AbstractStructTypeImpl<*>::wrapSafe.javaMethod!!.descriptorString
    }
}

internal fun structFactoryBootstrap(lookup: Lookup, name: String, type: MethodType): CallSite {
    val constructorHandle = classData(lookup, "_", MethodHandle::class.java)
    return ConstantCallSite(constructorHandle)
}

private val STRUCT_FACTORY_BOOTSTRAP_HANDLE = ::structFactoryBootstrap.javaMethod!!.let { javaMethod ->
    Handle(
        H_INVOKESTATIC,
        javaMethod.declaringClass.internalName,
        javaMethod.name,
        javaMethod.descriptorString,
        false,
    )
}

private val MEMORY_INTERNAL_NAME = getInternalName(Memory::class.java)

private val MEMORY_DESCRIPTOR = getDescriptor(Memory::class.java)
