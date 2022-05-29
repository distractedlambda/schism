package org.schism.schismatic

import java.lang.foreign.MemoryAddress

class LibusbErrorException(val code: Int) : Exception(errorMessage(code))

private fun errorMessage(code: Int): String {
    return (Libusb.strerror.invokeExact(code) as MemoryAddress).getUtf8String(0)
}
