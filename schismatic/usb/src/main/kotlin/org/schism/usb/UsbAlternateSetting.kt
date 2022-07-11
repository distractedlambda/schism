package org.schism.usb

import org.schism.ffi.wrapArray
import org.schism.usb.Libusb.EndpointDescriptor
import org.schism.usb.Libusb.InterfaceDescriptor
import org.schism.util.contextual

public class UsbAlternateSetting internal constructor(
    public val `interface`: UsbInterface,
    descriptor: InterfaceDescriptor,
) {
    public val interfaceClass: UByte = descriptor.bInterfaceClass
    public val interfaceSubClass: UByte = descriptor.bInterfaceSubClass
    public val interfaceProtocol: UByte = descriptor.bInterfaceProtocol

    public val configuration: UsbConfiguration get() {
        return `interface`.configuration
    }

    public val device: UsbDevice get() {
        return `interface`.device
    }

    public val endpoints: List<UsbEndpoint> = kotlin.run {
        val endpointDescriptors = EndpointDescriptor.wrapArray(descriptor.endpoint, descriptor.bNumEndpoints.toLong())
        buildList {
            for (endpointDescriptor in endpointDescriptors) {
                when (endpointDescriptor.bmAttributes and 0b11u) {
                    2.toUByte() -> when (endpointDescriptor.bEndpointAddress.toUInt() shr 7) {
                        0u -> add(UsbBulkTransferOutEndpoint(contextual<UsbAlternateSetting>(), endpointDescriptor))
                        1u -> add(UsbBulkTransferInEndpoint(contextual<UsbAlternateSetting>(), endpointDescriptor))
                    }

                    // TODO: handle other endpoint types
                }
            }
        }
    }

    public val extraDescriptors: List<UsbDescriptor> = parseExtraDescriptors(descriptor.extra, descriptor.extra_length)
}
