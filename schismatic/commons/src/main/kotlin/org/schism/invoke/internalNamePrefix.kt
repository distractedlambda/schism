package org.schism.invoke

import java.lang.invoke.MethodHandles

public fun MethodHandles.Lookup.internalNamePrefix(): String {
    val packageName = lookupClass().packageName
    return when {
        packageName.isEmpty() -> ""
        else -> packageName.replace('.', '/') + '/'
    }
}
