package org.schism.reflect

import org.objectweb.asm.Type.getConstructorDescriptor
import org.objectweb.asm.Type.getMethodDescriptor
import java.lang.reflect.Constructor
import java.lang.reflect.Method

public val Class<*>.internalName: String get() {
    return name.replace('.', '/')
}

public val Method.descriptorString: String get() {
    return getMethodDescriptor(this)
}

public val Constructor<*>.descriptorString: String get() {
    return getConstructorDescriptor(this)
}
