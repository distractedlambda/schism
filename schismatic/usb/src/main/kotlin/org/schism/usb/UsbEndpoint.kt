package org.schism.usb

import org.schism.usb.Libusb.EndpointDescriptor

public sealed class UsbEndpoint(
    public val alternateSetting: UsbAlternateSetting,
    descriptor: EndpointDescriptor,
) {
    internal val address = descriptor.bEndpointAddress

    public val maxPacketSize: UShort = descriptor.wMaxPacketSize

    public val extraDescriptors: List<UsbDescriptor> = parseExtraDescriptors(descriptor.extra, descriptor.extra_length)

    public val iface: UsbInterface get() {
        return alternateSetting.iface
    }

    public val configuration: UsbConfiguration get() {
        return alternateSetting.configuration
    }

    public val device: UsbDevice get() {
        return alternateSetting.device
    }
}
