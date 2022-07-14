package org.schism.ffi

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.ClassWriter.COMPUTE_FRAMES
import org.objectweb.asm.Handle
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Opcodes.ACC_FINAL
import org.objectweb.asm.Opcodes.ACC_PUBLIC
import org.objectweb.asm.Opcodes.H_INVOKESTATIC
import org.objectweb.asm.Opcodes.I2L
import org.objectweb.asm.Opcodes.LAND
import org.objectweb.asm.Opcodes.LRETURN
import org.objectweb.asm.Opcodes.RETURN
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
import java.nio.file.Path
import kotlin.reflect.KCallable
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.typeOf

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
public annotation class NativeLibrary(val value: String)

@Target(AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
public annotation class NativeFunction(val name: String)

public fun <T> linkNativeLibrary(clazz: Class<T>): T {
    return (@Suppress("UNCHECKED_CAST") (LinkedNativeLibraries[clazz] as T))
}

public inline fun <reified T> linkNativeLibrary(): T {
    return linkNativeLibrary(T::class.java)
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

    val libraryName = requireNotNull(klass.findAnnotation<NativeLibrary>()?.value) {
        "$klass is missing a ${NativeLibrary::class.simpleName} annotation"
    }

    val symbolLookup = when (libraryName) {
        "" -> NATIVE_LINKER.defaultLookup()
        else -> libraryLookup(Path.of(libraryName), MemorySession.openImplicit())
    }

    val downcallHandles = mutableListOf<MethodHandle>()

    val implWriter = ClassWriter(COMPUTE_FRAMES)

    implWriter.visit(
        V19,
        ACC_FINAL,
        "org/schism/ffi/NativeLibraryImpl",
        null,
        "java/lang/Object",
        arrayOf(getInternalName(clazz)),
    )

    implWriter.visitMethod(Opcodes.ACC_PRIVATE, "<init>", "()V", null, null).run {
        visitCode()
        visitVarInsn(Opcodes.ALOAD, 0)
        visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false)
        visitInsn(RETURN)
        visitMaxs(0, 0)
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

        val symbolName = requireNotNull(member.findAnnotation<NativeFunction>()?.name) {
            "Abstract member $member is missing a ${NativeFunction::class.simpleName} annotation"
        }

        val javaMethod = requireNotNull(member.javaMethod) {
            "Abstract member $member does not correspond to a Java method"
        }

        val symbol = checkNotNull(symbolLookup.lookup(symbolName).orElse(null)) {
            "Could not find $symbolName in library $libraryName"
        }

        val methodWriter = implWriter.visitMethod(
            ACC_PUBLIC,
            javaMethod.name,
            getMethodDescriptor(javaMethod),
            null,
            null,
        )

        methodWriter.visitCode()

        val argumentLayouts = mutableListOf<MemoryLayout>()
        var nextArgumentLocal = 1

        for ((kParam, jParam) in member.parameters.drop(1).zip(javaMethod.parameterTypes)) when (kParam.type) {
            typeOf<Byte>(), typeOf<UByte>() -> {
                require(jParam == Byte::class.java)

                argumentLayouts.add(ValueLayout.JAVA_BYTE)
                methodWriter.visitVarInsn(Opcodes.ILOAD, nextArgumentLocal)
                nextArgumentLocal += 1
            }

            typeOf<Short>(), typeOf<UShort>() -> {
                require(jParam == Short::class.java)

                argumentLayouts.add(ValueLayout.JAVA_SHORT)
                methodWriter.visitVarInsn(Opcodes.ILOAD, nextArgumentLocal)
                nextArgumentLocal += 1
            }

            typeOf<Int>(), typeOf<UInt>() -> {
                require(jParam == Int::class.java)

                argumentLayouts.add(JAVA_INT)
                methodWriter.visitVarInsn(Opcodes.ILOAD, nextArgumentLocal)
                nextArgumentLocal += 1
            }

            typeOf<Long>(), typeOf<ULong>() -> {
                require(jParam == Long::class.java)

                argumentLayouts.add(JAVA_LONG)
                methodWriter.visitVarInsn(Opcodes.LLOAD, nextArgumentLocal)
                nextArgumentLocal += 2
            }

            typeOf<Float>() -> {
                require(jParam == Float::class.java)

                argumentLayouts.add(ValueLayout.JAVA_FLOAT)
                methodWriter.visitVarInsn(Opcodes.FLOAD, nextArgumentLocal)
                nextArgumentLocal += 1
            }

            typeOf<Double>() -> {
                require(jParam == Double::class.java)

                argumentLayouts.add(ValueLayout.JAVA_DOUBLE)
                methodWriter.visitVarInsn(Opcodes.DLOAD, nextArgumentLocal)
                nextArgumentLocal += 2
            }

            typeOf<CLong>(), typeOf<CUnsignedLong>() -> {
                require(jParam == Long::class.java)

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
                require(jParam == Long::class.java)

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
                throw UnsupportedOperationException("Unsupported parameter type '${kParam.type}'")
            }
        }

        val visitReturn: MethodVisitor.() -> Unit
        val returnLayout: MemoryLayout?

        when (member.returnType) {
            typeOf<Byte>(), typeOf<UByte>() -> {
                require(javaMethod.returnType == Byte::class.java)
                visitReturn = { visitInsn(Opcodes.IRETURN) }
                returnLayout = ValueLayout.JAVA_BYTE
            }

            typeOf<Short>(), typeOf<UShort>() -> {
                require(javaMethod.returnType == Short::class.java)
                visitReturn = { visitInsn(Opcodes.IRETURN) }
                returnLayout = ValueLayout.JAVA_SHORT
            }

            typeOf<Int>(), typeOf<UInt>() -> {
                require(javaMethod.returnType == Int::class.java)
                visitReturn = { visitInsn(Opcodes.IRETURN) }
                returnLayout = JAVA_INT
            }

            typeOf<Long>(), typeOf<ULong>() -> {
                require(javaMethod.returnType == Long::class.java)
                visitReturn = { visitInsn(LRETURN) }
                returnLayout = JAVA_LONG
            }

            typeOf<Float>() -> {
                require(javaMethod.returnType == Float::class.java)
                visitReturn = { visitInsn(Opcodes.FRETURN) }
                returnLayout = ValueLayout.JAVA_FLOAT
            }

            typeOf<Double>() -> {
                require(javaMethod.returnType == Double::class.java)
                visitReturn = { visitInsn(Opcodes.DRETURN) }
                returnLayout = ValueLayout.JAVA_DOUBLE
            }

            typeOf<CLong>() -> {
                require(javaMethod.returnType == Long::class.java)

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
                require(javaMethod.returnType == Long::class.java)

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
                require(javaMethod.returnType == Long::class.java)

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
                require(javaMethod.returnType == Long::class.java)

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
                require(javaMethod.returnType == Void.TYPE)
                visitReturn = { visitInsn(RETURN) }
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
        methodWriter.visitMaxs(0, 0)
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
