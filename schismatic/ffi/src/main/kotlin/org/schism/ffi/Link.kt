package org.schism.ffi

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Opcodes.ACC_PRIVATE
import org.objectweb.asm.Opcodes.ALOAD
import org.objectweb.asm.Opcodes.DLOAD
import org.objectweb.asm.Opcodes.DRETURN
import org.objectweb.asm.Opcodes.FLOAD
import org.objectweb.asm.Opcodes.FRETURN
import org.objectweb.asm.Opcodes.I2L
import org.objectweb.asm.Opcodes.ILOAD
import org.objectweb.asm.Opcodes.INVOKESPECIAL
import org.objectweb.asm.Opcodes.IRETURN
import org.objectweb.asm.Opcodes.L2I
import org.objectweb.asm.Opcodes.LAND
import org.objectweb.asm.Opcodes.LLOAD
import org.objectweb.asm.Opcodes.LRETURN
import org.objectweb.asm.Type.getInternalName
import org.objectweb.asm.Type.getMethodDescriptor
import org.schism.memory.NativeAddress
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker
import java.lang.foreign.MemoryLayout
import java.lang.foreign.MemorySession
import java.lang.foreign.SymbolLookup
import java.lang.foreign.ValueLayout.JAVA_BYTE
import java.lang.foreign.ValueLayout.JAVA_DOUBLE
import java.lang.foreign.ValueLayout.JAVA_FLOAT
import java.lang.foreign.ValueLayout.JAVA_INT
import java.lang.foreign.ValueLayout.JAVA_LONG
import java.lang.foreign.ValueLayout.JAVA_SHORT
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodHandles.Lookup
import java.lang.invoke.MethodType.methodType
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.typeOf

public inline fun <reified L : CLibrary> link(library: String): L {
    return link(MethodHandles.lookup(), L::class, library)
}

public fun <L : CLibrary> link(lookup: Lookup, klass: KClass<L>, library: String): L {
    require(klass.java.isInterface) {
        "Only interface types can be linked"
    }

    require(klass.isSubclassOf(CLibrary::class)) {
        "Only types inheriting from ${CLibrary::class.simpleName} can be linked"
    }

    val nativeLinker = Linker.nativeLinker()
    val symbolLookup = SymbolLookup.libraryLookup(library, MemorySession.openImplicit())

    val writer = ClassWriter(ClassWriter.COMPUTE_FRAMES)
    val downcallHandles = mutableListOf<MethodHandle>()

    writer.visit(
        Opcodes.V19,
        Opcodes.ACC_FINAL,
        LINK_IMPL_NAME,
        null,
        "java/lang/Object",
        arrayOf(getInternalName(klass.java)),
    )

    val constructor = writer.visitMethod(ACC_PRIVATE, "<init>", "()V", null, null)
    constructor.visitCode()
    constructor.visitVarInsn(ALOAD, 0)
    constructor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false)
    constructor.visitEnd()

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
            throw UnsupportedOperationException("Encountered abstract member of unrecognized type: $member")
        }

        val javaMethod = member.javaMethod ?: throw UnsupportedOperationException(
            "Abstract member $member does not correspond to a Java method"
        )

        val symbolName = member.findAnnotation<CFunction>()?.value ?: throw UnsupportedOperationException(
            "Abstract member $member is missing a ${CFunction::class.simpleName} annotation"
        )

        val symbol = symbolLookup.lookup(symbolName).orElse(null) ?: throw UnsatisfiedLinkError(
            "Could not find $symbolName in library $library"
        )

        val methodWriter = writer.visitMethod(0, javaMethod.name, getMethodDescriptor(javaMethod), null, null)
        methodWriter.visitCode()

        val argumentLayouts = mutableListOf<MemoryLayout>()
        var nextArgumentLocal = 1

        for (param in member.parameters.drop(1)) when (param.type) {
            typeOf<Byte>(), typeOf<UByte>() -> {
                argumentLayouts.add(JAVA_BYTE)
                methodWriter.visitVarInsn(ILOAD, nextArgumentLocal)
                nextArgumentLocal += 1
            }

            typeOf<Short>(), typeOf<UShort>() -> {
                argumentLayouts.add(JAVA_SHORT)
                methodWriter.visitVarInsn(ILOAD, nextArgumentLocal)
                nextArgumentLocal += 1
            }

            typeOf<Int>(), typeOf<UInt>() -> {
                argumentLayouts.add(JAVA_INT)
                methodWriter.visitVarInsn(ILOAD, nextArgumentLocal)
                nextArgumentLocal += 1
            }

            typeOf<Long>(), typeOf<ULong>() -> {
                argumentLayouts.add(JAVA_LONG)
                methodWriter.visitVarInsn(LLOAD, nextArgumentLocal)
                nextArgumentLocal += 2
            }

            typeOf<Float>() -> {
                argumentLayouts.add(JAVA_FLOAT)
                methodWriter.visitVarInsn(FLOAD, nextArgumentLocal)
                nextArgumentLocal += 1
            }

            typeOf<Double>() -> {
                argumentLayouts.add(JAVA_DOUBLE)
                methodWriter.visitVarInsn(DLOAD, nextArgumentLocal)
                nextArgumentLocal += 2
            }

            typeOf<CLong>(), typeOf<CUnsignedLong>() -> {
                methodWriter.visitVarInsn(LLOAD, nextArgumentLocal)
                nextArgumentLocal += 2

                if (C_LONG_IS_4_BYTES) {
                    methodWriter.visitInsn(L2I)
                    argumentLayouts.add(JAVA_INT)
                } else {
                    argumentLayouts.add(JAVA_LONG)
                }
            }

            typeOf<CPtrDiffT>(), typeOf<CSizeT>(), typeOf<NativeAddress>() -> {
                methodWriter.visitVarInsn(LLOAD, nextArgumentLocal)
                nextArgumentLocal += 2

                if (ADDRESS_IS_4_BYTES) {
                    methodWriter.visitInsn(L2I)
                    argumentLayouts.add(JAVA_INT)
                } else {
                    argumentLayouts.add(JAVA_LONG)
                }
            }

            else -> {
                throw UnsupportedOperationException("Unsupported parameter type: ${param.type}")
            }
        }

        val visitReturn: MethodVisitor.() -> Unit
        val returnLayout: MemoryLayout?

        when (member.returnType) {
            typeOf<Byte>(), typeOf<UByte>() -> {
                visitReturn = { visitInsn(IRETURN) }
                returnLayout = JAVA_BYTE
            }

            typeOf<Short>(), typeOf<UShort>() -> {
                visitReturn = { visitInsn(IRETURN) }
                returnLayout = JAVA_SHORT
            }

            typeOf<Int>(), typeOf<UInt>() -> {
                visitReturn = { visitInsn(IRETURN) }
                returnLayout = JAVA_INT
            }

            typeOf<Long>(), typeOf<ULong>() -> {
                visitReturn = { visitInsn(LRETURN) }
                returnLayout = JAVA_LONG
            }

            typeOf<Float>() -> {
                visitReturn = { visitInsn(FRETURN) }
                returnLayout = JAVA_FLOAT
            }

            typeOf<Double>() -> {
                visitReturn = { visitInsn(DRETURN) }
                returnLayout = JAVA_DOUBLE
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
                throw UnsupportedOperationException("Unsupported return type: ${member.returnType}")
            }
        }

        val functionDescriptor = if (returnLayout != null) {
            FunctionDescriptor.of(returnLayout, *argumentLayouts.toTypedArray())
        } else {
            FunctionDescriptor.ofVoid(*argumentLayouts.toTypedArray())
        }


        methodWriter.visitReturn()
        methodWriter.visitEnd()
    }

    klass.members.forEach(::linkMember)

    writer.visitEnd()

    val implLookup = lookup.defineHiddenClassWithClassData(writer.toByteArray(), downcallHandles.toList(), false)
    val implConstructor = implLookup.findConstructor(implLookup.lookupClass(), methodType(implLookup.lookupClass()))
    return (@Suppress("UNCHECKED_CAST") (implConstructor.invoke() as L))
}

private const val LINK_IMPL_NAME = "LinkImpl"
