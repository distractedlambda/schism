package org.schism.usb

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import org.schism.foreign.NativeAddress
import org.schism.foreign.NativeBuffer
import org.schism.foreign.NativeLayout
import org.schism.foreign.NativeMember
import org.schism.foreign.calculateCStructLayout
import org.schism.foreign.index
import org.schism.foreign.nativeAddress
import org.schism.util.SharedLifetime
import java.lang.Math.toIntExact
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker.nativeLinker
import java.lang.foreign.MemoryAddress
import java.lang.foreign.MemoryLayout
import java.lang.foreign.MemorySession
import java.lang.foreign.SymbolLookup.libraryLookup
import java.lang.foreign.ValueLayout
import java.lang.foreign.ValueLayout.ADDRESS
import java.lang.foreign.ValueLayout.JAVA_BYTE
import java.lang.foreign.ValueLayout.JAVA_INT
import java.lang.foreign.ValueLayout.JAVA_LONG
import java.lang.foreign.ValueLayout.JAVA_SHORT
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType.methodType
import java.lang.ref.Cleaner
import java.nio.ByteOrder.LITTLE_ENDIAN
import java.util.concurrent.ConcurrentHashMap
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success
import kotlin.concurrent.thread
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.io.path.Path

object Libusb : UsbBackend {
    override val attachedDevices = MutableStateFlow<List<UsbDevice>>(emptyList())

    private val handle = NativeBuffer.withUnmanaged(ADDRESS) { handleBuffer ->
        checkReturn(nativeInit.invokeExact(handleBuffer.start.toMemoryAddress()) as Int)
        handleBuffer[ADDRESS]
    }

    private val deviceCleaner = Cleaner.create(
        Thread.ofPlatform()
            .name("Libusb.Device cleaner")
            .factory()
    )

    init {
        thread(isDaemon = true, name = "libusb event handler") {
            while (true) {
                checkReturn(nativeHandleEvents.invokeExact(handle) as Int)
            }
        }

        thread(isDaemon = true, name = "libusb device enumerator") {
            var devicesByHandle = hashMapOf<NativeAddress, Result<Device>>()

            NativeBuffer.withUnmanaged(ADDRESS) { listBuffer ->
                while (true) {
                    val listSize = checkSize(
                        nativeGetDeviceList.invokeExact(
                            handle.toMemoryAddress(),
                            listBuffer.start.toMemoryAddress()
                        ) as Long
                    )

                    val list = NativeBuffer.unmanaged(listBuffer[ADDRESS], listSize)
                    val newDevicesByHandle = hashMapOf<NativeAddress, Result<Device>>()

                    try {
                        for (i in 0 until listSize) {
                            val deviceHandle = list[ADDRESS, i.index]
                            newDevicesByHandle[deviceHandle] = devicesByHandle[deviceHandle]
                                ?: try {
                                    success(Device(deviceHandle))
                                } catch (exception: UsbException) {
                                    failure(exception)
                                }
                        }
                    } finally {
                        nativeFreeDeviceList.invokeExact(list.start.toMemoryAddress(), 1)
                    }

                    devicesByHandle = newDevicesByHandle

                    attachedDevices.value = devicesByHandle
                        .values
                        .mapNotNull { it.getOrNull() }

                    Thread.sleep(500)
                }
            }
        }
    }

    private class DeviceConnection(override val device: Device) : UsbDeviceConnection {
        private val handle = NativeBuffer.withUnmanaged(ADDRESS) { handleBuffer ->
            checkReturn(
                nativeOpen.invokeExact(
                    device.handle.toMemoryAddress(),
                    handleBuffer.start.toMemoryAddress(),
                ) as Int
            )

            handleBuffer[ADDRESS]
        }

        private val lifetime = SharedLifetime()

        fun claimInterface(`interface`: UsbInterface) {
            require(`interface` is Interface)
            require(`interface`.associatedDevice === device)

            lifetime.withRetained {
                checkReturn(
                    nativeClaimInterface.invokeExact(
                        handle.toMemoryAddress(),
                        `interface`.number.toInt()
                    ) as Int
                )
            }
        }

        suspend fun releaseInterface(`interface`: UsbInterface) {
            require(`interface` is Interface)
            require(`interface`.associatedDevice === device)

            lifetime.withRetained {
                withContext(NonCancellable) {
                    withContext(Dispatchers.IO) {
                        checkReturn(nativeReleaseInterface.invokeExact(handle, `interface`.number.toInt()) as Int)
                    }
                }
            }
        }

        suspend fun transfer(endpointAddress: UByte, buffer: NativeBuffer): Long {
            lifetime.withRetained {
                val nativeTransferAddress = (nativeAllocTransfer.invokeExact(0) as MemoryAddress).nativeAddress()

                if (nativeTransferAddress.isNULL()) {
                    throw OutOfMemoryError("Failed to allocate transfer")
                }

                try {
                    val nativeTransfer = NativeBuffer.unmanaged(nativeTransferAddress, NativeTransfer.layout.size)

                    nativeTransfer[NativeTransfer.devHandle] = handle
                    nativeTransfer[NativeTransfer.endpoint] = endpointAddress.toByte()
                    nativeTransfer[NativeTransfer.type] = NativeTransferType.BULK.toByte()
                    nativeTransfer[NativeTransfer.timeout] = 0
                    nativeTransfer[NativeTransfer.length] = minOf(buffer.size, Int.MAX_VALUE.toLong()).toInt()
                    nativeTransfer[NativeTransfer.callback] = nativeTransferCallback
                    nativeTransfer[NativeTransfer.buffer] = buffer.start

                    buffer.keepAlive {
                        suspendCoroutine<Unit> { continuation ->
                            inFlightTransfers[nativeTransferAddress] = continuation

                            when (val returnCode = nativeSubmitTransfer.invokeExact(nativeTransferAddress) as Int) {
                                0 -> Unit
                                else -> {
                                    inFlightTransfers.remove(nativeTransferAddress)
                                    throw UsbException(errorMessage(returnCode))
                                }
                            }

                            @OptIn(InternalCoroutinesApi::class)
                            continuation.context[Job]?.invokeOnCompletion(
                                onCancelling = true,
                                invokeImmediately = true,
                            ) {
                                nativeCancelTransfer.invokeExact(nativeTransferAddress)
                            }
                        }
                    }

                    currentCoroutineContext().ensureActive()

                    when (nativeTransfer[NativeTransfer.status]) {
                        NativeTransferStatus.COMPLETED -> Unit
                        NativeTransferStatus.ERROR -> throw UsbException("Transfer error")
                        NativeTransferStatus.TIMED_OUT -> throw UsbException("Transfer timed out")
                        NativeTransferStatus.CANCELED -> throw UsbException("Transfer cancelled")
                        NativeTransferStatus.STALL -> throw UsbException("Transfer stalled")
                        NativeTransferStatus.NO_DEVICE -> throw UsbException("Device is no longer attached")
                        NativeTransferStatus.OVERFLOW -> throw UsbException("Transfer overflowed")
                        else -> throw UsbException("Unknown transfer error")
                    }

                    return nativeTransfer[NativeTransfer.actualLength].toLong()
                } finally {
                    nativeFreeTransfer.invokeExact(nativeTransferAddress)
                }
            }
        }

        suspend fun send(endpoint: UsbBulkTransferOutEndpoint, source: NativeBuffer): Long {
            require(endpoint is BulkTransferOutEndpoint)
            require(endpoint.associatedDevice === device)
            return transfer(endpoint.address, source)
        }

        suspend fun receive(endpoint: UsbBulkTransferInEndpoint, destination: NativeBuffer): Long {
            require(endpoint is BulkTransferInEndpoint)
            require(endpoint.associatedDevice === device)
            return transfer(endpoint.address, destination)
        }

        override suspend fun close() {
            if (lifetime.end()) {
                nativeClose.invokeExact(handle)
            }
        }

        private companion object {
            private val inFlightTransfers = ConcurrentHashMap<NativeAddress, Continuation<Unit>>()

            @[Suppress("UNUSED") JvmStatic]
            private fun transferCallback(nativeTransfer: MemoryAddress) {
                checkNotNull(inFlightTransfers.remove(nativeTransfer.nativeAddress())).resume(Unit)
            }

            private val nativeTransferCallback = nativeLinker().upcallStub(
                MethodHandles.lookup().findStatic(
                    DeviceConnection::class.java, "transferCallback",
                    methodType(Void.TYPE, MemoryAddress::class.java),
                ),
                FunctionDescriptor.ofVoid(ADDRESS),
                MemorySession.global(),
            ).nativeAddress()
        }
    }

    private class Device(handle: NativeAddress) : UsbDevice {
        init {
            nativeRefDevice.invokeExact(handle)
            deviceCleaner.register(this) { nativeUnrefDevice.invokeExact(handle) }
        }

        val handle = handle

        override val usbVersion: UShort
        override val deviceClass: UByte
        override val deviceSubClass: UByte
        override val deviceProtocol: UByte
        override val vendorId: UShort
        override val productId: UShort
        override val deviceVersion: UShort
        override val configurations: List<Configuration>

        override val backend get() = Libusb

        init {
            NativeBuffer.withUnmanaged(NativeDeviceDescriptor.layout) { descriptor ->
                checkReturn(
                    nativeGetDeviceDescriptor.invokeExact(
                        handle.toMemoryAddress(),
                        descriptor.start.toMemoryAddress(),
                    ) as Int
                )

                usbVersion = descriptor[NativeDeviceDescriptor.bcdUsb].toUShort()
                deviceClass = descriptor[NativeDeviceDescriptor.bDeviceClass].toUByte()
                deviceSubClass = descriptor[NativeDeviceDescriptor.bDeviceSubClass].toUByte()
                deviceProtocol = descriptor[NativeDeviceDescriptor.bDeviceProtocol].toUByte()
                vendorId = descriptor[NativeDeviceDescriptor.idVendor].toUShort()
                productId = descriptor[NativeDeviceDescriptor.idProduct].toUShort()
                deviceVersion = descriptor[NativeDeviceDescriptor.bcdDevice].toUShort()

                val numConfigurations = descriptor[NativeDeviceDescriptor.bNumConfigurations].toUByte()

                NativeBuffer.withUnmanaged(ADDRESS) { configDescriptorAddressBuffer ->
                    configurations = buildList {
                        for (i in 0 until numConfigurations.toInt()) {
                            checkReturn(
                                nativeGetConfigDescriptor.invokeExact(
                                    handle.toMemoryAddress(),
                                    i.toByte(),
                                    configDescriptorAddressBuffer.start.toMemoryAddress(),
                                ) as Int
                            )

                            val configDescriptor = NativeBuffer.unmanaged(
                                configDescriptorAddressBuffer[ADDRESS],
                                NativeConfigDescriptor.layout.size,
                            )

                            try {
                                add(Configuration(this@Device, configDescriptor))
                            } finally {
                                nativeFreeConfigDescriptor.invokeExact(configDescriptor.start.toMemoryAddress())
                            }
                        }
                    }
                }
            }
        }

        override fun connect(): DeviceConnection {
            return DeviceConnection(this)
        }
    }

    private class Configuration(override val device: Device, descriptor: NativeBuffer) : UsbConfiguration {
        override val interfaces: List<UsbInterface> =
            NativeBuffer.unmanagedArrayElements(
                descriptor[NativeConfigDescriptor.`interface`],
                NativeInterface.layout,
                descriptor[NativeConfigDescriptor.bNumInterfaces].toUByte().toLong()
            ).flatMap { nativeInterface ->
                NativeBuffer.unmanagedArrayElements(
                    nativeInterface[NativeInterface.altsetting],
                    NativeInterfaceDescriptor.layout,
                    nativeInterface[NativeInterface.numAltsetting].toLong(),
                )
            }.map { interfaceDescriptor ->
                Interface(this@Configuration, interfaceDescriptor)
            }.toList()
    }

    private class Interface(override val configuration: Configuration, descriptor: NativeBuffer) : UsbInterface {
        val number = descriptor[NativeInterfaceDescriptor.bInterfaceNumber].toUByte()

        val alternateSetting = descriptor[NativeInterfaceDescriptor.bAlternateSetting].toUByte()

        val interfaceClass = descriptor[NativeInterfaceDescriptor.bInterfaceClass].toUByte()

        val interfaceSubClass = descriptor[NativeInterfaceDescriptor.bInterfaceSubClass].toUByte()

        val interfaceProtocol = descriptor[NativeInterfaceDescriptor.bInterfaceProtocol].toUByte()

        val name = descriptor[NativeInterfaceDescriptor.iInterface].toUByte()

        val endpoints: List<UsbEndpoint> =
            NativeBuffer.unmanagedArrayElements(
                descriptor[NativeInterfaceDescriptor.endpoint],
                NativeEndpointDescriptor.layout,
                descriptor[NativeInterfaceDescriptor.bNumEndpoints].toUByte().toLong(),
            ).mapNotNull { endpointDescriptor ->
                val address = endpointDescriptor[NativeEndpointDescriptor.bEndpointAddress].toUByte()
                val attributes = endpointDescriptor[NativeEndpointDescriptor.bmAttributes].toUByte().toInt()
                val maxPacketSize = endpointDescriptor[NativeEndpointDescriptor.wMaxPacketSize].toUShort()
                when (attributes and 0b11) {
                    0 -> ControlEndpoint(this@Interface, address, maxPacketSize)
                    1 -> null
                    2 -> when (address.toUInt() shr 7) {
                        0u -> BulkTransferOutEndpoint(this@Interface, address, maxPacketSize)
                        1u -> BulkTransferOutEndpoint(this@Interface, address, maxPacketSize)
                        else -> throw AssertionError()
                    }
                    3 -> null
                    else -> throw AssertionError()
                }
            }.toList()
    }

    private class AlternateSetting(override val `interface`: Interface) : UsbAlternateSetting {

    }

    private sealed class Endpoint(override val alternateSetting: AlternateSetting) : UsbEndpoint

    private class ControlEndpoint(
        iface: UsbInterface,
        address: UByte,
        maxPacketSize: UShort,
    ) : Endpoint(), UsbControlEndpoint

    private class BulkTransferInEndpoint(
        iface: UsbInterface,
        address: UByte,
        maxPacketSize: UShort,
    ) : Endpoint(), UsbBulkTransferInEndpoint


    private class BulkTransferOutEndpoint(
        iface: UsbInterface,
        address: UByte,
        maxPacketSize: UShort,
    ) : Endpoint(), UsbBulkTransferOutEndpoint

    val nativeAllocTransfer: MethodHandle
    val nativeCancelTransfer: MethodHandle
    val nativeClaimInterface: MethodHandle
    val mhClearHalt: MethodHandle
    val nativeClose: MethodHandle
    val mhExit: MethodHandle
    val nativeFreeConfigDescriptor: MethodHandle
    val nativeFreeDeviceList: MethodHandle
    val nativeFreeTransfer: MethodHandle
    val mhGetActiveConfigDescriptor: MethodHandle
    val mhGetBusNumber: MethodHandle
    val nativeGetConfigDescriptor: MethodHandle
    val mhGetDeviceAddress: MethodHandle
    val nativeGetDeviceDescriptor: MethodHandle
    val nativeGetDeviceList: MethodHandle
    val nativeHandleEvents: MethodHandle
    val nativeInit: MethodHandle
    val mhInterruptEventHandler: MethodHandle
    val nativeOpen: MethodHandle
    val nativeRefDevice: MethodHandle
    val nativeReleaseInterface: MethodHandle
    val mhResetDevice: MethodHandle
    val mhStrerror: MethodHandle
    val nativeSubmitTransfer: MethodHandle
    val nativeUnrefDevice: MethodHandle

    init {
        val lib = libraryLookup(Path("/opt/homebrew/lib/libusb-1.0.dylib"), MemorySession.global())
        val linker = nativeLinker()

        fun link(name: String, vararg argumentLayouts: MemoryLayout, returning: MemoryLayout? = null): MethodHandle {
            val descriptor = if (returning != null) {
                FunctionDescriptor.of(returning, *argumentLayouts)
            } else {
                FunctionDescriptor.ofVoid(*argumentLayouts)
            }

            return linker.downcallHandle(lib.lookup("libusb_$name").orElseThrow(), descriptor)
        }

        nativeAllocTransfer = link("alloc_transfer", JAVA_INT, returning = ADDRESS)
        nativeCancelTransfer = link("cancel_transfer", ADDRESS, returning = JAVA_INT)
        nativeClaimInterface = link("claim_interface", ADDRESS, JAVA_INT, returning = JAVA_INT)
        mhClearHalt = link("clear_halt", ADDRESS, JAVA_BYTE, returning = JAVA_INT)
        nativeClose = link("close", ADDRESS)
        mhExit = link("exit", ADDRESS)
        nativeFreeConfigDescriptor = link("free_config_descriptor", ADDRESS)
        nativeFreeDeviceList = link("free_device_list", ADDRESS, JAVA_INT)
        nativeFreeTransfer = link("free_transfer", ADDRESS)
        mhGetActiveConfigDescriptor = link("get_active_config_descriptor", ADDRESS, ADDRESS, returning = JAVA_INT)
        mhGetBusNumber = link("get_bus_number", ADDRESS, returning = JAVA_BYTE)
        nativeGetConfigDescriptor = link("get_config_descriptor", ADDRESS, JAVA_BYTE, ADDRESS, returning = JAVA_INT)
        mhGetDeviceAddress = link("get_device_address", ADDRESS, returning = JAVA_BYTE)
        nativeGetDeviceDescriptor = link("get_device_descriptor", ADDRESS, ADDRESS, returning = JAVA_INT)
        nativeGetDeviceList = link("get_device_list", ADDRESS, ADDRESS, returning = JAVA_LONG) // FIXME: 32-bit ssize_t?
        nativeHandleEvents = link("handle_events", ADDRESS, returning = JAVA_INT)
        nativeInit = link("init", ADDRESS, returning = JAVA_INT)
        mhInterruptEventHandler = link("interrupt_event_handler", ADDRESS)
        nativeOpen = link("open", ADDRESS, ADDRESS, returning = JAVA_INT)
        nativeRefDevice = link("ref_device", ADDRESS, returning = ADDRESS)
        nativeReleaseInterface = link("release_interface", ADDRESS, JAVA_INT, returning = JAVA_INT)
        mhResetDevice = link("reset_device", ADDRESS, returning = JAVA_INT)
        mhStrerror = link("strerror", JAVA_INT, returning = ADDRESS)
        nativeSubmitTransfer = link("submit_transfer", ADDRESS, returning = JAVA_INT)
        nativeUnrefDevice = link("unref_device", ADDRESS)
    }

    private fun checkReturn(code: Int) {
        if (code != 0) {
            throw UsbException(errorMessage(code))
        }
    }

    private fun checkSize(value: Long): Long {
        if (value < 0) {
            throw UsbException(errorMessage(toIntExact(value)))
        }

        return value
    }

    private fun errorMessage(code: Int): String {
        return (mhStrerror(code) as MemoryAddress).getUtf8String(0)
    }

    private object NativeTransfer {
        val devHandle: NativeMember<ValueLayout.OfAddress>
        val flags: NativeMember<ValueLayout.OfByte>
        val endpoint: NativeMember<ValueLayout.OfByte>
        val type: NativeMember<ValueLayout.OfByte>
        val timeout: NativeMember<ValueLayout.OfInt>
        val status: NativeMember<ValueLayout.OfInt>
        val length: NativeMember<ValueLayout.OfInt>
        val actualLength: NativeMember<ValueLayout.OfInt>
        val callback: NativeMember<ValueLayout.OfAddress>
        val userData: NativeMember<ValueLayout.OfAddress>
        val buffer: NativeMember<ValueLayout.OfAddress>
        val numIsoPackets: NativeMember<ValueLayout.OfInt>

        val layout: NativeLayout = calculateCStructLayout {
            devHandle = member(ADDRESS)
            flags = member(JAVA_BYTE)
            endpoint = member(JAVA_BYTE)
            type = member(JAVA_BYTE)
            timeout = member(JAVA_INT)
            status = member(JAVA_INT)
            length = member(JAVA_INT)
            actualLength = member(JAVA_INT)
            callback = member(ADDRESS)
            userData = member(ADDRESS)
            buffer = member(ADDRESS)
            numIsoPackets = member(JAVA_INT)
        }
    }

    private object NativeTransferStatus {
        const val COMPLETED = 0
        const val ERROR = 1
        const val TIMED_OUT = 2
        const val CANCELED = 3
        const val STALL = 4
        const val NO_DEVICE = 5
        const val OVERFLOW = 6
    }

    private object NativeTransferFlags {
        const val SHORT_NOT_OK: UByte = 0x1u
        const val FREE_BUFFER: UByte = 0x2u
        const val FREE_TRANSFER: UByte = 0x4u
        const val ADD_ZERO_PACKET: UByte = 0x8u
    }

    private object NativeTransferType {
        const val CONTROL: UByte = 0u
        const val ISOCHRONOUS: UByte = 1u
        const val BULK: UByte = 2u
        const val INTERRUPT: UByte = 3u
        const val BULK_STREAM: UByte = 4u
    }

    private object NativeDeviceDescriptor {
        val bLength: NativeMember<ValueLayout.OfByte>
        val bDescriptorType: NativeMember<ValueLayout.OfByte>
        val bcdUsb: NativeMember<ValueLayout.OfShort>
        val bDeviceClass: NativeMember<ValueLayout.OfByte>
        val bDeviceSubClass: NativeMember<ValueLayout.OfByte>
        val bDeviceProtocol: NativeMember<ValueLayout.OfByte>
        val bMaxPacketSize0: NativeMember<ValueLayout.OfByte>
        val idVendor: NativeMember<ValueLayout.OfShort>
        val idProduct: NativeMember<ValueLayout.OfShort>
        val bcdDevice: NativeMember<ValueLayout.OfShort>
        val iManufacturer: NativeMember<ValueLayout.OfByte>
        val iProduct: NativeMember<ValueLayout.OfByte>
        val iSerialNumber: NativeMember<ValueLayout.OfByte>
        val bNumConfigurations: NativeMember<ValueLayout.OfByte>

        val layout = calculateCStructLayout {
            bLength = member(JAVA_BYTE)
            bDescriptorType = member(JAVA_BYTE)
            bcdUsb = member(JAVA_SHORT)
            bDeviceClass = member(JAVA_BYTE)
            bDeviceSubClass = member(JAVA_BYTE)
            bDeviceProtocol = member(JAVA_BYTE)
            bMaxPacketSize0 = member(JAVA_BYTE)
            idVendor = member(JAVA_SHORT)
            idProduct = member(JAVA_SHORT)
            bcdDevice = member(JAVA_SHORT)
            iManufacturer = member(JAVA_BYTE)
            iProduct = member(JAVA_BYTE)
            iSerialNumber = member(JAVA_BYTE)
            bNumConfigurations = member(JAVA_BYTE)
        }
    }

    private object NativeConfigDescriptor {
        val bLength: NativeMember<ValueLayout.OfByte>
        val bDescriptorType: NativeMember<ValueLayout.OfByte>
        val wTotalLength: NativeMember<ValueLayout.OfShort>
        val bNumInterfaces: NativeMember<ValueLayout.OfByte>
        val bConfigurationValue: NativeMember<ValueLayout.OfByte>
        val iConfiguration: NativeMember<ValueLayout.OfByte>
        val bmAttributes: NativeMember<ValueLayout.OfByte>
        val maxPower: NativeMember<ValueLayout.OfByte>
        val `interface`: NativeMember<ValueLayout.OfAddress>
        val extra: NativeMember<ValueLayout.OfAddress>
        val extraLength: NativeMember<ValueLayout.OfInt>

        val layout = calculateCStructLayout {
            bLength = member(JAVA_BYTE)
            bDescriptorType = member(JAVA_BYTE)
            wTotalLength = member(JAVA_SHORT)
            bNumInterfaces = member(JAVA_BYTE)
            bConfigurationValue = member(JAVA_BYTE)
            iConfiguration = member(JAVA_BYTE)
            bmAttributes = member(JAVA_BYTE)
            maxPower = member(JAVA_BYTE)
            `interface` = member(ADDRESS)
            extra = member(ADDRESS)
            extraLength = member(JAVA_INT)
        }
    }

    private object NativeInterface {
        val altsetting: NativeMember<ValueLayout.OfAddress>
        val numAltsetting: NativeMember<ValueLayout.OfInt>

        val layout = calculateCStructLayout {
            altsetting = member(ADDRESS)
            numAltsetting = member(JAVA_INT)
        }
    }

    private object NativeInterfaceDescriptor {
        val bLength: NativeMember<ValueLayout.OfByte>
        val bDescriptorType: NativeMember<ValueLayout.OfByte>
        val bInterfaceNumber: NativeMember<ValueLayout.OfByte>
        val bAlternateSetting: NativeMember<ValueLayout.OfByte>
        val bNumEndpoints: NativeMember<ValueLayout.OfByte>
        val bInterfaceClass: NativeMember<ValueLayout.OfByte>
        val bInterfaceSubClass: NativeMember<ValueLayout.OfByte>
        val bInterfaceProtocol: NativeMember<ValueLayout.OfByte>
        val iInterface: NativeMember<ValueLayout.OfByte>
        val endpoint: NativeMember<ValueLayout.OfAddress>
        val extra: NativeMember<ValueLayout.OfAddress>
        val extraLength: NativeMember<ValueLayout.OfInt>

        val layout = calculateCStructLayout {
            bLength = member(JAVA_BYTE)
            bDescriptorType = member(JAVA_BYTE)
            bInterfaceNumber = member(JAVA_BYTE)
            bAlternateSetting = member(JAVA_BYTE)
            bNumEndpoints = member(JAVA_BYTE)
            bInterfaceClass = member(JAVA_BYTE)
            bInterfaceSubClass = member(JAVA_BYTE)
            bInterfaceProtocol = member(JAVA_BYTE)
            iInterface = member(JAVA_BYTE)
            endpoint = member(ADDRESS)
            extra = member(ADDRESS)
            extraLength = member(JAVA_INT)
        }
    }

    private object NativeEndpointDescriptor {
        val bLength: NativeMember<ValueLayout.OfByte>
        val bDescriptorType: NativeMember<ValueLayout.OfByte>
        val bEndpointAddress: NativeMember<ValueLayout.OfByte>
        val bmAttributes: NativeMember<ValueLayout.OfByte>
        val wMaxPacketSize: NativeMember<ValueLayout.OfShort>
        val bInterval: NativeMember<ValueLayout.OfByte>
        val bRefresh: NativeMember<ValueLayout.OfByte>
        val bSynchAddress: NativeMember<ValueLayout.OfByte>
        val extra: NativeMember<ValueLayout.OfAddress>
        val extraLength: NativeMember<ValueLayout.OfInt>

        val layout = calculateCStructLayout {
            bLength = member(JAVA_BYTE)
            bDescriptorType = member(JAVA_BYTE)
            bEndpointAddress = member(JAVA_BYTE)
            bmAttributes = member(JAVA_BYTE)
            wMaxPacketSize = member(JAVA_SHORT)
            bInterval = member(JAVA_BYTE)
            bRefresh = member(JAVA_BYTE)
            bSynchAddress = member(JAVA_BYTE)
            extra = member(ADDRESS)
            extraLength = member(JAVA_INT)
        }
    }

    private object NativeControlSetup {
        val bmRequestType: NativeMember<ValueLayout.OfByte>
        val bRequest: NativeMember<ValueLayout.OfByte>
        val wValue: NativeMember<ValueLayout.OfShort>
        val wIndex: NativeMember<ValueLayout.OfShort>
        val wLength: NativeMember<ValueLayout.OfShort>

        val layout = calculateCStructLayout {
            bmRequestType = member(JAVA_BYTE)
            bRequest = member(JAVA_BYTE)
            wValue = member(JAVA_SHORT.withOrder(LITTLE_ENDIAN))
            wIndex = member(JAVA_SHORT.withOrder(LITTLE_ENDIAN))
            wLength = member(JAVA_SHORT.withOrder(LITTLE_ENDIAN))
        }
    }
}
