package org.schism.foreign

import java.lang.System.getProperty
import java.lang.foreign.ValueLayout.ADDRESS

internal enum class IntOrLong {
    INT,
    LONG;
}

internal val ADDRESS_TYPE = when (ADDRESS.bitSize()) {
    32L -> IntOrLong.INT
    64L -> IntOrLong.LONG
    else -> throw UnsupportedOperationException("Unexpected native address bit size: ${ADDRESS.bitSize()}")
}

internal val C_LONG_TYPE = when (ADDRESS_TYPE) {
    IntOrLong.INT -> IntOrLong.INT
    IntOrLong.LONG -> when {
        getProperty("os.name").startsWith("Windows") -> IntOrLong.INT
        else -> IntOrLong.LONG
    }
}
