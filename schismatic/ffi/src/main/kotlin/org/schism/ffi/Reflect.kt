package org.schism.ffi

import java.lang.invoke.MethodHandles

internal fun MethodHandles.Lookup.lookupPackagePrefix(): String {
    val packageName = lookupClass().packageName
    return if (packageName.isEmpty()) {
        ""
    } else {
        "${packageName.replace('.', '/')}/"
    }
}
