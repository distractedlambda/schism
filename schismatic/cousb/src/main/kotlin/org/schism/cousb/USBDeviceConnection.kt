package org.schism.cousb

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.schism.cousb.Libusb.Transfer
import org.schism.cousb.Libusb.TransferStatus
import org.schism.cousb.Libusb.TransferType
import org.schism.cousb.Libusb.checkReturnCode
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker
import java.lang.foreign.MemoryAddress
import java.lang.foreign.MemorySegment
import java.lang.foreign.MemorySession
import java.lang.foreign.ValueLayout.ADDRESS
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.lang.ref.Reference.reachabilityFence
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

public class USBDeviceConnection @PublishedApi internal constructor(public val device: USBDevice) {
    private var handle: MemoryAddress? =
        MemorySession.openConfined().use { memorySession ->
            val connectionHandleStorage = memorySession.allocate(ADDRESS)
            checkReturnCode(Libusb.open(device.handle, connectionHandleStorage) as Int)
            connectionHandleStorage[ADDRESS, 0]
        }

    private val mutex = Mutex()

    public suspend fun claimInterface(iface: USBInterface) {
        require(iface.device === device)

        mutex.withLock {
            val handle = checkNotNull(handle) { "Connection is closed" }
            checkReturnCode(Libusb.claimInterface(handle, iface.number.toInt()) as Int)
        }
    }

    public suspend fun releaseInterface(iface: USBInterface) {
        require(iface.device === device)

        mutex.withLock {
            val handle = checkNotNull(handle) { "Connection is closed" }
            withContext(Dispatchers.IO) {
                checkReturnCode(Libusb.releaseInterface(handle, iface.number.toInt()) as Int)
            }
        }
    }

    private suspend fun submitAndSuspendOnTransfer(memorySession: MemorySession, transfer: MemoryAddress) {
        mutex.lock()

        suspendCancellableCoroutine<Unit> { continuation ->
            // FIXME: can we safely throw exceptions out of here?
            try {
                Transfer.DEV_HANDLE.set(transfer, checkNotNull(handle))

                transfers[transfer] = continuation to memorySession

                when (val returnCode = Libusb.submitTransfer(transfer) as Int) {
                    0 -> continuation.invokeOnCancellation {
                        try {
                            Libusb.cancelTransfer(transfer)
                        } finally {
                            reachabilityFence(memorySession)
                        }
                    }

                    else -> {
                        transfers.remove(transfer)
                        throw LibusbErrorException(returnCode)
                    }
                }
            } finally {
                reachabilityFence(memorySession)
                mutex.unlock()
            }
        }
    }

    public suspend fun transfer(endpoint: USBBulkTransferOutEndpoint, data: MemorySegment): Int {
        require(endpoint.device === device)

        val transfer = Libusb.allocTransfer(0) as MemoryAddress

        if (transfer == MemoryAddress.NULL) {
            throw OutOfMemoryError("Failed to allocate transfer")
        }

        val memorySession = MemorySession.openImplicit()
        memorySession.addCloseAction { Libusb.freeTransfer(transfer) }

        try {
            Transfer.ENDPOINT.set(transfer, endpoint.address.toByte())
            Transfer.TYPE.set(transfer, TransferType.BULK)
            Transfer.TIMEOUT.set(transfer, 0)
            Transfer.LENGTH.set(transfer, Math.toIntExact(data.byteSize()))
            Transfer.CALLBACK.set(transfer, TRANSFER_CALLBACK)

            if (data.byteSize() != 0L) {
                val buffer = MemorySegment.allocateNative(data.byteSize(), memorySession)
                buffer.copyFrom(data)
                Transfer.BUFFER.set(transfer, buffer)
            } else {
                Transfer.BUFFER.set(transfer, MemoryAddress.NULL)
            }
        } finally {
            reachabilityFence(memorySession)
        }

        submitAndSuspendOnTransfer(memorySession, transfer)

        try {
            checkTransferStatus(transfer)
            return Transfer.ACTUAL_LENGTH[transfer] as Int
        } finally {
            reachabilityFence(memorySession)
        }
    }

    public suspend fun transfer(endpoint: USBBulkTransferInEndpoint): ByteArray {
        require(endpoint.device === device)

        val transfer = Libusb.allocTransfer(0) as MemoryAddress

        if (transfer == MemoryAddress.NULL) {
            throw OutOfMemoryError("Failed to allocate transfer")
        }

        val memorySession = MemorySession.openImplicit()
        memorySession.addCloseAction { Libusb.freeTransfer(transfer) }


        val buffer = if (bufferSize != 0L) {
            MemorySegment.allocateNative(bufferSize, memorySession)
        } else {
            NULL_SEGMENT
        }

        try {
            Transfer.ENDPOINT.set(transfer, endpoint.address.toByte())
            Transfer.TYPE.set(transfer, TransferType.BULK)
            Transfer.TIMEOUT.set(transfer, 0)
            Transfer.LENGTH.set(transfer, Math.toIntExact(buffer.byteSize()))
            Transfer.CALLBACK.set(transfer, TRANSFER_CALLBACK)
            Transfer.BUFFER.set(transfer, buffer)
        } finally {
            reachabilityFence(memorySession)
        }

        submitAndSuspendOnTransfer(memorySession, transfer)

        try {
            checkTransferStatus(transfer)
        } finally {
            reachabilityFence(memorySession)
        }
    }

    @PublishedApi internal suspend fun close() {
        withContext(NonCancellable) {
            mutex.withLock {
                Libusb.close(handle!!)
                handle = null
            }
        }
    }

    public companion object {
        private val transfers = ConcurrentHashMap<MemoryAddress, Pair<Continuation<Unit>, MemorySession>>()

        private val NULL_SEGMENT = MemorySegment.ofAddress(MemoryAddress.NULL, 0, MemorySession.global())

        @[Suppress("UNUSED") JvmStatic]
        private fun transferCallback(transfer: MemoryAddress) {
            val (continuation, memorySession) = checkNotNull(transfers.remove(transfer))
            try {
                continuation.resume(Unit)
            } finally {
                reachabilityFence(memorySession)
            }
        }

        private fun checkTransferStatus(transfer: MemoryAddress) {
            when (Transfer.STATUS[transfer] as Int) {
                TransferStatus.COMPLETED -> Unit
                TransferStatus.ERROR -> throw USBTransferException("Transfer error")
                TransferStatus.TIMED_OUT -> throw USBTransferException("Transfer timed out")
                TransferStatus.CANCELED -> throw CancellationException("Transfer cancelled")
                TransferStatus.STALL -> throw USBTransferException("Transfer stalled")
                TransferStatus.NO_DEVICE -> throw USBTransferException("Device is no longer attached")
                TransferStatus.OVERFLOW -> throw USBTransferException("Transfer overflowed")
                else -> throw USBTransferException("Unknown transfer error")
            }
        }

        private val TRANSFER_CALLBACK: MemorySegment =
            Linker.nativeLinker().upcallStub(
                MethodHandles.lookup().findStatic(
                    USBDeviceConnection::class.java, "transferCallback",
                    MethodType.methodType(Void.TYPE, MemoryAddress::class.java),
                ),
                FunctionDescriptor.ofVoid(ADDRESS),
                MemorySession.global(),
            )
    }
}
