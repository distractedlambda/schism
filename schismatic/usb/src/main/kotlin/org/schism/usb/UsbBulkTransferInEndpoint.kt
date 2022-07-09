package org.schism.usb

import org.schism.usb.Libusb.EndpointDescriptor

public class UsbBulkTransferInEndpoint internal constructor(
    alternateSetting: UsbAlternateSetting,
    descriptor: EndpointDescriptor,
) : UsbEndpoint(alternateSetting, descriptor) {
    public companion object
}
