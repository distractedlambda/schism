package org.schism.usb

import org.schism.ffi.wrapArray
import org.schism.usb.Libusb.ConfigDescriptor
import org.schism.usb.Libusb.Interface

public class UsbConfiguration internal constructor(public val device: UsbDevice, descriptor: ConfigDescriptor) {
    internal val value = descriptor.bConfigurationValue

    public val interfaces: List<UsbInterface> = kotlin.run {
        val interfaceStructs = Interface.wrapArray(descriptor.`interface`, descriptor.bNumInterfaces.toLong())
        List(descriptor.bNumInterfaces.toInt()) {
            UsbInterface(this, interfaceStructs[it.toLong()])
        }
    }

    public val extraDescriptors: List<UsbDescriptor> = parseExtraDescriptors(descriptor.extra, descriptor.extra_length)

    public companion object
}
