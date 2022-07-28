package org.schism.usb

import org.schism.foreign.asStructArray
import org.schism.usb.Libusb.ConfigDescriptor
import org.schism.usb.Libusb.Interface

public class UsbConfiguration internal constructor(public val device: UsbDevice, descriptor: ConfigDescriptor) {
    internal val value = descriptor.bConfigurationValue

    public val interfaces: List<UsbInterface> = kotlin.run {
        val interfaceStructs = descriptor.iface.asStructArray(Interface.Type, descriptor.bNumInterfaces.toLong())
        List(descriptor.bNumInterfaces.toInt()) {
            UsbInterface(this, interfaceStructs[it.toLong()])
        }
    }

    public val extraDescriptors: List<UsbDescriptor> = parseExtraDescriptors(descriptor.extra, descriptor.extra_length)
}
