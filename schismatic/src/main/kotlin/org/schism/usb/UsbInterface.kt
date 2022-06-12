package org.schism.usb

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

interface UsbInterface {
    val configuration: UsbConfiguration

    val alternateSettings: List<UsbAlternateSetting>

    context (UsbDeviceConnection) fun claim()

    context (UsbDeviceConnection) suspend fun release()
}

val UsbInterface.device: UsbDevice
    get() = configuration.device

val UsbInterface.backend: UsbBackend
    get() = configuration.backend

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
