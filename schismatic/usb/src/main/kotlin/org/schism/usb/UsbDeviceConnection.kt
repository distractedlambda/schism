package org.schism.usb

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.schism.coroutines.SharedLifetime
import org.schism.coroutines.SuspendingAutocloseable
import org.schism.ffi.NativeCallable
import org.schism.ffi.nativeCallable
import org.schism.ffi.wrap
import org.schism.memory.Memory
import org.schism.memory.NativeAddress
import org.schism.memory.free
import org.schism.memory.isNULL
import org.schism.memory.malloc
import org.schism.memory.nativeMemory
import org.schism.memory.nextLeUtf16
import org.schism.memory.plus
import org.schism.memory.putLeUShort
import org.schism.memory.putUByte
import org.schism.memory.readUByte
import org.schism.memory.withNativeInt
import org.schism.usb.Libusb.TransferStatus
import org.schism.usb.Libusb.TransferType
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.resumeWithException

public class UsbDeviceConnection internal constructor(
    public val device: UsbDevice,
    private val nativeHandle: NativeAddress,
) : SuspendingAutocloseable {
    private val lifetime = SharedLifetime()

    private fun transfer(
        continuation: CancellableContinuation<Int>,
        callback: NativeCallable,
        endpointAddress: UByte,
        type: UByte,
        buffer: NativeAddress,
        bufferLength: Int,
    ) {
        var submitted = false
        try {
            lifetime.retain()
            try {
                val transfer = freeTransfers.poll() ?: kotlin.run {
                    val nativeAddress = libusb.alloc_transfer(0)

                    if (nativeAddress.isNULL()) {
                        throw OutOfMemoryError("Failed to allocate libusb transfer")
                    }

                    Transfer(nativeAddress).also {
                        transfersByAddress[nativeAddress] = it
                    }
                }

                try {
                    transfer.native.let {
                        it.dev_handle = nativeHandle
                        it.callback = callback.address
                        it.endpoint = endpointAddress
                        it.type = type
                        it.buffer = buffer
                        it.length = bufferLength
                    }

                    transfer.continuation = continuation
                    transfer.deviceConnectionLifetime = lifetime

                    submitted = true
                    when (val returnCode = libusb.submit_transfer(transfer.nativeAddress)) {
                        0 -> Unit
                        else -> {
                            submitted = false
                            throw UsbException(libusbErrorMessage(returnCode))
                        }
                    }

                    continuation.invokeOnCancellation {
                        // FIXME: make lock-free?
                        synchronized(transfer) {
                            // FIXME: replace this check with something less fragile
                            if (transfer.continuation !== continuation) return@invokeOnCancellation
                            libusb.cancel_transfer(transfer.nativeAddress)
                        }
                    }
                } finally {
                    if (!submitted) {
                        transfer.continuation = null
                        transfer.deviceConnectionLifetime = null
                        freeTransfers.add(transfer)
                    }
                }
            } finally {
                if (!submitted) {
                    lifetime.release()
                }
            }
        } catch (exception: Throwable) {
            if (!submitted) {
                continuation.resumeWithException(exception)
            } else {
                throw exception
            }
        } finally {
            if (!submitted && !buffer.isNULL()) {
                free(buffer)
            }
        }
    }

    private suspend inline fun transfer(
        endpointAddress: UByte,
        callback: NativeCallable,
        type: UByte,
        buffer: NativeAddress,
        bufferLength: Int,
    ): Int {
        return suspendCancellableCoroutine { continuation ->
            transfer(continuation, callback, endpointAddress, type, buffer, bufferLength)
        }
    }

    private suspend fun getString(index: UByte): String? {
        if (index == 0.toUByte()) {
            return null
        }

        val bufferLength = 8 + 255
        val buffer = malloc(bufferLength.toLong())

        nativeMemory(buffer, 8).encoder().run {
            putUByte(0x80u)
            putUByte(0x06u)
            putUByte(index)
            putUByte(0x03u)
            putShort(0)
            putLeUShort(255u)
        }

        transfer(0x00u, nativeInTransferCallback, TransferType.CONTROL, buffer, bufferLength)

        try {
            val descriptorLength = (buffer + 8).readUByte().toInt()
            return nativeMemory(buffer, bufferLength.toLong())
                .slice(offset = 10)
                .decoder()
                .nextLeUtf16((descriptorLength - 2) / 2)
        } finally {
            free(buffer)
        }
    }

    public suspend fun getManufacturerName(): String? {
        return getString(device.iManufacturer)
    }

    public suspend fun getProductName(): String? {
        return getString(device.iProduct)
    }

    public suspend fun getSerialNumber(): String? {
        return getString(device.iSerialNumber)
    }

    public suspend fun getName(alternateSetting: UsbAlternateSetting): String? {
        require(alternateSetting.device === device)
        return getString(alternateSetting.iInterface)
    }

    public suspend fun getActiveConfiguration(): UsbConfiguration? {
        val activeValue = withContext(Dispatchers.IO) {
            lifetime.withRetained {
                withNativeInt { configurationValue ->
                    checkLibusbReturn(libusb.get_configuration(nativeHandle, configurationValue.address))
                    configurationValue.value
                }
            }
        }

        return device.configurations.firstOrNull { it.value == activeValue.toUByte() }
    }

    public suspend fun resetDevice() {
        withContext(Dispatchers.IO) {
            lifetime.withRetained {
                checkLibusbReturn(libusb.reset_device(nativeHandle))
            }
        }
    }

    public fun claim(iface: UsbInterface) {
        require(iface.device === device)

        lifetime.withRetained {
            checkLibusbReturn(libusb.claim_interface(nativeHandle, iface.number.toInt()))
        }
    }

    public suspend fun release(iface: UsbInterface) {
        require(iface.device === device)

        withContext(NonCancellable + Dispatchers.IO) {
            lifetime.withRetained {
                checkLibusbReturn(libusb.release_interface(nativeHandle, iface.number.toInt()))
            }
        }
    }

    @OptIn(ExperimentalContracts::class)
    public suspend inline fun <R> withClaim(iface: UsbInterface, block: () -> R): R {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }

        claim(iface)

        try {
            return block()
        } finally {
            release(iface)
        }
    }

    public suspend fun makeActive(alternateSetting: UsbAlternateSetting) {
        require(alternateSetting.device === device)

        withContext(Dispatchers.IO) {
            lifetime.withRetained {
                checkLibusbReturn(
                    libusb.set_interface_alt_setting(
                        nativeHandle,
                        alternateSetting.iface.number.toInt(),
                        alternateSetting.value.toInt(),
                    )
                )
            }
        }
    }

    public suspend fun clearHalt(endpoint: UsbEndpoint) {
        require(endpoint.device === device)

        withContext(Dispatchers.IO) {
            lifetime.withRetained {
                checkLibusbReturn(libusb.clear_halt(nativeHandle, endpoint.address))
            }
        }
    }

    @OptIn(ExperimentalContracts::class)
    public suspend fun sendPacket(endpoint: UsbBulkTransferOutEndpoint, fill: suspend (Memory) -> Long) {
        contract {
            callsInPlace(fill, InvocationKind.EXACTLY_ONCE)
        }

        require(endpoint.device === device)

        val buffer = malloc(endpoint.maxPacketSize.toLong())

        val length = try {
            fill(nativeMemory(buffer, endpoint.maxPacketSize.toLong()))
        } catch (exception: Throwable) {
            free(buffer)
            throw exception
        }

        val sentLength = transfer(
            endpoint.address,
            nativeOutTransferCallback,
            TransferType.BULK,
            buffer,
            length.toInt(),
        )

        if (sentLength.toLong() != length) {
            throw UsbException("Incomplete outgoing transfer")
        }
    }

    public suspend fun sendZeroLengthPacket(endpoint: UsbBulkTransferOutEndpoint) {
        require(endpoint.device === device)
        transfer(endpoint.address, nativeZeroLengthTransferCallback, TransferType.BULK, NativeAddress.NULL, 0)
    }

    @OptIn(ExperimentalContracts::class)
    public suspend fun <R> receivePacket(endpoint: UsbBulkTransferInEndpoint, process: suspend (Memory) -> R): R {
        contract {
            callsInPlace(process, InvocationKind.EXACTLY_ONCE)
        }

        require(endpoint.device === device)

        val buffer = malloc(endpoint.maxPacketSize.toLong())

        val receivedLength = transfer(
            endpoint.address,
            nativeInTransferCallback,
            TransferType.BULK,
            buffer,
            endpoint.maxPacketSize.toInt(),
        )

        try {
            return process(nativeMemory(buffer, receivedLength.toLong()).asReadOnly())
        } finally {
            free(buffer)
        }
    }

    public suspend fun receiveZeroLengthPacket(endpoint: UsbBulkTransferInEndpoint) {
        require(endpoint.device === device)
        transfer(endpoint.address, nativeZeroLengthTransferCallback, TransferType.BULK, NativeAddress.NULL, 0)
    }

    override suspend fun close() {
        if (lifetime.end()) {
            libusb.close(nativeHandle)
        }
    }
}

private class Transfer(val nativeAddress: NativeAddress) {
    var continuation: CancellableContinuation<Int>? = null
    var deviceConnectionLifetime: SharedLifetime? = null

    val native: Libusb.Transfer get() {
        return Libusb.Transfer.Type.wrap(nativeAddress)
    }

    fun claimFromCallback(): CancellableContinuation<Int> {
        val claimedContinuation = synchronized(this) {
            continuation!!.also { continuation = null }
        }

        deviceConnectionLifetime!!.release()
        deviceConnectionLifetime = null

        return claimedContinuation
    }
}

private val transfersByAddress = ConcurrentHashMap<NativeAddress, Transfer>()
private val freeTransfers = ConcurrentLinkedQueue<Transfer>()
private val nativeInTransferCallback = nativeCallable(::inTransferCallback)
private val nativeOutTransferCallback = nativeCallable(::outTransferCallback)
private val nativeZeroLengthTransferCallback = nativeCallable(::zeroLengthTransferCallback)

private fun transferError(status: Int): UsbException {
    return UsbException(when (status) {
        TransferStatus.ERROR -> "Transfer error"
        TransferStatus.TIMED_OUT -> "Transfer timed out"
        TransferStatus.CANCELED -> "Transfer was canceled"
        TransferStatus.STALL -> "Transfer stalled"
        TransferStatus.NO_DEVICE -> "Transfer target no longer connected"
        TransferStatus.OVERFLOW -> "Transfer overflowed"
        else -> "Unknown transfer error"
    })
}

private fun inTransferCallback(transferAddress: NativeAddress) {
    val transfer = transfersByAddress[transferAddress]!!
    val continuation = transfer.claimFromCallback()

    val buffer: NativeAddress
    val status: Int
    val actualLength: Int

    transfer.native.let {
        buffer = it.buffer
        status = it.status
        actualLength = it.actual_length
        it.buffer = NativeAddress.NULL
    }

    freeTransfers.add(transfer)

    if (status == TransferStatus.COMPLETED) {
        @OptIn(ExperimentalCoroutinesApi::class)
        continuation.resume(actualLength) {
            free(buffer)
        }
    } else {
        free(buffer)
        continuation.resumeWithException(transferError(status))
    }
}

private fun outTransferCallback(transferAddress: NativeAddress) {
    val transfer = transfersByAddress[transferAddress]!!
    val continuation = transfer.claimFromCallback()

    val buffer: NativeAddress
    val status: Int
    val actualLength: Int

    transfer.native.let {
        buffer = it.buffer
        status = it.status
        actualLength = it.actual_length
        it.buffer = NativeAddress.NULL
    }

    freeTransfers.add(transfer)
    free(buffer)

    continuation.resumeWith(
        if (status == TransferStatus.COMPLETED) {
            success(actualLength)
        } else {
            failure(transferError(status))
        }
    )
}

private fun zeroLengthTransferCallback(transferAddress: NativeAddress) {
    val transfer = transfersByAddress[transferAddress]!!
    val continuation = transfer.claimFromCallback()

    val status = transfer.native.status
    freeTransfers.add(transfer)

    continuation.resumeWith(
        if (status == TransferStatus.COMPLETED) {
            success(0)
        } else {
            failure(transferError(status))
        }
    )
}
