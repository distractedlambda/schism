package org.schism.usb

import org.schism.usb.Libusb.EndpointDescriptor

public class UsbBulkTransferOutEndpoint internal constructor(
    alternateSetting: UsbAlternateSetting,
    descriptor: EndpointDescriptor,
) : UsbEndpoint(alternateSetting, descriptor) {
    public companion object
}
