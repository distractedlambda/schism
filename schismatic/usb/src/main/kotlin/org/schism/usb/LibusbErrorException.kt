package org.schism.usb

import java.lang.foreign.MemoryAddress

public class LibusbErrorException(public val code: Int) : Exception(errorMessage(code)) {
    public companion object
}

private fun errorMessage(code: Int): String {
    return (Libusb.strerror(code) as MemoryAddress).getUtf8String(0)
}
