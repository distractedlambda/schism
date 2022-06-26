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

    suspend fun getManufacturerName(): String?

    suspend fun getProductName(): String?

    suspend fun getSerialNumber(): String?

    fun claim(`interface`: UsbInterface)

    suspend fun release(`interface`: UsbInterface)

    suspend fun makeActive(alternateSetting: UsbAlternateSetting)

    suspend fun getName(alternateSetting: UsbAlternateSetting): String?

    suspend fun clearHalt(endpoint: UsbEndpoint)

    suspend fun receive(endpoint: UsbBulkTransferInEndpoint, destination: NativeBuffer): Long

    suspend fun send(endpoint: UsbBulkTransferOutEndpoint, source: NativeBuffer): Long
}

suspend inline fun UsbDeviceConnection.isActive(configuration: UsbConfiguration): Boolean {
    return getActiveConfiguration() === configuration
}

@OptIn(ExperimentalContracts::class)
suspend inline fun <R> UsbDeviceConnection.withClaim(`interface`: UsbInterface, block: () -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    claim(`interface`)

    try {
        return block()
    } finally {
        release(`interface`)
    }
}

suspend inline fun UsbDeviceConnection.receiveExact(endpoint: UsbBulkTransferInEndpoint, destination: NativeBuffer) {
    if (receive(endpoint, destination) != destination.size) {
        throw UsbException("Incomplete IN transfer")
    }
}

suspend inline fun UsbDeviceConnection.receiveZeroLength(endpoint: UsbBulkTransferInEndpoint) {
    receive(endpoint, NativeBuffer.empty)
}

suspend inline fun UsbDeviceConnection.sendExact(endpoint: UsbBulkTransferOutEndpoint, source: NativeBuffer) {
    if (send(endpoint, source) != source.size) {
        throw UsbException("Incomplete OUT transfer")
    }
}

suspend inline fun UsbDeviceConnection.sendZeroLength(endpoint: UsbBulkTransferOutEndpoint) {
    send(endpoint, NativeBuffer.empty)
}
