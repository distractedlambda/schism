package org.schism.usb

import org.schism.foreign.NativeBuffer

interface UsbBulkTransferOutEndpoint : UsbEndpoint {
    context (UsbDeviceConnection) suspend fun send(source: NativeBuffer): Long
}

context (UsbDeviceConnection) suspend inline fun UsbBulkTransferOutEndpoint.sendExact(source: NativeBuffer) {
    if (send(source) != source.size) {
        throw UsbException("Incomplete OUT transfer")
    }
}

context (UsbDeviceConnection) suspend inline fun UsbBulkTransferOutEndpoint.sendZeroLength() {
    send(NativeBuffer.empty)
}
