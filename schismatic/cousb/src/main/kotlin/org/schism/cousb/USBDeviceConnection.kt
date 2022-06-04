package org.schism.cousb

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.schism.bytes.HeapSegmentAllocator
import org.schism.bytes.copyFrom
import org.schism.bytes.newConfinedMemorySession
import org.schism.cousb.Libusb.Transfer
import org.schism.cousb.Libusb.TransferFlags
import org.schism.cousb.Libusb.TransferStatus
import org.schism.cousb.Libusb.TransferType
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker
import java.lang.foreign.MemoryAddress
import java.lang.foreign.MemorySegment
import java.lang.foreign.MemorySession
import java.lang.foreign.SegmentAllocator
import java.lang.foreign.ValueLayout.ADDRESS
import java.lang.foreign.ValueLayout.JAVA_LONG
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType.methodType
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.Continuation

public class USBDeviceConnection @PublishedApi internal constructor(public val device: USBDevice) {
    private var handle: MemoryAddress? =
        newConfinedMemorySession().use { memorySession ->
            val connectionHandleStorage = memorySession.allocate(ADDRESS)
            Libusb.checkReturn(Libusb.open(device.handle, connectionHandleStorage) as Int)
            connectionHandleStorage[ADDRESS, 0]
        }

    private val mutex = Mutex()

    public suspend fun claimInterface(iface: USBInterface) {
        require(iface.device === device)

        mutex.withLock {
            val handle = checkNotNull(handle) { "Connection is closed" }
            Libusb.checkReturn(Libusb.claimInterface(handle, iface.number.toInt()) as Int)
        }
    }

    public suspend fun releaseInterface(iface: USBInterface) {
        require(iface.device === device)

        mutex.withLock {
            val handle = checkNotNull(handle) { "Connection is closed" }
            withContext(Dispatchers.IO) {
                Libusb.checkReturn(Libusb.releaseInterface(handle, iface.number.toInt()) as Int)
            }
        }
    }

    public suspend fun sendPacket(endpoint: USBBulkTransferOutEndpoint, data: MemorySegment) {
        require(endpoint.device === device)
        require(data.byteSize() <= endpoint.maxPacketSize.toLong())

        val libusbTransfer = Libusb.allocTransfer(0) as MemoryAddress

        if (libusbTransfer == MemoryAddress.NULL) {
            throw OutOfMemoryError("Failed to allocate transfer")
        }

        Transfer.FLAGS.set(TransferFlags.FREE_TRANSFER or TransferFlags.FREE_BUFFER or TransferFlags.SHORT_NOT_OK)
        Transfer.ENDPOINT.set(libusbTransfer, endpoint.address.toByte())
        Transfer.TYPE.set(libusbTransfer, TransferType.BULK)
        Transfer.TIMEOUT.set(libusbTransfer, 0)
        Transfer.LENGTH.set(libusbTransfer, data.byteSize().toInt())
        Transfer.CALLBACK.set(libusbTransfer, OUT_TRANSFER_CALLBACK)

        if (data.byteSize() != 0L) {
            val buffer = malloc(data.byteSize()) as MemoryAddress

            if (buffer == MemoryAddress.NULL) {
                Libusb.freeTransfer(libusbTransfer)
                throw OutOfMemoryError("Failed to allocate buffer")
            }

            buffer.copyFrom(data)
            Transfer.BUFFER.set(libusbTransfer, buffer)
        } else {
            Transfer.BUFFER.set(libusbTransfer, MemoryAddress.NULL)
        }

        mutex.lock()

        return suspendCancellableCoroutine { continuation ->
            try {
                val handle = handle ?: kotlin.run {
                    Libusb.freeTransfer(libusbTransfer)
                    throw IllegalStateException("Connection is closed")
                }

                Transfer.DEV_HANDLE.set(libusbTransfer, handle)

                val transfer = OutTransfer(continuation)
                outTransfers[libusbTransfer] = transfer

                when (val returnCode = Libusb.submitTransfer(libusbTransfer) as Int) {
                    0 -> continuation.invokeOnCancellation {
                        synchronized(transfer) {
                            if (transfer.continuation != null) {
                                Libusb.cancelTransfer(transfer)
                            }
                        }
                    }

                    else -> {
                        outTransfers.remove(libusbTransfer)
                        Libusb.freeTransfer(libusbTransfer)
                        throw LibusbErrorException(returnCode)
                    }
                }
            } finally {
                mutex.unlock()
            }
        }
    }

    public suspend fun receivePacket(
        endpoint: USBBulkTransferInEndpoint,
        allocator: SegmentAllocator = HeapSegmentAllocator,
    ): MemorySegment {
        require(endpoint.device === device)

        val libusbTransfer = Libusb.allocTransfer(0) as MemoryAddress

        if (libusbTransfer == MemoryAddress.NULL) {
            throw OutOfMemoryError("Failed to allocate transfer")
        }

        val buffer = malloc(endpoint.maxPacketSize.toLong()) as MemoryAddress

        if (buffer == MemoryAddress.NULL) {
            Libusb.freeTransfer(libusbTransfer)
            throw OutOfMemoryError("Failed to allocate packet buffer")
        }

        Transfer.FLAGS.set(TransferFlags.FREE_TRANSFER or TransferFlags.FREE_BUFFER)
        Transfer.ENDPOINT.set(libusbTransfer, endpoint.address.toByte())
        Transfer.TYPE.set(libusbTransfer, TransferType.BULK)
        Transfer.TIMEOUT.set(libusbTransfer, 0)
        Transfer.LENGTH.set(libusbTransfer, endpoint.maxPacketSize.toInt())
        Transfer.CALLBACK.set(libusbTransfer, IN_TRANSFER_CALLBACK)
        Transfer.BUFFER.set(libusbTransfer, buffer)

        mutex.lock()

        return suspendCancellableCoroutine { continuation ->
            try {
                val handle = handle ?: kotlin.run {
                    Libusb.freeTransfer(libusbTransfer)
                    throw IllegalStateException("Connection is closed")
                }

                Transfer.DEV_HANDLE.set(libusbTransfer, handle)

                val transfer = InTransfer(continuation, allocator)
                inTransfers[libusbTransfer] = transfer

                when (val returnCode = Libusb.submitTransfer(libusbTransfer) as Int) {
                    0 -> continuation.invokeOnCancellation {
                        synchronized(transfer) {
                            if (transfer.continuation != null) {
                                Libusb.cancelTransfer(transfer)
                            }
                        }
                    }

                    else -> {
                        outTransfers.remove(libusbTransfer)
                        Libusb.freeTransfer(libusbTransfer)
                        throw LibusbErrorException(returnCode)
                    }
                }
            } finally {
                mutex.unlock()
            }
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

    private class OutTransfer(continuation: Continuation<Unit>) {
        var continuation: Continuation<Unit>? = continuation
    }

    private class InTransfer(continuation: Continuation<MemorySegment>, val allocator: SegmentAllocator) {
        var continuation: Continuation<MemorySegment>? = continuation
    }

    public companion object {
        private val outTransfers = ConcurrentHashMap<MemoryAddress, OutTransfer>()
        private val inTransfers = ConcurrentHashMap<MemoryAddress, InTransfer>()

        private fun checkTransferStatus(libusbTransfer: MemoryAddress) {
            when (val status = Transfer.STATUS[libusbTransfer] as Int) {
                TransferStatus.COMPLETED -> Unit
                TransferStatus.ERROR -> throw USBTransferException("Transfer error")
                TransferStatus.TIMED_OUT -> throw USBTransferException("Transfer timed out")
                TransferStatus.CANCELED -> throw CancellationException("Transfer cancelled")
                TransferStatus.STALL -> throw USBTransferException("Transfer stalled")
                TransferStatus.NO_DEVICE -> throw USBTransferException("Device is no longer attached")
                TransferStatus.OVERFLOW -> throw USBTransferException("Transfer overflowed")
                else -> throw USBTransferException("Unknown transfer error (status code $status)")
            }
        }

        @[Suppress("UNUSED") JvmStatic]
        private fun outTransferCallback(libusbTransfer: MemoryAddress) {
            val transfer = checkNotNull(outTransfers.remove(libusbTransfer))

            val continuation = synchronized(transfer) {
                (transfer.continuation ?: return).also { transfer.continuation = null }
            }

            continuation.resumeWith(kotlin.runCatching {
                checkTransferStatus(libusbTransfer)
            })
        }

        @[Suppress("UNUSED") JvmStatic]
        private fun inTransferCallback(libusbTransfer: MemoryAddress) {
            val transfer = checkNotNull(inTransfers.remove(libusbTransfer))

            val continuation = synchronized(transfer) {
                (transfer.continuation ?: return).also { transfer.continuation = null }
            }

            continuation.resumeWith(runCatching {
                checkTransferStatus(libusbTransfer)
                val buffer = Transfer.BUFFER[libusbTransfer] as MemoryAddress
                val actualLength = Transfer.ACTUAL_LENGTH[libusbTransfer] as Int
                val segment = transfer.allocator.allocate(actualLength.toLong())
                segment.copyFrom(MemorySegment.ofAddress(buffer, actualLength.toLong(), MemorySession.global()))
                segment
            })
        }

        private val OUT_TRANSFER_CALLBACK: MemorySegment
        private val IN_TRANSFER_CALLBACK: MemorySegment
        private val malloc: MethodHandle

        init {
            val linker = Linker.nativeLinker()

            OUT_TRANSFER_CALLBACK = linker.upcallStub(
                MethodHandles.lookup().findStatic(
                    USBDeviceConnection::class.java, "outTransferCallback",
                    methodType(Void.TYPE, MemoryAddress::class.java),
                ),
                FunctionDescriptor.ofVoid(ADDRESS),
                MemorySession.global(),
            )

            IN_TRANSFER_CALLBACK = linker.upcallStub(
                MethodHandles.lookup().findStatic(
                    USBDeviceConnection::class.java, "inTransferCallback",
                    methodType(Void.TYPE, MemoryAddress::class.java),
                ),
                FunctionDescriptor.ofVoid(ADDRESS),
                MemorySession.global(),
            )

            malloc = linker.downcallHandle(
                linker.defaultLookup().lookup("malloc").orElseThrow(),
                FunctionDescriptor.of(ADDRESS, JAVA_LONG), // FIXME: handle 32-bit size_t
            )
        }
    }
}
