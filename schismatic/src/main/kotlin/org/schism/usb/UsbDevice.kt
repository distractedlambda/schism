package org.schism.usb

import org.schism.util.use
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

interface UsbDevice {
    val backend: UsbBackend

    val usbVersion: UShort

    val deviceClass: UByte

    val deviceSubClass: UByte

    val deviceProtocol: UByte

    val vendorId: UShort

    val productId: UShort

    val deviceVersion: UShort

    val configurations: List<UsbConfiguration>

    fun connect(): UsbDeviceConnection
}

@OptIn(ExperimentalContracts::class)
suspend inline fun <R> UsbDevice.connect(block: UsbDeviceConnection.() -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return connect().use(block)
}
