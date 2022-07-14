package org.schism.jit

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.ClassWriter.COMPUTE_FRAMES
import org.objectweb.asm.ConstantDynamic
import org.objectweb.asm.Handle
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.AALOAD
import org.objectweb.asm.Opcodes.AASTORE
import org.objectweb.asm.Opcodes.ACONST_NULL
import org.objectweb.asm.Opcodes.ALOAD
import org.objectweb.asm.Opcodes.ANEWARRAY
import org.objectweb.asm.Opcodes.ARETURN
import org.objectweb.asm.Opcodes.ARRAYLENGTH
import org.objectweb.asm.Opcodes.ASTORE
import org.objectweb.asm.Opcodes.ATHROW
import org.objectweb.asm.Opcodes.BALOAD
import org.objectweb.asm.Opcodes.BASTORE
import org.objectweb.asm.Opcodes.BIPUSH
import org.objectweb.asm.Opcodes.CALOAD
import org.objectweb.asm.Opcodes.CASTORE
import org.objectweb.asm.Opcodes.CHECKCAST
import org.objectweb.asm.Opcodes.D2F
import org.objectweb.asm.Opcodes.D2I
import org.objectweb.asm.Opcodes.D2L
import org.objectweb.asm.Opcodes.DADD
import org.objectweb.asm.Opcodes.DALOAD
import org.objectweb.asm.Opcodes.DASTORE
import org.objectweb.asm.Opcodes.DCMPG
import org.objectweb.asm.Opcodes.DCMPL
import org.objectweb.asm.Opcodes.DCONST_0
import org.objectweb.asm.Opcodes.DCONST_1
import org.objectweb.asm.Opcodes.DDIV
import org.objectweb.asm.Opcodes.DLOAD
import org.objectweb.asm.Opcodes.DMUL
import org.objectweb.asm.Opcodes.DNEG
import org.objectweb.asm.Opcodes.DREM
import org.objectweb.asm.Opcodes.DRETURN
import org.objectweb.asm.Opcodes.DSTORE
import org.objectweb.asm.Opcodes.DSUB
import org.objectweb.asm.Opcodes.DUP
import org.objectweb.asm.Opcodes.DUP2
import org.objectweb.asm.Opcodes.DUP2_X1
import org.objectweb.asm.Opcodes.DUP2_X2
import org.objectweb.asm.Opcodes.DUP_X1
import org.objectweb.asm.Opcodes.DUP_X2
import org.objectweb.asm.Opcodes.F2D
import org.objectweb.asm.Opcodes.F2I
import org.objectweb.asm.Opcodes.F2L
import org.objectweb.asm.Opcodes.FADD
import org.objectweb.asm.Opcodes.FALOAD
import org.objectweb.asm.Opcodes.FASTORE
import org.objectweb.asm.Opcodes.FCMPG
import org.objectweb.asm.Opcodes.FCMPL
import org.objectweb.asm.Opcodes.FCONST_0
import org.objectweb.asm.Opcodes.FCONST_1
import org.objectweb.asm.Opcodes.FCONST_2
import org.objectweb.asm.Opcodes.FDIV
import org.objectweb.asm.Opcodes.FLOAD
import org.objectweb.asm.Opcodes.FMUL
import org.objectweb.asm.Opcodes.FNEG
import org.objectweb.asm.Opcodes.FREM
import org.objectweb.asm.Opcodes.FRETURN
import org.objectweb.asm.Opcodes.FSTORE
import org.objectweb.asm.Opcodes.FSUB
import org.objectweb.asm.Opcodes.GETFIELD
import org.objectweb.asm.Opcodes.GETSTATIC
import org.objectweb.asm.Opcodes.GOTO
import org.objectweb.asm.Opcodes.H_INVOKESTATIC
import org.objectweb.asm.Opcodes.I2B
import org.objectweb.asm.Opcodes.I2C
import org.objectweb.asm.Opcodes.I2D
import org.objectweb.asm.Opcodes.I2F
import org.objectweb.asm.Opcodes.I2L
import org.objectweb.asm.Opcodes.I2S
import org.objectweb.asm.Opcodes.IADD
import org.objectweb.asm.Opcodes.IALOAD
import org.objectweb.asm.Opcodes.IAND
import org.objectweb.asm.Opcodes.IASTORE
import org.objectweb.asm.Opcodes.ICONST_0
import org.objectweb.asm.Opcodes.ICONST_1
import org.objectweb.asm.Opcodes.ICONST_2
import org.objectweb.asm.Opcodes.ICONST_3
import org.objectweb.asm.Opcodes.ICONST_4
import org.objectweb.asm.Opcodes.ICONST_5
import org.objectweb.asm.Opcodes.ICONST_M1
import org.objectweb.asm.Opcodes.IDIV
import org.objectweb.asm.Opcodes.IFEQ
import org.objectweb.asm.Opcodes.IFGE
import org.objectweb.asm.Opcodes.IFGT
import org.objectweb.asm.Opcodes.IFLE
import org.objectweb.asm.Opcodes.IFLT
import org.objectweb.asm.Opcodes.IFNE
import org.objectweb.asm.Opcodes.IFNONNULL
import org.objectweb.asm.Opcodes.IFNULL
import org.objectweb.asm.Opcodes.IF_ACMPEQ
import org.objectweb.asm.Opcodes.IF_ACMPNE
import org.objectweb.asm.Opcodes.IF_ICMPEQ
import org.objectweb.asm.Opcodes.IF_ICMPGE
import org.objectweb.asm.Opcodes.IF_ICMPGT
import org.objectweb.asm.Opcodes.IF_ICMPLE
import org.objectweb.asm.Opcodes.IF_ICMPLT
import org.objectweb.asm.Opcodes.IF_ICMPNE
import org.objectweb.asm.Opcodes.ILOAD
import org.objectweb.asm.Opcodes.IMUL
import org.objectweb.asm.Opcodes.INEG
import org.objectweb.asm.Opcodes.INSTANCEOF
import org.objectweb.asm.Opcodes.INVOKEINTERFACE
import org.objectweb.asm.Opcodes.INVOKESPECIAL
import org.objectweb.asm.Opcodes.INVOKESTATIC
import org.objectweb.asm.Opcodes.INVOKEVIRTUAL
import org.objectweb.asm.Opcodes.IOR
import org.objectweb.asm.Opcodes.IREM
import org.objectweb.asm.Opcodes.IRETURN
import org.objectweb.asm.Opcodes.ISHL
import org.objectweb.asm.Opcodes.ISHR
import org.objectweb.asm.Opcodes.ISTORE
import org.objectweb.asm.Opcodes.ISUB
import org.objectweb.asm.Opcodes.IUSHR
import org.objectweb.asm.Opcodes.IXOR
import org.objectweb.asm.Opcodes.L2D
import org.objectweb.asm.Opcodes.L2F
import org.objectweb.asm.Opcodes.L2I
import org.objectweb.asm.Opcodes.LADD
import org.objectweb.asm.Opcodes.LALOAD
import org.objectweb.asm.Opcodes.LAND
import org.objectweb.asm.Opcodes.LASTORE
import org.objectweb.asm.Opcodes.LCMP
import org.objectweb.asm.Opcodes.LCONST_0
import org.objectweb.asm.Opcodes.LDIV
import org.objectweb.asm.Opcodes.LLOAD
import org.objectweb.asm.Opcodes.LMUL
import org.objectweb.asm.Opcodes.LNEG
import org.objectweb.asm.Opcodes.LOR
import org.objectweb.asm.Opcodes.LREM
import org.objectweb.asm.Opcodes.LRETURN
import org.objectweb.asm.Opcodes.LSHL
import org.objectweb.asm.Opcodes.LSHR
import org.objectweb.asm.Opcodes.LSTORE
import org.objectweb.asm.Opcodes.LSUB
import org.objectweb.asm.Opcodes.LUSHR
import org.objectweb.asm.Opcodes.LXOR
import org.objectweb.asm.Opcodes.MONITORENTER
import org.objectweb.asm.Opcodes.MONITOREXIT
import org.objectweb.asm.Opcodes.NEW
import org.objectweb.asm.Opcodes.NEWARRAY
import org.objectweb.asm.Opcodes.NOP
import org.objectweb.asm.Opcodes.POP
import org.objectweb.asm.Opcodes.POP2
import org.objectweb.asm.Opcodes.PUTFIELD
import org.objectweb.asm.Opcodes.PUTSTATIC
import org.objectweb.asm.Opcodes.RETURN
import org.objectweb.asm.Opcodes.SALOAD
import org.objectweb.asm.Opcodes.SASTORE
import org.objectweb.asm.Opcodes.SIPUSH
import org.objectweb.asm.Opcodes.SWAP
import org.objectweb.asm.Opcodes.T_BOOLEAN
import org.objectweb.asm.Opcodes.T_BYTE
import org.objectweb.asm.Opcodes.T_CHAR
import org.objectweb.asm.Opcodes.T_DOUBLE
import org.objectweb.asm.Opcodes.T_FLOAT
import org.objectweb.asm.Opcodes.T_INT
import org.objectweb.asm.Opcodes.T_LONG
import org.objectweb.asm.Opcodes.T_SHORT
import org.objectweb.asm.Type.getConstructorDescriptor
import org.objectweb.asm.Type.getDescriptor
import org.objectweb.asm.Type.getInternalName
import org.objectweb.asm.Type.getMethodDescriptor
import org.objectweb.asm.Type.getType
import java.lang.invoke.CallSite
import java.lang.invoke.ConstantCallSite
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodHandles.Lookup
import java.lang.invoke.MethodType
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.reflect.KType
import kotlin.reflect.typeOf

@JitDsl
public class ClassBuilder(
    public val superclass: KType = typeOf<Any>(),
    public val interfaces: List<KType> = emptyList(),
) {
    private val writer = ClassWriter(COMPUTE_FRAMES)
    private val classData = mutableListOf<Any>()

    public fun buildIn(lookup: Lookup): Lookup {
        return lookup.defineHiddenClassWithClassData(writer.toByteArray(), classData.toMutableList(), false)
    }

    @JitDsl
    public inner class MethodBuilder internal constructor(private val methodWriter: MethodVisitor) {
        @OptIn(ExperimentalContracts::class)
        public inline fun <R> asm(block: Asm.() -> R): R {
            contract {
                callsInPlace(block, InvocationKind.EXACTLY_ONCE)
            }

            return Asm().block()
        }

        public fun placeLabel(label: Label): Label {
            return label.also(methodWriter::visitLabel)
        }

        public fun placeLabel(): Label {
            return placeLabel(Label())
        }

        @JitDsl
        public inner class Asm {
            public fun aaload() {
                this@MethodBuilder.methodWriter.visitInsn(AALOAD)
            }

            public fun aastore() {
                this@MethodBuilder.methodWriter.visitInsn(AASTORE)
            }

            public fun aconst_null() {
                this@MethodBuilder.methodWriter.visitInsn(ACONST_NULL)
            }

            public fun aload(index: Int) {
                this@MethodBuilder.methodWriter.visitVarInsn(ALOAD, index)
            }

            public fun anewarray(elementType: Class<*>) {
                this@MethodBuilder.methodWriter.visitTypeInsn(ANEWARRAY, getInternalName(elementType))
            }

            public inline fun <reified T : Any> anewarray() {
                anewarray(T::class.java)
            }

            public fun areturn() {
                this@MethodBuilder.methodWriter.visitInsn(ARETURN)
            }

            public fun arraylength() {
                this@MethodBuilder.methodWriter.visitInsn(ARRAYLENGTH)
            }

            public fun astore(index: Int) {
                this@MethodBuilder.methodWriter.visitVarInsn(ASTORE, index)
            }

            public fun athrow() {
                this@MethodBuilder.methodWriter.visitInsn(ATHROW)
            }

            public fun baload() {
                this@MethodBuilder.methodWriter.visitInsn(BALOAD)
            }

            public fun bastore() {
                this@MethodBuilder.methodWriter.visitInsn(BASTORE)
            }

            public fun bipush(value: Byte) {
                this@MethodBuilder.methodWriter.visitIntInsn(BIPUSH, value.toInt())
            }

            public fun caload() {
                this@MethodBuilder.methodWriter.visitInsn(CALOAD)
            }

            public fun castore() {
                this@MethodBuilder.methodWriter.visitInsn(CASTORE)
            }

            public fun checkcast(type: Class<*>) {
                this@MethodBuilder.methodWriter.visitTypeInsn(CHECKCAST, getInternalName(type))
            }

            public inline fun <reified T : Any> checkcast() {
                checkcast(T::class.java)
            }

            public fun d2f() {
                this@MethodBuilder.methodWriter.visitInsn(D2F)
            }

            public fun d2i() {
                this@MethodBuilder.methodWriter.visitInsn(D2I)
            }

            public fun d2l() {
                this@MethodBuilder.methodWriter.visitInsn(D2L)
            }

            public fun dadd() {
                this@MethodBuilder.methodWriter.visitInsn(DADD)
            }

            public fun daload() {
                this@MethodBuilder.methodWriter.visitInsn(DALOAD)
            }

            public fun dastore() {
                this@MethodBuilder.methodWriter.visitInsn(DASTORE)
            }

            public fun dcmpl() {
                this@MethodBuilder.methodWriter.visitInsn(DCMPL)
            }

            public fun dcmpg() {
                this@MethodBuilder.methodWriter.visitInsn(DCMPG)
            }

            public fun dconst_0() {
                this@MethodBuilder.methodWriter.visitInsn(DCONST_0)
            }

            public fun dconst_1() {
                this@MethodBuilder.methodWriter.visitInsn(DCONST_1)
            }

            public fun ddiv() {
                this@MethodBuilder.methodWriter.visitInsn(DDIV)
            }

            public fun dload(index: Int) {
                this@MethodBuilder.methodWriter.visitVarInsn(DLOAD, index)
            }

            public fun dmul() {
                this@MethodBuilder.methodWriter.visitInsn(DMUL)
            }

            public fun dneg() {
                this@MethodBuilder.methodWriter.visitInsn(DNEG)
            }

            public fun drem() {
                this@MethodBuilder.methodWriter.visitInsn(DREM)
            }

            public fun dreturn() {
                this@MethodBuilder.methodWriter.visitInsn(DRETURN)
            }

            public fun dstore(index: Int) {
                this@MethodBuilder.methodWriter.visitVarInsn(DSTORE, index)
            }

            public fun dsub() {
                this@MethodBuilder.methodWriter.visitInsn(DSUB)
            }

            public fun dup() {
                this@MethodBuilder.methodWriter.visitInsn(DUP)
            }

            public fun dup_x1() {
                this@MethodBuilder.methodWriter.visitInsn(DUP_X1)
            }

            public fun dup_x2() {
                this@MethodBuilder.methodWriter.visitInsn(DUP_X2)
            }

            public fun dup2() {
                this@MethodBuilder.methodWriter.visitInsn(DUP2)
            }

            public fun dup2_x1() {
                this@MethodBuilder.methodWriter.visitInsn(DUP2_X1)
            }

            public fun dup2_x2() {
                this@MethodBuilder.methodWriter.visitInsn(DUP2_X2)
            }

            public fun f2d() {
                this@MethodBuilder.methodWriter.visitInsn(F2D)
            }

            public fun f2i() {
                this@MethodBuilder.methodWriter.visitInsn(F2I)
            }

            public fun f2l() {
                this@MethodBuilder.methodWriter.visitInsn(F2L)
            }

            public fun fadd() {
                this@MethodBuilder.methodWriter.visitInsn(FADD)
            }

            public fun faload() {
                this@MethodBuilder.methodWriter.visitInsn(FALOAD)
            }

            public fun fastore() {
                this@MethodBuilder.methodWriter.visitInsn(FASTORE)
            }

            public fun fcmpl() {
                this@MethodBuilder.methodWriter.visitInsn(FCMPL)
            }

            public fun fcmpg() {
                this@MethodBuilder.methodWriter.visitInsn(FCMPG)
            }

            public fun fconst_0() {
                this@MethodBuilder.methodWriter.visitInsn(FCONST_0)
            }

            public fun fconst_1() {
                this@MethodBuilder.methodWriter.visitInsn(FCONST_1)
            }

            public fun fconst_2() {
                this@MethodBuilder.methodWriter.visitInsn(FCONST_2)
            }

            public fun fdiv() {
                this@MethodBuilder.methodWriter.visitInsn(FDIV)
            }

            public fun fload(index: Int) {
                this@MethodBuilder.methodWriter.visitVarInsn(FLOAD, index)
            }

            public fun fmul() {
                this@MethodBuilder.methodWriter.visitInsn(FMUL)
            }

            public fun fneg() {
                this@MethodBuilder.methodWriter.visitInsn(FNEG)
            }

            public fun frem() {
                this@MethodBuilder.methodWriter.visitInsn(FREM)
            }

            public fun freturn() {
                this@MethodBuilder.methodWriter.visitInsn(FRETURN)
            }

            public fun fstore(index: Int) {
                this@MethodBuilder.methodWriter.visitVarInsn(FSTORE, index)
            }

            public fun fsub() {
                this@MethodBuilder.methodWriter.visitInsn(FSUB)
            }

            public fun get(field: Field) {
                this@MethodBuilder.methodWriter.visitFieldInsn(
                    if (Modifier.isStatic(field.modifiers)) GETSTATIC else GETFIELD,
                    getInternalName(field.declaringClass),
                    field.name,
                    getDescriptor(field.type),
                )
            }

            public fun goto(target: Label) {
                this@MethodBuilder.methodWriter.visitJumpInsn(GOTO, target)
            }

            public fun i2b() {
                this@MethodBuilder.methodWriter.visitInsn(I2B)
            }

            public fun i2c() {
                this@MethodBuilder.methodWriter.visitInsn(I2C)
            }

            public fun i2d() {
                this@MethodBuilder.methodWriter.visitInsn(I2D)
            }

            public fun i2f() {
                this@MethodBuilder.methodWriter.visitInsn(I2F)
            }

            public fun i2l() {
                this@MethodBuilder.methodWriter.visitInsn(I2L)
            }

            public fun i2s() {
                this@MethodBuilder.methodWriter.visitInsn(I2S)
            }

            public fun iadd() {
                this@MethodBuilder.methodWriter.visitInsn(IADD)
            }

            public fun iaload() {
                this@MethodBuilder.methodWriter.visitInsn(IALOAD)
            }

            public fun iand() {
                this@MethodBuilder.methodWriter.visitInsn(IAND)
            }

            public fun iastore() {
                this@MethodBuilder.methodWriter.visitInsn(IASTORE)
            }

            public fun iconst_m1() {
                this@MethodBuilder.methodWriter.visitInsn(ICONST_M1)
            }

            public fun iconst_0() {
                this@MethodBuilder.methodWriter.visitInsn(ICONST_0)
            }

            public fun iconst_1() {
                this@MethodBuilder.methodWriter.visitInsn(ICONST_1)
            }

            public fun iconst_2() {
                this@MethodBuilder.methodWriter.visitInsn(ICONST_2)
            }

            public fun iconst_3() {
                this@MethodBuilder.methodWriter.visitInsn(ICONST_3)
            }

            public fun iconst_4() {
                this@MethodBuilder.methodWriter.visitInsn(ICONST_4)
            }

            public fun iconst_5() {
                this@MethodBuilder.methodWriter.visitInsn(ICONST_5)
            }

            public fun idiv() {
                this@MethodBuilder.methodWriter.visitInsn(IDIV)
            }

            public fun if_acmpeq(target: Label) {
                this@MethodBuilder.methodWriter.visitJumpInsn(IF_ACMPEQ, target)
            }

            public fun if_acmpne(target: Label) {
                this@MethodBuilder.methodWriter.visitJumpInsn(IF_ACMPNE, target)
            }

            public fun if_icmpeq(target: Label) {
                this@MethodBuilder.methodWriter.visitJumpInsn(IF_ICMPEQ, target)
            }

            public fun if_icmpne(target: Label) {
                this@MethodBuilder.methodWriter.visitJumpInsn(IF_ICMPNE, target)
            }

            public fun if_icmplt(target: Label) {
                this@MethodBuilder.methodWriter.visitJumpInsn(IF_ICMPLT, target)
            }

            public fun if_icmpge(target: Label) {
                this@MethodBuilder.methodWriter.visitJumpInsn(IF_ICMPGE, target)
            }

            public fun if_icmpgt(target: Label) {
                this@MethodBuilder.methodWriter.visitJumpInsn(IF_ICMPGT, target)
            }

            public fun if_icmple(target: Label) {
                this@MethodBuilder.methodWriter.visitJumpInsn(IF_ICMPLE, target)
            }

            public fun ifeq(target: Label) {
                this@MethodBuilder.methodWriter.visitJumpInsn(IFEQ, target)
            }

            public fun ifne(target: Label) {
                this@MethodBuilder.methodWriter.visitJumpInsn(IFNE, target)
            }

            public fun iflt(target: Label) {
                this@MethodBuilder.methodWriter.visitJumpInsn(IFLT, target)
            }

            public fun ifge(target: Label) {
                this@MethodBuilder.methodWriter.visitJumpInsn(IFGE, target)
            }

            public fun ifgt(target: Label) {
                this@MethodBuilder.methodWriter.visitJumpInsn(IFGT, target)
            }

            public fun ifle(target: Label) {
                this@MethodBuilder.methodWriter.visitJumpInsn(IFLE, target)
            }

            public fun ifnonnull(target: Label) {
                this@MethodBuilder.methodWriter.visitJumpInsn(IFNONNULL, target)
            }

            public fun ifnull(target: Label) {
                this@MethodBuilder.methodWriter.visitJumpInsn(IFNULL, target)
            }

            public fun iinc(index: Int, increment: Byte) {
                this@MethodBuilder.methodWriter.visitIincInsn(index, increment.toInt())
            }

            public fun iload(index: Int) {
                this@MethodBuilder.methodWriter.visitVarInsn(ILOAD, index)
            }

            public fun imul() {
                this@MethodBuilder.methodWriter.visitInsn(IMUL)
            }

            public fun ineg() {
                this@MethodBuilder.methodWriter.visitInsn(INEG)
            }

            public fun instanceof(type: Class<*>) {
                this@MethodBuilder.methodWriter.visitTypeInsn(INSTANCEOF, getInternalName(type))
            }

            public inline fun <reified T : Any> instanceof() {
                instanceof(T::class.java)
            }

            public fun invokedynamic(type: MethodType, resolver: CallSiteResolver) {
                this@ClassBuilder.classData.add(resolver)
                this@MethodBuilder.methodWriter.visitInvokeDynamicInsn(
                    "_",
                    type.toMethodDescriptorString(),
                    RESOLVED_CALL_SITE_BOOTSTRAP,
                    this@ClassBuilder.classData.lastIndex,
                )
            }

            public fun invokedynamic(callSite: CallSite) {
                this@ClassBuilder.classData.add(callSite)
                this@MethodBuilder.methodWriter.visitInvokeDynamicInsn(
                    "_",
                    callSite.type().toMethodDescriptorString(),
                    IMMEDIATE_CALL_SITE_BOOTSTRAP,
                    this@ClassBuilder.classData.lastIndex,
                )
            }

            public fun invokedynamic(target: MethodHandle) {
                invokedynamic(ConstantCallSite(target))
            }

            public fun invoke(method: Method) {
                val opcode = when {
                    Modifier.isStatic(method.modifiers) -> INVOKESTATIC
                    method.declaringClass.isInterface -> INVOKEINTERFACE
                    else -> INVOKEVIRTUAL
                }

                this@MethodBuilder.methodWriter.visitMethodInsn(
                    opcode,
                    getInternalName(method.declaringClass),
                    method.name,
                    getMethodDescriptor(method),
                    method.declaringClass.isInterface,
                )
            }

            public fun invokespecial(method: Method) {
                this@MethodBuilder.methodWriter.visitMethodInsn(
                    INVOKESPECIAL,
                    getInternalName(method.declaringClass),
                    method.name,
                    getMethodDescriptor(method),
                    method.declaringClass.isInterface,
                )
            }

            public fun invokespecial(constructor: Constructor<*>) {
                this@MethodBuilder.methodWriter.visitMethodInsn(
                    INVOKESPECIAL,
                    getInternalName(constructor.declaringClass),
                    "<init>",
                    getConstructorDescriptor(constructor),
                    false,
                )
            }

            public fun ior() {
                this@MethodBuilder.methodWriter.visitInsn(IOR)
            }

            public fun irem() {
                this@MethodBuilder.methodWriter.visitInsn(IREM)
            }

            public fun ireturn() {
                this@MethodBuilder.methodWriter.visitInsn(IRETURN)
            }

            public fun ishl() {
                this@MethodBuilder.methodWriter.visitInsn(ISHL)
            }

            public fun ishr() {
                this@MethodBuilder.methodWriter.visitInsn(ISHR)
            }

            public fun istore(index: Int) {
                this@MethodBuilder.methodWriter.visitVarInsn(ISTORE, index)
            }

            public fun isub() {
                this@MethodBuilder.methodWriter.visitInsn(ISUB)
            }

            public fun iushr() {
                this@MethodBuilder.methodWriter.visitInsn(IUSHR)
            }

            public fun ixor() {
                this@MethodBuilder.methodWriter.visitInsn(IXOR)
            }

            public fun l2d() {
                this@MethodBuilder.methodWriter.visitInsn(L2D)
            }

            public fun l2f() {
                this@MethodBuilder.methodWriter.visitInsn(L2F)
            }

            public fun l2i() {
                this@MethodBuilder.methodWriter.visitInsn(L2I)
            }

            public fun ladd() {
                this@MethodBuilder.methodWriter.visitInsn(LADD)
            }

            public fun laload() {
                this@MethodBuilder.methodWriter.visitInsn(LALOAD)
            }

            public fun land() {
                this@MethodBuilder.methodWriter.visitInsn(LAND)
            }

            public fun lastore() {
                this@MethodBuilder.methodWriter.visitInsn(LASTORE)
            }

            public fun lcmp() {
                this@MethodBuilder.methodWriter.visitInsn(LCMP)
            }

            public fun lconst_0() {
                this@MethodBuilder.methodWriter.visitInsn(LCONST_0)
            }

            public fun lconst_1() {
                this@MethodBuilder.methodWriter.visitInsn(LCONST_0)
            }

            public fun ldc(value: Int) {
                this@MethodBuilder.methodWriter.visitLdcInsn(value)
            }

            public fun ldc(value: Long) {
                this@MethodBuilder.methodWriter.visitLdcInsn(value)
            }

            public fun ldc(value: Float) {
                this@MethodBuilder.methodWriter.visitLdcInsn(value)
            }

            public fun ldc(value: Double) {
                this@MethodBuilder.methodWriter.visitLdcInsn(value)
            }

            public fun ldc(value: Any) {
                this@ClassBuilder.classData.add(value)
                this@MethodBuilder.methodWriter.visitLdcInsn(
                    ConstantDynamic(
                        "_",
                        getDescriptor(value.javaClass),
                        IMMEDIATE_CONSTANT_BOOTSTRAP,
                        this@ClassBuilder.classData.lastIndex,
                    )
                )
            }

            public fun <T> ldc(type: Class<T>, resolver: ConstantResolver<T>) {
                this@ClassBuilder.classData.add(resolver)
                this@MethodBuilder.methodWriter.visitLdcInsn(
                    ConstantDynamic(
                        "_",
                        getDescriptor(type),
                        RESOLVED_CONSTANT_BOOTSTRAP,
                        this@ClassBuilder.classData.lastIndex,
                    )
                )
            }

            public fun ldiv() {
                this@MethodBuilder.methodWriter.visitInsn(LDIV)
            }

            public fun lload(index: Int) {
                this@MethodBuilder.methodWriter.visitVarInsn(LLOAD, index)
            }

            public fun lmul() {
                this@MethodBuilder.methodWriter.visitInsn(LMUL)
            }

            public fun lneg() {
                this@MethodBuilder.methodWriter.visitInsn(LNEG)
            }

            public fun lookupswitch(default: Label, cases: Map<Int, Label>) {
                this@MethodBuilder.methodWriter.visitLookupSwitchInsn(
                    default,
                    cases.keys.toIntArray(),
                    cases.values.toTypedArray(),
                )
            }

            public fun lor() {
                this@MethodBuilder.methodWriter.visitInsn(LOR)
            }

            public fun lrem() {
                this@MethodBuilder.methodWriter.visitInsn(LREM)
            }

            public fun lreturn() {
                this@MethodBuilder.methodWriter.visitInsn(LRETURN)
            }

            public fun lshl() {
                this@MethodBuilder.methodWriter.visitInsn(LSHL)
            }

            public fun lshr() {
                this@MethodBuilder.methodWriter.visitInsn(LSHR)
            }

            public fun lstore(index: Int) {
                this@MethodBuilder.methodWriter.visitVarInsn(LSTORE, index)
            }

            public fun lsub() {
                this@MethodBuilder.methodWriter.visitInsn(LSUB)
            }

            public fun lushr() {
                this@MethodBuilder.methodWriter.visitInsn(LUSHR)
            }

            public fun lxor() {
                this@MethodBuilder.methodWriter.visitInsn(LXOR)
            }

            public fun monitorenter() {
                this@MethodBuilder.methodWriter.visitInsn(MONITORENTER)
            }

            public fun monitorexit() {
                this@MethodBuilder.methodWriter.visitInsn(MONITOREXIT)
            }

            public fun multianewarray(elementType: Class<*>, numDimensions: Int) {
                this@MethodBuilder.methodWriter.visitMultiANewArrayInsn(getInternalName(elementType), numDimensions)
            }

            public inline fun <reified T : Any> multianewarray(numDimensions: Int) {
                multianewarray(T::class.java, numDimensions)
            }

            public fun new(type: Class<*>) {
                this@MethodBuilder.methodWriter.visitTypeInsn(NEW, getInternalName(type))
            }

            public inline fun <reified T : Any> new() {
                new(T::class.java)
            }

            public fun newarray(elementType: PrimitiveArrayElementType) {
                this@MethodBuilder.methodWriter.visitIntInsn(NEWARRAY, elementType.atype)
            }

            public fun nop() {
                this@MethodBuilder.methodWriter.visitInsn(NOP)
            }

            public fun pop() {
                this@MethodBuilder.methodWriter.visitInsn(POP)
            }

            public fun pop2() {
                this@MethodBuilder.methodWriter.visitInsn(POP2)
            }

            public fun put(field: Field) {
                this@MethodBuilder.methodWriter.visitFieldInsn(
                    if (Modifier.isStatic(field.modifiers)) PUTSTATIC else PUTFIELD,
                    getInternalName(field.declaringClass),
                    field.name,
                    getDescriptor(field.type),
                )
            }

            public fun `return`() {
                this@MethodBuilder.methodWriter.visitInsn(RETURN)
            }

            public fun saload() {
                this@MethodBuilder.methodWriter.visitInsn(SALOAD)
            }

            public fun sastore() {
                this@MethodBuilder.methodWriter.visitInsn(SASTORE)
            }

            public fun sipush(value: Short) {
                this@MethodBuilder.methodWriter.visitIntInsn(SIPUSH, value.toInt())
            }

            public fun swap() {
                this@MethodBuilder.methodWriter.visitInsn(SWAP)
            }

            public fun tableswitch(min: Int, max: Int, default: Label, vararg cases: Label) {
                this@MethodBuilder.methodWriter.visitTableSwitchInsn(min, max, default, *cases)
            }
        }
    }

    @JitDsl
    public class CallSiteResolutionContext internal constructor(
        public val lookup: Lookup,
        public val type: MethodType,
    )

    @JitDsl
    public class ConstantResolutionContext<T> internal constructor(
        public val lookup: Lookup,
        public val type: Class<T>,
    )

    public enum class PrimitiveArrayElementType(internal val atype: kotlin.Int) {
        Boolean(T_BOOLEAN),
        Char(T_CHAR),
        Float(T_FLOAT),
        Double(T_DOUBLE),
        Byte(T_BYTE),
        Short(T_SHORT),
        Int(T_INT),
        Long(T_LONG);
    }

    public fun interface CallSiteResolver {
        public fun CallSiteResolutionContext.resolve(): CallSite
    }

    public fun interface ConstantResolver<T> {
        public fun ConstantResolutionContext<T>.resolve(): T
    }

    public companion object {
        private val RESOLVED_CALL_SITE_BOOTSTRAP = Handle(
            H_INVOKESTATIC,
            getInternalName(ClassBuilder::class.java),
            "resolvedCallSiteBootstrap",
            getMethodDescriptor(
                getType(CallSite::class.java),
                getType(Lookup::class.java),
                getType(String::class.java),
                getType(MethodType::class.java),
                getType(Int::class.java),
            ),
            false,
        )

        private val IMMEDIATE_CALL_SITE_BOOTSTRAP = Handle(
            H_INVOKESTATIC,
            getInternalName(ClassBuilder::class.java),
            "immediateCallSiteBootstrap",
            getMethodDescriptor(
                getType(CallSite::class.java),
                getType(Lookup::class.java),
                getType(String::class.java),
                getType(MethodType::class.java),
                getType(Int::class.java),
            ),
            false,
        )

        private val RESOLVED_CONSTANT_BOOTSTRAP = Handle(
            H_INVOKESTATIC,
            getInternalName(ClassBuilder::class.java),
            "resolvedConstantBootstrap",
            getMethodDescriptor(
                getType(Any::class.java),
                getType(Lookup::class.java),
                getType(String::class.java),
                getType(Class::class.java),
                getType(Int::class.java),
            ),
            false,
        )

        private val IMMEDIATE_CONSTANT_BOOTSTRAP = Handle(
            H_INVOKESTATIC,
            getInternalName(MethodHandles::class.java),
            "classDataAt",
            getMethodDescriptor(
                getType(Any::class.java),
                getType(Lookup::class.java),
                getType(String::class.java),
                getType(Class::class.java),
                getType(Int::class.java),
            ),
            false,
        )

        @JvmStatic @Suppress("UNUSED")
        internal fun resolvedCallSiteBootstrap(lookup: Lookup, name: String, type: MethodType, index: Int): CallSite {
            val classData = MethodHandles.classData(lookup, "_", MutableList::class.java)
            val resolver = classData.removeAt(index) as CallSiteResolver
            val context = CallSiteResolutionContext(lookup, type)
            return resolver.run { context.resolve() }
        }

        @JvmStatic @Suppress("UNUSED")
        internal fun immediateCallSiteBootstrap(lookup: Lookup, name: String, type: MethodType, index: Int): CallSite {
            return MethodHandles.classDataAt(lookup, "_", CallSite::class.java, index)
        }

        @JvmStatic @Suppress("UNUSED")
        internal fun <T> resolvedConstantBootstrap(lookup: Lookup, name: String, type: Class<T>, index: Int): T {
            val classData = MethodHandles.classData(lookup, "_", MutableList::class.java)
            val resolver = @Suppress("UNCHECKED_CAST") (classData.removeAt(index) as ConstantResolver<T>)
            val context = ConstantResolutionContext(lookup, type)
            return type.cast(resolver.run { context.resolve() })
        }
    }
}
