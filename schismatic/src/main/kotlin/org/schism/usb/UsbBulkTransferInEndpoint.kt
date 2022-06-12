package org.schism.usb

import org.schism.foreign.NativeBuffer

interface UsbBulkTransferInEndpoint : UsbEndpoint {
    context (UsbDeviceConnection) suspend fun receive(destination: NativeBuffer): Long
}

context (UsbDeviceConnection) suspend inline fun UsbBulkTransferInEndpoint.receiveExact(destination: NativeBuffer) {
    if (receive(destination) != destination.size) {
        throw UsbException("Incomplete IN transfer")
    }
}

context (UsbDeviceConnection) suspend inline fun UsbBulkTransferInEndpoint.receiveZeroLength() {
    receive(NativeBuffer.empty)
}
