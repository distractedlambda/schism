package org.schism.ffi

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.ClassWriter.COMPUTE_FRAMES
import org.objectweb.asm.Handle
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Opcodes.ACC_FINAL
import org.objectweb.asm.Opcodes.H_INVOKESTATIC
import org.objectweb.asm.Opcodes.I2L
import org.objectweb.asm.Opcodes.LAND
import org.objectweb.asm.Opcodes.LRETURN
import org.objectweb.asm.Opcodes.V19
import org.objectweb.asm.Type.getInternalName
import org.objectweb.asm.Type.getMethodDescriptor
import org.schism.memory.NativeAddress
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker.nativeLinker
import java.lang.foreign.MemoryLayout
import java.lang.foreign.MemorySession
import java.lang.foreign.SymbolLookup.libraryLookup
import java.lang.foreign.ValueLayout
import java.lang.foreign.ValueLayout.JAVA_INT
import java.lang.foreign.ValueLayout.JAVA_LONG
import java.lang.invoke.CallSite
import java.lang.invoke.ConstantCallSite
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodHandles.Lookup
import java.lang.invoke.MethodHandles.Lookup.ClassOption.NESTMATE
import java.lang.invoke.MethodHandles.classDataAt
import java.lang.invoke.MethodType
import java.lang.invoke.MethodType.methodType
import kotlin.reflect.KCallable
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.typeOf

public interface NativeLibrary {
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    public annotation class Name(val value: String)

    @Target(AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.RUNTIME)
    public annotation class Function(val name: String)

    public companion object {
        public fun <T : NativeLibrary> link(clazz: Class<T>): T {
            return (@Suppress("UNCHECKED_CAST") (LinkedNativeLibraries[clazz] as T))
        }

        public inline fun <reified T : NativeLibrary> link(): T {
            return link(T::class.java)
        }
    }
}

private object LinkedNativeLibraries : ClassValue<NativeLibrary>() {
    override fun computeValue(type: Class<*>): NativeLibrary {
        return generateLinkedLibrary(type)
    }
}

private fun generateLinkedLibrary(clazz: Class<*>): NativeLibrary {
    val klass = clazz.kotlin

    require(clazz.isInterface) {
        "$klass is not an interface"
    }

    require(klass.isSubclassOf(NativeLibrary::class)) {
        "$klass does not extend ${NativeLibrary::class.simpleName}"
    }

    val libraryName = requireNotNull(klass.findAnnotation<NativeLibrary.Name>()?.value) {
        "$klass is missing a ${NativeLibrary.Name::class.simpleName} annotation"
    }

    val symbolLookup = libraryLookup(libraryName, MemorySession.openImplicit())
    val downcallHandles = mutableListOf<MethodHandle>()

    val implWriter = ClassWriter(COMPUTE_FRAMES)

    implWriter.visit(
        V19,
        ACC_FINAL,
        "org/schism/ffi/LinkedNativeLibrary",
        null,
        "java/lang/Object",
        arrayOf(getInternalName(clazz)),
    )

    implWriter.visitMethod(Opcodes.ACC_PRIVATE, "<init>", "()V", null, null).run {
        visitCode()
        visitVarInsn(Opcodes.ALOAD, 0)
        visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false)
        visitEnd()
    }

    fun linkMember(member: KCallable<*>) {
        if (!member.isAbstract) {
            return
        }

        if (member is KProperty) {
            linkMember(member.getter)

            if (member is KMutableProperty) {
                linkMember(member.setter)
            }

            return
        }

        if (member !is KFunction) {
            throw UnsupportedOperationException("Encountered abstract member of unsupported type: $member")
        }

        val symbolName = requireNotNull(member.findAnnotation<NativeLibrary.Function>()?.name) {
            "Abstract member $member is missing a ${NativeLibrary.Function::class.simpleName} annotation"
        }

        val javaMethod = requireNotNull(member.javaMethod) {
            "Abstract member $member does not correspond to a Java method"
        }

        val symbol = checkNotNull(symbolLookup.lookup(symbolName).orElse(null)) {
            "Could not find $symbolName in library $libraryName"
        }

        val methodWriter = implWriter.visitMethod(0, javaMethod.name, getMethodDescriptor(javaMethod), null, null)
        methodWriter.visitCode()

        val argumentLayouts = mutableListOf<MemoryLayout>()
        var nextArgumentLocal = 1

        for (param in member.parameters.drop(1)) when (param.type) {
            typeOf<Byte>(), typeOf<UByte>() -> {
                argumentLayouts.add(ValueLayout.JAVA_BYTE)
                methodWriter.visitVarInsn(Opcodes.ILOAD, nextArgumentLocal)
                nextArgumentLocal += 1
            }

            typeOf<Short>(), typeOf<UShort>() -> {
                argumentLayouts.add(ValueLayout.JAVA_SHORT)
                methodWriter.visitVarInsn(Opcodes.ILOAD, nextArgumentLocal)
                nextArgumentLocal += 1
            }

            typeOf<Int>(), typeOf<UInt>() -> {
                argumentLayouts.add(JAVA_INT)
                methodWriter.visitVarInsn(Opcodes.ILOAD, nextArgumentLocal)
                nextArgumentLocal += 1
            }

            typeOf<Long>(), typeOf<ULong>() -> {
                argumentLayouts.add(JAVA_LONG)
                methodWriter.visitVarInsn(Opcodes.LLOAD, nextArgumentLocal)
                nextArgumentLocal += 2
            }

            typeOf<Float>() -> {
                argumentLayouts.add(ValueLayout.JAVA_FLOAT)
                methodWriter.visitVarInsn(Opcodes.FLOAD, nextArgumentLocal)
                nextArgumentLocal += 1
            }

            typeOf<Double>() -> {
                argumentLayouts.add(ValueLayout.JAVA_DOUBLE)
                methodWriter.visitVarInsn(Opcodes.DLOAD, nextArgumentLocal)
                nextArgumentLocal += 2
            }

            typeOf<CLong>(), typeOf<CUnsignedLong>() -> {
                methodWriter.visitVarInsn(Opcodes.LLOAD, nextArgumentLocal)
                nextArgumentLocal += 2

                if (C_LONG_IS_4_BYTES) {
                    methodWriter.visitInsn(Opcodes.L2I)
                    argumentLayouts.add(JAVA_INT)
                } else {
                    argumentLayouts.add(JAVA_LONG)
                }
            }

            typeOf<CPtrDiffT>(), typeOf<CSizeT>(), typeOf<NativeAddress>() -> {
                methodWriter.visitVarInsn(Opcodes.LLOAD, nextArgumentLocal)
                nextArgumentLocal += 2

                if (ADDRESS_IS_4_BYTES) {
                    methodWriter.visitInsn(Opcodes.L2I)
                    argumentLayouts.add(JAVA_INT)
                } else {
                    argumentLayouts.add(JAVA_LONG)
                }
            }

            else -> {
                throw UnsupportedOperationException("Unsupported parameter type '${param.type}'")
            }
        }

        val visitReturn: MethodVisitor.() -> Unit
        val returnLayout: MemoryLayout?

        when (member.returnType) {
            typeOf<Byte>(), typeOf<UByte>() -> {
                visitReturn = { visitInsn(Opcodes.IRETURN) }
                returnLayout = ValueLayout.JAVA_BYTE
            }

            typeOf<Short>(), typeOf<UShort>() -> {
                visitReturn = { visitInsn(Opcodes.IRETURN) }
                returnLayout = ValueLayout.JAVA_SHORT
            }

            typeOf<Int>(), typeOf<UInt>() -> {
                visitReturn = { visitInsn(Opcodes.IRETURN) }
                returnLayout = JAVA_INT
            }

            typeOf<Long>(), typeOf<ULong>() -> {
                visitReturn = { visitInsn(LRETURN) }
                returnLayout = JAVA_LONG
            }

            typeOf<Float>() -> {
                visitReturn = { visitInsn(Opcodes.FRETURN) }
                returnLayout = ValueLayout.JAVA_FLOAT
            }

            typeOf<Double>() -> {
                visitReturn = { visitInsn(Opcodes.DRETURN) }
                returnLayout = ValueLayout.JAVA_DOUBLE
            }

            typeOf<CLong>() -> {
                if (C_LONG_IS_4_BYTES) {
                    visitReturn = {
                        visitInsn(I2L)
                        visitInsn(LRETURN)
                    }

                    returnLayout = JAVA_INT
                } else {
                    visitReturn = { visitInsn(LRETURN) }
                    returnLayout = JAVA_LONG
                }
            }

            typeOf<CUnsignedLong>() -> {
                if (C_LONG_IS_4_BYTES) {
                    visitReturn = {
                        visitInsn(I2L)
                        visitLdcInsn(0xFFFF_FFFFL)
                        visitInsn(LAND)
                        visitInsn(LRETURN)
                    }

                    returnLayout = JAVA_INT
                } else {
                    visitReturn = { visitInsn(LRETURN) }
                    returnLayout = JAVA_LONG
                }
            }

            typeOf<CPtrDiffT>() -> {
                if (ADDRESS_IS_4_BYTES) {
                    visitReturn = {
                        visitInsn(I2L)
                        visitInsn(LRETURN)
                    }

                    returnLayout = JAVA_INT
                } else {
                    visitReturn = { visitInsn(LRETURN) }
                    returnLayout = JAVA_LONG
                }
            }

            typeOf<CSizeT>(), typeOf<NativeAddress>() -> {
                if (ADDRESS_IS_4_BYTES) {
                    visitReturn = {
                        visitInsn(I2L)
                        visitLdcInsn(0xFFFF_FFFFL)
                        visitInsn(LAND)
                        visitInsn(LRETURN)
                    }

                    returnLayout = JAVA_INT
                } else {
                    visitReturn = { visitInsn(LRETURN) }
                    returnLayout = JAVA_LONG
                }
            }

            typeOf<Unit>() -> {
                visitReturn = {}
                returnLayout = null
            }

            else -> {
                throw UnsupportedOperationException("Unsupported return type '${member.returnType}'")
            }
        }

        val argumentLayoutsArray = argumentLayouts.toTypedArray()

        val functionDescriptor = if (returnLayout != null) {
            FunctionDescriptor.of(returnLayout, *argumentLayoutsArray)
        } else {
            FunctionDescriptor.ofVoid(*argumentLayoutsArray)
        }

        val downcallHandle = NATIVE_LINKER.downcallHandle(symbol, functionDescriptor)
        downcallHandles.add(downcallHandle)

        methodWriter.visitInvokeDynamicInsn(
            "_",
            downcallHandle.type().descriptorString(),
            DOWNCALL_BOOTSTRAP_HANDLE,
            downcallHandles.lastIndex,
        )

        methodWriter.visitReturn()
        methodWriter.visitEnd()
    }

    klass.members.forEach(::linkMember)

    implWriter.visitEnd()

    val implLookup = LOOKUP.defineHiddenClassWithClassData(
        implWriter.toByteArray(),
        downcallHandles.toList(),
        false,
        NESTMATE,
    )

    val implConstructor = implLookup.findConstructor(implLookup.lookupClass(), methodType(Void.TYPE))

    return implConstructor.invoke() as NativeLibrary
}

private fun downcallBootstrap(lookup: Lookup, name: String, type: MethodType, index: Int): CallSite {
    val downcallHandle = classDataAt(lookup, "_", MethodHandle::class.java, index)
    return ConstantCallSite(downcallHandle)
}

private val DOWNCALL_BOOTSTRAP_HANDLE = ::downcallBootstrap.javaMethod!!.let { javaMethod ->
    Handle(
        H_INVOKESTATIC,
        getInternalName(javaMethod.declaringClass),
        javaMethod.name,
        getMethodDescriptor(javaMethod),
        false,
    )
}

private val LOOKUP = MethodHandles.lookup()

private val NATIVE_LINKER = nativeLinker()
