package org.schism.usb

public class UsbBulkTransferInEndpoint internal constructor(
    alternateSetting: UsbAlternateSetting,
    maxPacketSize: UShort,
) : UsbEndpoint(alternateSetting, maxPacketSize)
