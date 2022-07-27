package org.schism.invoke

import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles.varHandleExactInvoker
import java.lang.invoke.MethodHandles.varHandleInvoker
import java.lang.invoke.VarHandle
import java.lang.invoke.VarHandle.AccessMode

public fun VarHandle.invoker(accessMode: AccessMode): MethodHandle {
    return varHandleInvoker(accessMode, accessModeType(accessMode)).bindTo(this)
}

public fun VarHandle.exactInvoker(accessMode: AccessMode): MethodHandle {
    return varHandleExactInvoker(accessMode, accessModeType(accessMode)).bindTo(this)
}
