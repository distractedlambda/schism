package org.schism.usb

import org.schism.bytes.globalMemorySession
import org.schism.bytes.newConfinedMemorySession
import org.schism.usb.Libusb.Transfer
import org.schism.usb.Libusb.TransferStatus
import org.schism.usb.Libusb.TransferType
import java.io.IOException
import java.lang.Thread.currentThread
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker
import java.lang.foreign.MemoryAddress
import java.lang.foreign.MemorySegment
import java.lang.foreign.MemorySession
import java.lang.foreign.ValueLayout.ADDRESS
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType.methodType
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.LockSupport
import java.util.concurrent.locks.LockSupport.park
import java.util.concurrent.locks.LockSupport.unpark
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

public class UsbDeviceConnection @PublishedApi internal constructor(public val device: UsbDevice) : AutoCloseable {
    private var handle: MemoryAddress? =
        newConfinedMemorySession().use { memorySession ->
            val connectionHandleStorage = memorySession.allocate(ADDRESS)
            Libusb.checkReturn(Libusb.open(device.handle, connectionHandleStorage) as Int)
            connectionHandleStorage[ADDRESS, 0]
        }

    private val closeLock = ReentrantReadWriteLock(true)

    public fun claimInterface(iface: UsbInterface) {
        require(iface.device === device)

        closeLock.read {
            val handle = checkNotNull(handle) { "Connection is closed" }
            Libusb.checkReturn(Libusb.claimInterface(handle, iface.number.toInt()) as Int)
        }
    }

    public fun releaseInterface(iface: UsbInterface) {
        require(iface.device === device)

        closeLock.read {
            val handle = checkNotNull(handle) { "Connection is closed" }
            Libusb.checkReturn(Libusb.releaseInterface(handle, iface.number.toInt()) as Int)
        }
    }

    private fun transfer(endpointAddress: UByte, buffer: MemorySegment): Long {
        closeLock.read {
            val handle = checkNotNull(handle) { "Connection is closed" }

            val libusbTransfer = Libusb.allocTransfer(0) as MemoryAddress

            if (libusbTransfer == MemoryAddress.NULL) {
                throw OutOfMemoryError("Failed to allocate transfer")
            }

            try {
                Transfer.DEV_HANDLE.set(libusbTransfer, handle)
                Transfer.ENDPOINT.set(libusbTransfer, endpointAddress.toByte())
                Transfer.TYPE.set(libusbTransfer, TransferType.BULK)
                Transfer.TIMEOUT.set(libusbTransfer, 0)
                Transfer.LENGTH.set(libusbTransfer, minOf(buffer.byteSize(), Int.MAX_VALUE.toLong()).toInt())
                Transfer.CALLBACK.set(libusbTransfer, TRANSFER_CALLBACK)
                Transfer.BUFFER.set(libusbTransfer, buffer.address())

                buffer.session().whileAlive {
                    val transfer = InFlightTransfer()
                    inFlightTransfers[libusbTransfer.toRawLongValue()] = transfer

                    when (val returnCode = Libusb.submitTransfer(libusbTransfer) as Int) {
                        0 -> Unit
                        else -> {
                            inFlightTransfers.remove(libusbTransfer.toRawLongValue())
                            throw LibusbErrorException(returnCode)
                        }
                    }

                    var interrupted = false

                    while (transfer.thread != null) {
                        park()
                        if (Thread.interrupted()) {
                            if (!interrupted) {
                                interrupted = true
                                Libusb.cancelTransfer(libusbTransfer)
                            }
                        }
                    }

                    if (interrupted) {
                        throw InterruptedException()
                    }

                    when (val status = Transfer.STATUS[libusbTransfer] as Int) {
                        TransferStatus.COMPLETED -> Unit
                        TransferStatus.ERROR -> throw UsbTransferException("Transfer error")
                        TransferStatus.TIMED_OUT -> throw UsbTransferException("Transfer timed out")
                        TransferStatus.CANCELED -> throw UsbTransferException("Transfer cancelled")
                        TransferStatus.STALL -> throw UsbTransferException("Transfer stalled")
                        TransferStatus.NO_DEVICE -> throw UsbTransferException("Device is no longer attached")
                        TransferStatus.OVERFLOW -> throw UsbTransferException("Transfer overflowed")
                        else -> throw UsbTransferException("Unknown transfer error (status code $status)")
                    }
                }

                return (Transfer.ACTUAL_LENGTH[libusbTransfer] as Int).toLong()
            } finally {
                Libusb.freeTransfer(libusbTransfer)
            }
        }
    }

    public fun send(endpoint: UsbBulkTransferOutEndpoint, data: MemorySegment): Long {
        require(endpoint.device === device)
        require(data.isNative)
        return transfer(endpoint.address, data)
    }

    public fun sendExact(endpoint: UsbBulkTransferOutEndpoint, data: MemorySegment) {
        if (send(endpoint, data) != data.byteSize()) {
            throw IOException("Incomplete USB send")
        }
    }

    public fun sendZeroLength(endpoint: UsbBulkTransferOutEndpoint) {
        require(endpoint.device === device)
        transfer(endpoint.address, emptySegment)
    }

    public fun receive(endpoint: UsbBulkTransferInEndpoint, destination: MemorySegment): Long {
        require(endpoint.device === device)
        require(destination.isNative)
        require(!destination.isReadOnly)
        return transfer(endpoint.address, destination)
    }

    public fun receiveExact(endpoint: UsbBulkTransferInEndpoint, destination: MemorySegment) {
        if (receive(endpoint, destination) != destination.byteSize()) {
            throw IOException("Incomplete USB receive")
        }
    }

    public fun receiveZeroLength(endpoint: UsbBulkTransferInEndpoint) {
        require(endpoint.device === device)
        transfer(endpoint.address, emptySegment)
    }

    override fun close() {
        closeLock.write {
            val handle = (handle ?: return).also { handle = null }
            Libusb.close(handle)
        }
    }

    private class InFlightTransfer {
        @Volatile var thread: Thread? = currentThread()
    }

    public companion object {
        private val inFlightTransfers = ConcurrentHashMap<Long, InFlightTransfer>()

        private val emptySegment = globalMemorySession().allocate(0)

        @[Suppress("UNUSED") JvmStatic]
        private fun transferCallback(libusbTransfer: MemoryAddress) {
            val transfer = checkNotNull(inFlightTransfers.remove(libusbTransfer.toRawLongValue()))
            val thread = checkNotNull(transfer.thread)
            transfer.thread = null
            unpark(thread)
        }

        private val TRANSFER_CALLBACK: MemorySegment = Linker.nativeLinker().upcallStub(
            MethodHandles.lookup().findStatic(
                UsbDeviceConnection::class.java, "transferCallback",
                methodType(Void.TYPE, MemoryAddress::class.java),
            ),
            FunctionDescriptor.ofVoid(ADDRESS),
            MemorySession.global(),
        )

        init {
            // FIXME does this actually force LockSupport to be loaded?
            LockSupport::class.java
        }
    }
}
