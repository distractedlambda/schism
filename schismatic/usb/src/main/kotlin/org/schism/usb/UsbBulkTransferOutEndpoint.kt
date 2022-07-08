package org.schism.usb

public class UsbBulkTransferOutEndpoint internal constructor(
    alternateSetting: UsbAlternateSetting,
    maxPacketSize: UShort,
) : UsbEndpoint(alternateSetting, maxPacketSize)
