package org.schism.usb

import org.schism.usb.Libusb.EndpointDescriptor

public sealed class UsbEndpoint(
    public val alternateSetting: UsbAlternateSetting,
    descriptor: EndpointDescriptor,
) {
    internal val address = descriptor.bEndpointAddress

    public val maxPacketSize: UShort = descriptor.wMaxPacketSize

    public val extraDescriptors: List<UsbDescriptor> = parseExtraDescriptors(descriptor.extra, descriptor.extra_length)
}
