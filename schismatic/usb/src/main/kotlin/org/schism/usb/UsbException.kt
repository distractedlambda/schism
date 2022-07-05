package org.schism.usb

public class UsbException internal constructor(
    message: String? = null,
    cause: Throwable? = null,
) : Exception(message, cause)
