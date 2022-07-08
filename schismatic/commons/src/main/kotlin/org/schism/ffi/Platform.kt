package org.schism.ffi

import java.lang.System.getProperty
import java.lang.foreign.ValueLayout.ADDRESS

internal val ADDRESS_IS_4_BYTES = ADDRESS.bitSize() == 32L

// FIXME: use less fragile detection logic
internal val C_LONG_IS_4_BYTES = ADDRESS.bitSize() == 32L || getProperty("os.name").startsWith("Windows")
