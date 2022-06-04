package org.schism.bytes

import java.lang.foreign.MemorySession
import java.lang.ref.Cleaner

public fun globalMemorySession(): MemorySession =
    MemorySession.global()

public fun newImplicitMemorySession(): MemorySession =
    MemorySession.openImplicit()

public fun newConfinedMemorySession(): MemorySession =
    MemorySession.openConfined()

public fun newConfinedMemorySession(cleaner: Cleaner): MemorySession =
    MemorySession.openConfined(cleaner)

public fun newSharedMemorySession(): MemorySession =
    MemorySession.openShared()

public fun newSharedMemorySession(cleaner: Cleaner): MemorySession =
    MemorySession.openShared(cleaner)
