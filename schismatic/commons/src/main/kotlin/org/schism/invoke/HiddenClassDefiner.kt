package org.schism.invoke

import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodHandles.Lookup
import java.lang.invoke.MethodHandles.Lookup.ClassOption

public interface HiddenClassDefiner {
    public val internalNamePrefix: String

    public fun define(
        bytes: ByteArray,
        classData: Any? = null,
        initialize: Boolean = false,
        vararg options: ClassOption,
    ): Lookup
}

@Suppress("NOTHING_TO_INLINE")
public inline fun HiddenClassDefiner(): HiddenClassDefiner {
    return HiddenClassDefiner(MethodHandles.lookup())
}

public fun HiddenClassDefiner(lookup: Lookup): HiddenClassDefiner {
    return HiddenClassDefinerImpl(lookup)
}

private class HiddenClassDefinerImpl(private val lookup: Lookup) : HiddenClassDefiner {
    override val internalNamePrefix: String get() {
        val packageName = lookup.lookupClass().packageName
        return if (packageName.isEmpty()) {
            ""
        } else {
            packageName.replace('.', '/') + '/'
        }
    }

    override fun define(bytes: ByteArray, classData: Any?, initialize: Boolean, vararg options: ClassOption): Lookup {
        return if (classData == null) {
            lookup.defineHiddenClass(bytes, initialize, *options)
        } else {
            lookup.defineHiddenClassWithClassData(bytes, classData, initialize, *options)
        }
    }
}
