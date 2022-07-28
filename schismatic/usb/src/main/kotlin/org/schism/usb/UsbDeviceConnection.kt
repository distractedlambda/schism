package org.schism.usb

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.schism.coroutines.SharedLifetime
import org.schism.foreign.asMemorySegment
import org.schism.foreign.asStruct
import org.schism.foreign.decoder
import org.schism.foreign.encoder
import org.schism.foreign.free
import org.schism.foreign.getInt
import org.schism.foreign.getUByte
import org.schism.foreign.implicitMemorySession
import org.schism.foreign.isNULL
import org.schism.foreign.malloc
import org.schism.foreign.nativeCallable
import org.schism.foreign.withConfinedMemorySession
import org.schism.foreign.withSharedMemorySession
import org.schism.usb.Libusb.TransferStatus
import org.schism.usb.Libusb.TransferType
import java.lang.foreign.MemoryAddress
import java.lang.foreign.MemoryAddress.NULL
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout.JAVA_INT
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
    private val nativeHandle: MemoryAddress,
    private val lifetime: SharedLifetime,
) {
    private fun transfer(
        continuation: CancellableContinuation<Int>,
        callback: MemoryAddress,
        endpointAddress: UByte,
        type: UByte,
        buffer: MemoryAddress,
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
                        it.callback = callback
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
            if (!submitted) {
                free(buffer)
            }
        }
    }

    private suspend inline fun transfer(
        endpointAddress: UByte,
        callback: MemoryAddress,
        type: UByte,
        buffer: MemoryAddress,
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

        buffer.asMemorySegment(8).encoder().run {
            putUByte(0x80u)
            putUByte(0x06u)
            putUByte(index)
            putUByte(0x03u)
            putShort(0)
            putLeUShort(255u)
        }

        transfer(0x00u, nativeInTransferCallback.address(), TransferType.CONTROL, buffer, bufferLength)

        try {
            val descriptorLength = buffer.getUByte(8).toInt()
            return buffer
                .asMemorySegment(bufferLength.toLong())
                .decoder(offset = 10)
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
                withConfinedMemorySession {
                    val buffer = allocate(JAVA_INT) // FIXME: C Int?
                    checkLibusbReturn(libusb.get_configuration(nativeHandle, buffer))
                    buffer.getInt()
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
    public suspend fun sendPacket(endpoint: UsbBulkTransferOutEndpoint, fill: suspend (MemorySegment) -> Long) {
        contract {
            callsInPlace(fill, InvocationKind.EXACTLY_ONCE)
        }

        require(endpoint.device === device)

        val buffer = malloc(endpoint.maxPacketSize.toLong())

        val length = try {
            withSharedMemorySession {
                fill(buffer.asMemorySegment(endpoint.maxPacketSize.toLong(), session = asNonCloseable()))
            }
        } catch (exception: Throwable) {
            free(buffer)
            throw exception
        }

        val sentLength = transfer(
            endpoint.address,
            nativeOutTransferCallback.address(),
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
        transfer(endpoint.address, nativeZeroLengthTransferCallback.address(), TransferType.BULK, NULL, 0)
    }

    @OptIn(ExperimentalContracts::class)
    public suspend fun <R> receivePacket(
        endpoint: UsbBulkTransferInEndpoint,
        process: suspend (MemorySegment) -> R,
    ): R {
        contract {
            callsInPlace(process, InvocationKind.EXACTLY_ONCE)
        }

        require(endpoint.device === device)

        val buffer = malloc(endpoint.maxPacketSize.toLong())

        val receivedLength = transfer(
            endpoint.address,
            nativeInTransferCallback.address(),
            TransferType.BULK,
            buffer,
            endpoint.maxPacketSize.toInt(),
        )

        try {
            withSharedMemorySession {
                return process(buffer.asMemorySegment(receivedLength.toLong(), asNonCloseable()).asReadOnly())
            }
        } finally {
            free(buffer)
        }
    }

    public suspend fun receiveZeroLengthPacket(endpoint: UsbBulkTransferInEndpoint) {
        require(endpoint.device === device)
        transfer(endpoint.address, nativeZeroLengthTransferCallback.address(), TransferType.BULK, NULL, 0)
    }
}

private class Transfer(val nativeAddress: MemoryAddress) {
    var continuation: CancellableContinuation<Int>? = null
    var deviceConnectionLifetime: SharedLifetime? = null

    val native: Libusb.Transfer get() {
        return nativeAddress.asStruct(Libusb.Transfer.Type)
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

private val transfersByAddress = ConcurrentHashMap<MemoryAddress, Transfer>()
private val freeTransfers = ConcurrentLinkedQueue<Transfer>()

private val callbacksSession = implicitMemorySession()
private val nativeInTransferCallback = callbacksSession.nativeCallable(::inTransferCallback)
private val nativeOutTransferCallback = callbacksSession.nativeCallable(::outTransferCallback)
private val nativeZeroLengthTransferCallback = callbacksSession.nativeCallable(::zeroLengthTransferCallback)

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

private fun inTransferCallback(transferAddress: MemoryAddress) {
    val transfer = transfersByAddress[transferAddress]!!
    val continuation = transfer.claimFromCallback()

    val buffer: MemoryAddress
    val status: Int
    val actualLength: Int

    transfer.native.let {
        buffer = it.buffer
        status = it.status
        actualLength = it.actual_length
        it.buffer = NULL
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

private fun outTransferCallback(transferAddress: MemoryAddress) {
    val transfer = transfersByAddress[transferAddress]!!
    val continuation = transfer.claimFromCallback()

    val buffer: MemoryAddress
    val status: Int
    val actualLength: Int

    transfer.native.let {
        buffer = it.buffer
        status = it.status
        actualLength = it.actual_length
        it.buffer = NULL
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

private fun zeroLengthTransferCallback(transferAddress: MemoryAddress) {
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
