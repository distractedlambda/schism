package org.schism.foreign

import java.lang.foreign.ValueLayout

data class NativeMember<L: ValueLayout>(val layout: L, val offset: ByteOffset)
