package org.schism.usb

import org.schism.foreign.NativeBuffer
import org.schism.util.SuspendingAutocloseable
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

interface UsbDeviceConnection : SuspendingAutocloseable {
    val device: UsbDevice

    suspend fun resetDevice()

    suspend fun getActiveConfiguration(): UsbConfiguration?

    fun UsbInterface.claim()

    suspend fun UsbInterface.release()

    suspend fun UsbAlternateSetting.makeActive()

    suspend fun UsbEndpoint.clearHalt()

    suspend fun UsbBulkTransferInEndpoint.receive(destination: NativeBuffer): Long

    suspend fun UsbBulkTransferOutEndpoint.send(source: NativeBuffer): Long
}

context (UsbDeviceConnection) suspend inline fun UsbConfiguration.isActive(): Boolean {
    return getActiveConfiguration() === this@UsbConfiguration
}

context (UsbDeviceConnection) @OptIn(ExperimentalContracts::class)
suspend inline fun <R> UsbInterface.withClaim(block: () -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    claim()

    try {
        return block()
    } finally {
        release()
    }
}

context (UsbDeviceConnection) suspend inline fun UsbBulkTransferInEndpoint.receiveExact(destination: NativeBuffer) {
    if (receive(destination) != destination.size) {
        throw UsbException("Incomplete IN transfer")
    }
}

context (UsbDeviceConnection) suspend inline fun UsbBulkTransferInEndpoint.receiveZeroLength() {
    receive(NativeBuffer.empty)
}

context (UsbDeviceConnection) suspend inline fun UsbBulkTransferOutEndpoint.sendExact(source: NativeBuffer) {
    if (send(source) != source.size) {
        throw UsbException("Incomplete OUT transfer")
    }
}

context (UsbDeviceConnection) suspend inline fun UsbBulkTransferOutEndpoint.sendZeroLength() {
    send(NativeBuffer.empty)
}
