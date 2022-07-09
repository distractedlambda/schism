package org.schism.usb

import org.schism.ffi.wrapArray
import org.schism.usb.Libusb.ConfigDescriptor

public class UsbConfiguration internal constructor(public val device: UsbDevice, descriptor: ConfigDescriptor) {
    internal val value = descriptor.bConfigurationValue

    public val interfaces: List<UsbInterface> = kotlin.run {
        val interfaceStructs = Libusb.Interface.wrapArray(descriptor.`interface`, descriptor.bNumInterfaces.toLong())
        List(descriptor.bNumInterfaces.toInt()) {
            interfaceStructs[it.toLong()]
        }
    }
}
