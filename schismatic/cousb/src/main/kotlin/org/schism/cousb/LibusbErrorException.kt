package org.schism.cousb

import java.lang.foreign.MemoryAddress

public class LibusbErrorException(public val code: Int) : Exception(errorMessage(code))

private fun errorMessage(code: Int): String {
    return (Libusb.strerror.invokeExact(code) as MemoryAddress).getUtf8String(0)
}
