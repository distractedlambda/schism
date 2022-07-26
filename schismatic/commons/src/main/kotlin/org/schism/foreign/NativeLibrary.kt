package org.schism.foreign

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.ClassWriter.COMPUTE_FRAMES
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
import org.objectweb.asm.Opcodes.H_INVOKESTATIC
import org.objectweb.asm.Opcodes.I2L
import org.objectweb.asm.Opcodes.ILOAD
import org.objectweb.asm.Opcodes.INVOKESPECIAL
import org.objectweb.asm.Opcodes.IRETURN
import org.objectweb.asm.Opcodes.L2I
import org.objectweb.asm.Opcodes.LAND
import org.objectweb.asm.Opcodes.LLOAD
import org.objectweb.asm.Opcodes.LRETURN
import org.objectweb.asm.Opcodes.RETURN
import org.objectweb.asm.Opcodes.V19
import org.schism.invoke.HiddenClassDefiner
import org.schism.reflect.descriptorString
import org.schism.reflect.internalName
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker.downcallType
import java.lang.foreign.Linker.nativeLinker
import java.lang.foreign.MemoryLayout
import java.lang.foreign.SymbolLookup
import java.lang.invoke.CallSite
import java.lang.invoke.ConstantCallSite
import java.lang.invoke.MethodHandles.Lookup
import java.lang.invoke.MethodHandles.classData
import java.lang.invoke.MethodType
import java.lang.invoke.MethodType.methodType
import kotlin.jvm.optionals.getOrElse
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.javaMethod

@Target(AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
public annotation class NativeFunction(val name: String)

public inline fun <reified T> linkNativeLibrary(symbolLookup: SymbolLookup): T {
    return linkNativeLibrary(T::class, HiddenClassDefiner(), symbolLookup) as T
}

@PublishedApi
internal fun linkNativeLibrary(klass: KClass<*>, definer: HiddenClassDefiner, symbolLookup: SymbolLookup): Any {
    require(klass.java.isInterface) {
        "$klass is not an interface"
    }

    val downcallSpecs = mutableListOf<DowncallSpec>()

    val implName = "${definer.internalNamePrefix}${klass.simpleName}Impl"

    val implWriter = ClassWriter(COMPUTE_FRAMES)

    implWriter.visit(V19, ACC_FINAL, implName, null, "java/lang/Object", arrayOf(klass.java.internalName))

    implWriter.visitMethod(ACC_PRIVATE, "<init>", "()V", null, null).run {
        visitCode()
        visitVarInsn(ALOAD, 0)
        visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false)
        visitInsn(RETURN)
        visitMaxs(0, 0)
        visitEnd()
    }

    fun linkMember(member: KCallable<*>) {
        if (member is KProperty) {
            linkMember(member.getter)

            if (member is KMutableProperty) {
                linkMember(member.setter)
            }

            return
        }

        val symbolName = member.findAnnotation<NativeFunction>()?.name ?: return

        val javaMethod = requireNotNull((member as? KFunction)?.javaMethod) {
            "$member does not correspond to a Java method"
        }

        val methodWriter = implWriter.visitMethod(
            ACC_PUBLIC,
            javaMethod.name,
            javaMethod.descriptorString,
            null,
            null,
        )

        methodWriter.visitCode()

        val argumentLayouts = mutableListOf<MemoryLayout>()
        var nextArgumentLocal = 1

        for ((kParam, jType) in member.parameters.drop(1).zip(javaMethod.parameterTypes)) {
            val handling = ffiHandlingFor(kParam.type)

            argumentLayouts.add(handling.memoryLayout)

            require(handling.jvmType == jType)

            when (handling) {
                FfiHandling.I8AsByte, FfiHandling.I16AsShort, FfiHandling.I32AsInt -> {
                    methodWriter.visitVarInsn(ILOAD, nextArgumentLocal)
                    nextArgumentLocal += 1
                }

                FfiHandling.I64AsLong -> {
                    methodWriter.visitVarInsn(LLOAD, nextArgumentLocal)
                    nextArgumentLocal += 2
                }

                FfiHandling.F32AsFloat -> {
                    methodWriter.visitVarInsn(FLOAD, nextArgumentLocal)
                    nextArgumentLocal += 1
                }

                FfiHandling.F64AsDouble -> {
                    methodWriter.visitVarInsn(DLOAD, nextArgumentLocal)
                    nextArgumentLocal += 2
                }

                FfiHandling.I32AsSignedLong, FfiHandling.I32AsUnsignedLong -> {
                    methodWriter.visitVarInsn(LLOAD, nextArgumentLocal)
                    methodWriter.visitInsn(L2I) // TODO: add range check?
                    nextArgumentLocal += 2
                }

                FfiHandling.Address -> {
                    methodWriter.visitVarInsn(ALOAD, nextArgumentLocal)
                    nextArgumentLocal += 1
                }
            }
        }

        val visitReturn: MethodVisitor.() -> Unit
        val returnLayout: MemoryLayout?

        if (member.returnType.classifier == Unit::class) {
            require(javaMethod.returnType == Void.TYPE)
            visitReturn = { visitInsn(RETURN) }
            returnLayout = null
        } else {
            val returnHandling = ffiHandlingFor(member.returnType)

            require(returnHandling.jvmType == javaMethod.returnType)

            visitReturn = when (returnHandling) {
                FfiHandling.I8AsByte, FfiHandling.I16AsShort, FfiHandling.I32AsInt -> {
                    { visitInsn(IRETURN) }
                }

                FfiHandling.I64AsLong -> {
                    { visitInsn(LRETURN) }
                }

                FfiHandling.F32AsFloat -> {
                    { visitInsn(FRETURN) }
                }

                FfiHandling.F64AsDouble -> {
                    { visitInsn(DRETURN) }
                }

                FfiHandling.I32AsSignedLong -> {
                    {
                        visitInsn(I2L)
                        visitInsn(LRETURN)
                    }
                }

                FfiHandling.I32AsUnsignedLong -> {
                    {
                        visitInsn(I2L)
                        visitLdcInsn(0xFFFF_FFFFL)
                        visitInsn(LAND)
                        visitInsn(LRETURN)
                    }
                }

                FfiHandling.Address -> {
                    { visitInsn(ARETURN) }
                }
            }

            returnLayout = returnHandling.memoryLayout
        }

        val functionDescriptor = if (returnLayout != null) {
            FunctionDescriptor.of(returnLayout, *argumentLayouts.toTypedArray())
        } else {
            FunctionDescriptor.ofVoid(*argumentLayouts.toTypedArray())
        }

        downcallSpecs.add(DowncallSpec(symbolName, functionDescriptor))

        methodWriter.visitInvokeDynamicInsn(
            "_",
            downcallType(functionDescriptor).descriptorString(),
            DOWNCALL_BOOTSTRAP_HANDLE,
            downcallSpecs.lastIndex,
        )

        methodWriter.visitReturn()
        methodWriter.visitMaxs(0, 0)
        methodWriter.visitEnd()
    }

    klass.members.forEach(::linkMember)

    implWriter.visitEnd()

    val implLookup = definer.define(implWriter.toByteArray(), ClassData(symbolLookup, downcallSpecs.toList()))
    return implLookup.findConstructor(implLookup.lookupClass(), methodType(Void.TYPE)).invoke()
}

private data class DowncallSpec(val symbolName: String, val descriptor: FunctionDescriptor)

private data class ClassData(val symbolLookup: SymbolLookup, val downcallSpecs: List<DowncallSpec>)

@OptIn(ExperimentalStdlibApi::class)
internal fun downcallBootstrap(lookup: Lookup, name: String, type: MethodType, index: Int): CallSite {
    val (symbolLookup, downcallSpecs) = classData(lookup, "_", ClassData::class.java)
    val (symbolName, descriptor) = downcallSpecs[index]

    val symbol = symbolLookup.lookup(symbolName).getOrElse {
        throw NoSuchElementException("Couldn't find symbol '$symbolName' in $symbolLookup")
    }

    return ConstantCallSite(NATIVE_LINKER.downcallHandle(symbol, descriptor))
}

private val DOWNCALL_BOOTSTRAP_HANDLE = ::downcallBootstrap.javaMethod!!.let { javaMethod ->
    Handle(
        H_INVOKESTATIC,
        javaMethod.declaringClass.internalName,
        javaMethod.name,
        javaMethod.descriptorString,
        false,
    )
}

private val NATIVE_LINKER = nativeLinker()
