package org.schism.usb

import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.suspendCancellableCoroutine
import org.schism.coroutines.SharedLifetime
import org.schism.coroutines.SuspendingAutocloseable
import org.schism.ffi.nativeEntrypoint
import org.schism.ffi.wrap
import org.schism.math.toIntExact
import org.schism.memory.Memory
import org.schism.memory.NativeAddress
import org.schism.memory.isNULL
import org.schism.usb.Libusb.Companion.allocTransfer
import org.schism.usb.Libusb.Companion.cancelTransfer
import org.schism.usb.Libusb.Companion.errorMessage
import org.schism.usb.Libusb.Companion.submitTransfer
import org.schism.usb.Libusb.TransferStatus
import java.lang.ref.Reference.reachabilityFence
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

public class UsbDeviceConnection internal constructor(
    public val device: UsbDevice,
    private val nativeHandle: NativeAddress,
) : SuspendingAutocloseable {
    private val lifetime = SharedLifetime()

    private suspend fun transfer(endpointAddress: UByte, type: UByte, memory: Memory): Int {
        val job = currentCoroutineContext()[Job]

        job?.ensureActive()

        lifetime.withRetained {
            val transfer = allocateTransfer()

            try {
                Libusb.Transfer.wrap(transfer.native).let {
                    it.dev_handle = nativeHandle
                    it.endpoint = endpointAddress
                    it.type = type
                    it.length = memory.size.toIntExact()
                    it.buffer = memory.startAddress
                }

                try {
                    suspendCoroutine<Unit> { continuation ->
                        transfer.continuation = continuation
                        inFlightTransfers[transfer.native] = transfer

                        when (val returnCode = submitTransfer(transfer.native)) {
                            0 -> Unit
                            else -> {
                                continuation.resumeWithException(UsbException(errorMessage(returnCode)))
                                return@suspendCoroutine
                            }
                        }

                        if (job != null) {
                            @OptIn(InternalCoroutinesApi::class)
                            val cancellationHandle = job.invokeOnCompletion(onCancelling = true) { cause ->
                                if (cause == null) {
                                    return@invokeOnCompletion
                                }

                                synchronized(transfer) {
                                    // FIXME: is an identity check reliable, i.e. are continuations guaranteed unique?
                                    if (transfer.continuation !== continuation) {
                                        return@invokeOnCompletion
                                    }

                                    cancelTransfer(transfer.native)
                                }
                            }

                            synchronized(transfer) {
                                if (transfer.continuation === continuation) {
                                    transfer.cancellationHandle = cancellationHandle
                                } else {
                                    cancellationHandle.dispose()
                                }
                            }
                        }
                    }
                } finally {
                    reachabilityFence(memory)
                }

                job?.ensureActive()

                val errorMessage = when (Libusb.Transfer.wrap(transfer.native).status) {
                    TransferStatus.COMPLETED -> null
                    TransferStatus.ERROR -> "Transfer error"
                    TransferStatus.TIMED_OUT -> "Transfer timed out"
                    TransferStatus.CANCELED -> "Transfer cancelled"
                    TransferStatus.STALL -> "Transfer stalled"
                    TransferStatus.NO_DEVICE -> "Device is no longer attached"
                    TransferStatus.OVERFLOW -> "Transfer overflowed"
                    else -> "Unknown transfer error"
                }

                errorMessage?.let { throw UsbException(it) }

                return Libusb.Transfer.wrap(transfer.native).actual_length
            } finally {
                transfer.continuation = null

                transfer.cancellationHandle?.let {
                    transfer.cancellationHandle = null
                    it.dispose()
                }

                inFlightTransfers.remove(transfer.native)
                idleTransfers.add(transfer)
            }
        }
    }

    override suspend fun close() {
        if (lifetime.end()) {
            Libusb.close(nativeHandle)
        }
    }

    private class Transfer(
        val native: NativeAddress,
        var continuation: Continuation<Unit>? = null,
        var cancellationHandle: DisposableHandle? = null,
    )

    private companion object {
        private val inFlightTransfers = ConcurrentHashMap<NativeAddress, Transfer>()
        private val idleTransfers = ConcurrentLinkedQueue<Transfer>()

        private fun allocateTransfer(): Transfer {
            idleTransfers.poll()?.let { return it }

            val nativeTransfer = allocTransfer(0)

            if (nativeTransfer.isNULL()) {
                throw OutOfMemoryError("Failed to allocate libusb transfer")
            }

            Libusb.Transfer.wrap(nativeTransfer).callback = NATIVE_TRANSFER_CALLBACK

            return Transfer(nativeTransfer)
        }

        private val NATIVE_TRANSFER_CALLBACK = nativeEntrypoint(::transferCallback)

        @JvmStatic private fun transferCallback(nativeTransfer: NativeAddress) {
            val transfer = checkNotNull(inFlightTransfers[nativeTransfer])

            val continuation = synchronized(transfer) {
                transfer.continuation!!.also { transfer.continuation = null }
            }

            continuation.resume(Unit)
        }
    }
}
