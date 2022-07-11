package org.schism.usb

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import org.schism.coroutines.SharedLifetime
import org.schism.coroutines.SuspendingAutocloseable
import org.schism.ffi.CInt
import org.schism.ffi.NativeEntrypoint
import org.schism.ffi.nativeEntrypoint
import org.schism.ffi.wrap
import org.schism.memory.Memory
import org.schism.memory.MemoryEncoder
import org.schism.memory.NativeAddress
import org.schism.memory.free
import org.schism.memory.isNULL
import org.schism.memory.malloc
import org.schism.memory.nativeMemory
import org.schism.memory.positionalDifference
import org.schism.usb.Libusb.Companion.allocTransfer
import org.schism.usb.Libusb.Companion.cancelTransfer
import org.schism.usb.Libusb.Companion.errorMessage
import org.schism.usb.Libusb.Companion.submitTransfer
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
        callback: NativeEntrypoint,
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
                    val nativeAddress = allocTransfer(0)

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
                    when (val returnCode = submitTransfer(transfer.nativeAddress)) {
                        0 -> Unit
                        else -> {
                            submitted = false
                            throw UsbException(errorMessage(returnCode))
                        }
                    }

                    continuation.invokeOnCancellation {
                        // FIXME: make lock-free?
                        synchronized(transfer) {
                            // FIXME: replace this check with something less fragile
                            if (transfer.continuation !== continuation) return@invokeOnCancellation
                            cancelTransfer(transfer.nativeAddress)
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
        callback: NativeEntrypoint,
        type: UByte,
        buffer: NativeAddress,
        bufferLength: Int,
    ): Int {
        return suspendCancellableCoroutine { continuation ->
            transfer(continuation, callback, endpointAddress, type, buffer, bufferLength)
        }
    }

    @OptIn(ExperimentalContracts::class)
    public suspend fun sendPacket(endpoint: UsbBulkTransferOutEndpoint, encode: suspend MemoryEncoder.() -> Unit) {
        contract {
            callsInPlace(encode, InvocationKind.EXACTLY_ONCE)
        }

        require(endpoint.device === device)

        val buffer = malloc(endpoint.maxPacketSize.toLong())

        val length = try {
            nativeMemory(buffer, endpoint.maxPacketSize.toLong()).encoder().positionalDifference {
                encode()
            }
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
            return process(nativeMemory(buffer, receivedLength.toLong()))
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
            Libusb.close(nativeHandle)
        }
    }

    private class Transfer(val nativeAddress: NativeAddress) {
        var continuation: CancellableContinuation<Int>? = null
        var deviceConnectionLifetime: SharedLifetime? = null

        val native: Libusb.Transfer get() {
            return Libusb.Transfer.wrap(nativeAddress)
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

    private companion object {
        private val transfersByAddress = ConcurrentHashMap<NativeAddress, Transfer>()
        private val freeTransfers = ConcurrentLinkedQueue<Transfer>()
        private val nativeInTransferCallback = nativeEntrypoint(::inTransferCallback)
        private val nativeOutTransferCallback = nativeEntrypoint(::outTransferCallback)
        private val nativeZeroLengthTransferCallback = nativeEntrypoint(::zeroLengthTransferCallback)

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

        @JvmStatic private fun inTransferCallback(transferAddress: NativeAddress) {
            val transfer = transfersByAddress[transferAddress]!!
            val continuation = transfer.claimFromCallback()

            val buffer: NativeAddress
            val status: CInt
            val actualLength: CInt

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

        @JvmStatic private fun outTransferCallback(transferAddress: NativeAddress) {
            val transfer = transfersByAddress[transferAddress]!!
            val continuation = transfer.claimFromCallback()

            val buffer: NativeAddress
            val status: CInt
            val actualLength: CInt

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

        @JvmStatic private fun zeroLengthTransferCallback(transferAddress: NativeAddress) {
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
    }
}
