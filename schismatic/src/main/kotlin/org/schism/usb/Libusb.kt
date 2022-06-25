package org.schism.usb

import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import org.schism.coroutines.updateMutating
import org.schism.foreign.NativeAddress
import org.schism.foreign.NativeBuffer
import org.schism.foreign.NativeLayout
import org.schism.foreign.NativeMember
import org.schism.foreign.byteOffset
import org.schism.foreign.calculateCStructLayout
import org.schism.foreign.index
import org.schism.foreign.nativeAddress
import org.schism.foreign.nextLeUtf16
import org.schism.foreign.putLeUShort
import org.schism.foreign.putUByte
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
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.io.path.Path

object Libusb : UsbBackend {
    private val nativeAllocTransfer: MethodHandle
    private val nativeCancelTransfer: MethodHandle
    private val nativeClaimInterface: MethodHandle
    private val nativeClearHalt: MethodHandle
    private val nativeClose: MethodHandle
    private val nativeFreeConfigDescriptor: MethodHandle
    private val nativeFreeDeviceList: MethodHandle
    private val nativeFreeTransfer: MethodHandle
    private val nativeGetBusNumber: MethodHandle
    private val nativeGetConfigDescriptor: MethodHandle
    private val nativeGetConfiguration: MethodHandle
    private val nativeGetDeviceAddress: MethodHandle
    private val nativeGetDeviceDescriptor: MethodHandle
    private val nativeGetDeviceList: MethodHandle
    private val nativeGetPortNumbers: MethodHandle
    private val nativeHandleEvents: MethodHandle
    private val nativeInit: MethodHandle
    private val nativeOpen: MethodHandle
    private val nativeRefDevice: MethodHandle
    private val nativeReleaseInterface: MethodHandle
    private val nativeResetDevice: MethodHandle
    private val nativeSetInterfaceAltSetting: MethodHandle
    private val nativeStrerror: MethodHandle
    private val nativeSubmitTransfer: MethodHandle
    private val nativeUnrefDevice: MethodHandle

    init {
        val lib = libraryLookup(Path("/opt/homebrew/lib/libusb-1.0.dylib"), MemorySession.global())
        val linker = nativeLinker()

        fun link(name: String, vararg argumentLayouts: MemoryLayout, ret: MemoryLayout? = null): MethodHandle {
            val descriptor = if (ret != null) {
                FunctionDescriptor.of(ret, *argumentLayouts)
            } else {
                FunctionDescriptor.ofVoid(*argumentLayouts)
            }

            return linker.downcallHandle(lib.lookup("libusb_$name").orElseThrow(), descriptor)
        }

        nativeAllocTransfer = link("alloc_transfer", JAVA_INT, ret = ADDRESS)
        nativeCancelTransfer = link("cancel_transfer", ADDRESS, ret = JAVA_INT)
        nativeClaimInterface = link("claim_interface", ADDRESS, JAVA_INT, ret = JAVA_INT)
        nativeClearHalt = link("clear_halt", ADDRESS, JAVA_BYTE, ret = JAVA_INT)
        nativeClose = link("close", ADDRESS)
        nativeFreeConfigDescriptor = link("free_config_descriptor", ADDRESS)
        nativeFreeDeviceList = link("free_device_list", ADDRESS, JAVA_INT)
        nativeFreeTransfer = link("free_transfer", ADDRESS)
        nativeGetBusNumber = link("get_bus_number", ADDRESS, ret = JAVA_BYTE)
        nativeGetConfigDescriptor = link("get_config_descriptor", ADDRESS, JAVA_BYTE, ADDRESS, ret = JAVA_INT)
        nativeGetConfiguration = link("get_configuration", ADDRESS, ADDRESS, ret = JAVA_INT)
        nativeGetDeviceAddress = link("get_device_address", ADDRESS, ret = JAVA_BYTE)
        nativeGetDeviceDescriptor = link("get_device_descriptor", ADDRESS, ADDRESS, ret = JAVA_INT)
        nativeGetDeviceList = link("get_device_list", ADDRESS, ADDRESS, ret = JAVA_LONG) // FIXME: 32-bit ssize_t?
        nativeGetPortNumbers = link("get_port_numbers", ADDRESS, ADDRESS, JAVA_INT, ret = JAVA_INT)
        nativeHandleEvents = link("handle_events", ADDRESS, ret = JAVA_INT)
        nativeInit = link("init", ADDRESS, ret = JAVA_INT)
        nativeOpen = link("open", ADDRESS, ADDRESS, ret = JAVA_INT)
        nativeRefDevice = link("ref_device", ADDRESS, ret = ADDRESS)
        nativeReleaseInterface = link("release_interface", ADDRESS, JAVA_INT, ret = JAVA_INT)
        nativeResetDevice = link("reset_device", ADDRESS, ret = JAVA_INT)
        nativeSetInterfaceAltSetting = link("set_interface_alt_setting", ADDRESS, JAVA_INT, JAVA_INT, ret = JAVA_INT)
        nativeStrerror = link("strerror", JAVA_INT, ret = ADDRESS)
        nativeSubmitTransfer = link("submit_transfer", ADDRESS, ret = JAVA_INT)
        nativeUnrefDevice = link("unref_device", ADDRESS)
    }

    override val attachedDevices: StateFlow<PersistentSet<UsbDevice>>

    private val handle = NativeBuffer.withUnmanaged(ADDRESS) { handleBuffer ->
        checkReturn(nativeInit(handleBuffer.start.toMemoryAddress()) as Int)
        handleBuffer[ADDRESS]
    }

    private val deviceCleaner = Cleaner.create(
        Thread.ofPlatform()
            .name("Libusb.Device cleaner")
            .factory()
    )

    init {
        attachedDevices = MutableStateFlow(persistentSetOf<Device>())

        thread(isDaemon = true, name = "libusb event handler") {
            while (true) {
                checkReturn(nativeHandleEvents(handle.toMemoryAddress()) as Int)
            }
        }

        thread(isDaemon = true, name = "libusb device enumerator") {
            var devices = emptySet<Device>()
            var devicesByHandle = emptyMap<NativeAddress, Result<Device>>()

            NativeBuffer.withUnmanaged(ADDRESS) { listBuffer ->
                while (true) {
                    val listSize = checkSize(
                        nativeGetDeviceList(
                            handle.toMemoryAddress(),
                            listBuffer.start.toMemoryAddress()
                        ) as Long
                    )

                    val list = NativeBuffer.unmanaged(listBuffer[ADDRESS], listSize * ADDRESS.byteSize())
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
                        nativeFreeDeviceList(list.start.toMemoryAddress(), 1)
                    }

                    val newDevices = buildSet {
                        newDevicesByHandle.values.forEach {
                            it.getOrNull()?.let(::add)
                        }
                    }

                    val addedDevices = newDevices - devices
                    val removedDevices = devices - newDevices

                    attachedDevices.updateMutating {
                        removeAll(removedDevices)
                        addAll(addedDevices)
                    }

                    devicesByHandle = newDevicesByHandle
                    devices = newDevices

                    Thread.sleep(500)
                }
            }
        }
    }

    private class DeviceConnection(override val device: Device) : UsbDeviceConnection {
        private val handle = NativeBuffer.withUnmanaged(ADDRESS) { handleBuffer ->
            checkReturn(
                nativeOpen(
                    device.handle.toMemoryAddress(),
                    handleBuffer.start.toMemoryAddress(),
                ) as Int
            )

            handleBuffer[ADDRESS]
        }

        private val lifetime = SharedLifetime()

        private suspend fun transfer(endpointAddress: UByte, buffer: NativeBuffer): Int {
            currentCoroutineContext().ensureActive()

            lifetime.withRetained {
                val nativeTransferAddress = (nativeAllocTransfer(0) as MemoryAddress).nativeAddress()

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

                            when (val returnCode = nativeSubmitTransfer(nativeTransferAddress.toMemoryAddress()) as Int) {
                                0 -> Unit
                                else -> {
                                    inFlightTransfers.remove(nativeTransferAddress)
                                    continuation.resumeWithException(UsbException(errorMessage(returnCode)))
                                    return@suspendCoroutine
                                }
                            }

                            @OptIn(InternalCoroutinesApi::class)
                            continuation.context[Job]?.invokeOnCompletion(
                                onCancelling = true,
                                invokeImmediately = true,
                            ) {
                                nativeCancelTransfer(nativeTransferAddress.toMemoryAddress())
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

                    return nativeTransfer[NativeTransfer.actualLength]
                } finally {
                    nativeFreeTransfer(nativeTransferAddress.toMemoryAddress())
                }
            }
        }

        private suspend fun getString(index: UByte): String? {
            if (index == 0.toUByte()) {
                return null
            }

            NativeBuffer.withUnmanagedUninitialized(8 + 255) { buffer ->
                buffer.slice(size = 8).encoder().run {
                    putUByte(0x80u)
                    putUByte(0x06u)
                    putUByte(index)
                    putUByte(0x03u)
                    putNativeShort(0)
                    putLeUShort(255u)
                }

                val actualLength = transfer(0u, buffer)

                return buffer.slice(10.byteOffset).decoder().nextLeUtf16(actualLength - 2)
            }
        }

        override suspend fun getActiveConfiguration(): UsbConfiguration? {
            currentCoroutineContext().ensureActive()

            val activeValue = lifetime.withRetained {
                withContext(NonCancellable) {
                    withContext(Dispatchers.IO) {
                        NativeBuffer.withUnmanaged(JAVA_INT) { configurationValueBuffer ->
                            checkReturn(
                                nativeGetConfiguration(
                                    handle.toMemoryAddress(),
                                    configurationValueBuffer.start.toMemoryAddress(),
                                ) as Int
                            )

                            configurationValueBuffer[JAVA_INT].toUByte()
                        }
                    }
                }
            }

            currentCoroutineContext().ensureActive()

            return device.configurations.firstOrNull { it.value == activeValue }
        }

        override suspend fun resetDevice() {
            currentCoroutineContext().ensureActive()

            lifetime.withRetained {
                withContext(NonCancellable) {
                    withContext(Dispatchers.IO) {
                        checkReturn(nativeResetDevice(handle.toMemoryAddress()) as Int)
                    }
                }
            }

            currentCoroutineContext().ensureActive()
        }

        override suspend fun getManufacturerName(): String? {
            return getString(device.iManufacturer)
        }

        override suspend fun getProductName(): String? {
            return getString(device.iProduct)
        }

        override suspend fun getSerialNumber(): String? {
            return getString(device.iSerialNumber)
        }

        override fun UsbInterface.claim() {
            require(this@UsbInterface is Interface)
            require(this@UsbInterface.device === this@DeviceConnection.device)

            lifetime.withRetained {
                checkReturn(
                    nativeClaimInterface(
                        handle.toMemoryAddress(),
                        this@UsbInterface.number.toInt(),
                    ) as Int
                )
            }
        }

        override suspend fun UsbInterface.release() {
            currentCoroutineContext().ensureActive()

            require(this@UsbInterface is Interface)
            require(this@UsbInterface.device === this@DeviceConnection.device)

            lifetime.withRetained {
                withContext(NonCancellable) {
                    withContext(Dispatchers.IO) {
                        checkReturn(
                            nativeReleaseInterface(
                                handle.toMemoryAddress(),
                                this@UsbInterface.number.toInt(),
                            ) as Int
                        )
                    }
                }
            }

            currentCoroutineContext().ensureActive()
        }

        override suspend fun UsbAlternateSetting.makeActive() {
            currentCoroutineContext().ensureActive()

            require(this@UsbAlternateSetting is AlternateSetting)
            require(this@UsbAlternateSetting.device === this@DeviceConnection.device)

            lifetime.withRetained {
                withContext(NonCancellable) {
                    withContext(Dispatchers.IO) {
                        checkReturn(
                            nativeSetInterfaceAltSetting(
                                handle.toMemoryAddress(),
                                `interface`.number.toInt(),
                                value.toInt(),
                            ) as Int
                        )
                    }
                }
            }

            currentCoroutineContext().ensureActive()
        }

        override suspend fun UsbAlternateSetting.getName(): String? {
            require(this@UsbAlternateSetting is AlternateSetting)
            require(this@UsbAlternateSetting.device === this@DeviceConnection.device)
            return getString(iInterface)
        }

        override suspend fun UsbEndpoint.clearHalt() {
            currentCoroutineContext().ensureActive()

            require(this@UsbEndpoint is Endpoint)
            require(this@UsbEndpoint.device === this@DeviceConnection.device)

            lifetime.withRetained {
                withContext(NonCancellable) {
                    withContext(Dispatchers.IO) {
                        checkReturn(
                            nativeClearHalt(
                                handle.toMemoryAddress(),
                                this@UsbEndpoint.address.toByte(),
                            ) as Int
                        )
                    }
                }
            }

            currentCoroutineContext().ensureActive()
        }

        override suspend fun UsbBulkTransferInEndpoint.receive(destination: NativeBuffer): Long {
            require(this@UsbBulkTransferInEndpoint is BulkTransferInEndpoint)
            require(this@UsbBulkTransferInEndpoint.device === this@DeviceConnection.device)
            return transfer(this@UsbBulkTransferInEndpoint.address, destination).toLong()
        }

        override suspend fun UsbBulkTransferOutEndpoint.send(source: NativeBuffer): Long {
            require(this@UsbBulkTransferOutEndpoint is BulkTransferOutEndpoint)
            require(this@UsbBulkTransferOutEndpoint.device === this@DeviceConnection.device)
            return transfer(this@UsbBulkTransferOutEndpoint.address, source).toLong()
        }

        override suspend fun close() {
            if (lifetime.end()) {
                nativeClose(handle.toMemoryAddress())
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
            nativeRefDevice(handle.toMemoryAddress())
            deviceCleaner.register(this) { nativeUnrefDevice(handle.toMemoryAddress()) }
        }

        val handle = handle

        override val portNumbers: List<UByte>

        override val usbVersion: UShort
        override val deviceClass: UByte
        override val deviceSubClass: UByte
        override val deviceProtocol: UByte
        override val vendorId: UShort
        override val productId: UShort
        override val deviceVersion: UShort
        override val configurations: List<Configuration>

        val iManufacturer: UByte
        val iProduct: UByte
        val iSerialNumber: UByte

        override val backend get() = Libusb

        init {
            NativeBuffer.withUnmanaged(7) { portNumbersBuffer ->
                val portNumberCount = checkSize(
                    nativeGetPortNumbers(
                        handle.toMemoryAddress(),
                        portNumbersBuffer.start.toMemoryAddress(),
                        portNumbersBuffer.size.toInt(),
                    ) as Int
                )

                portNumbers = List(portNumberCount) {
                    portNumbersBuffer[JAVA_BYTE, it.byteOffset].toUByte()
                }
            }

            NativeBuffer.withUnmanaged(NativeDeviceDescriptor.layout) { descriptor ->
                checkReturn(
                    nativeGetDeviceDescriptor(
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
                iManufacturer = descriptor[NativeDeviceDescriptor.iManufacturer].toUByte()
                iProduct = descriptor[NativeDeviceDescriptor.iProduct].toUByte()
                iSerialNumber = descriptor[NativeDeviceDescriptor.iSerialNumber].toUByte()

                val numConfigurations = descriptor[NativeDeviceDescriptor.bNumConfigurations].toUByte()

                NativeBuffer.withUnmanaged(ADDRESS) { configDescriptorAddressBuffer ->
                    configurations = List(numConfigurations.toInt()) { configIndex ->
                        checkReturn(
                            nativeGetConfigDescriptor(
                                handle.toMemoryAddress(),
                                configIndex.toByte(),
                                configDescriptorAddressBuffer.start.toMemoryAddress(),
                            ) as Int
                        )

                        val configDescriptor = NativeBuffer.unmanaged(
                            configDescriptorAddressBuffer[ADDRESS],
                            NativeConfigDescriptor.layout.size,
                        )

                        try {
                            Configuration(this@Device, configDescriptor)
                        } finally {
                            nativeFreeConfigDescriptor(configDescriptor.start.toMemoryAddress())
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
        val value = descriptor[NativeConfigDescriptor.bConfigurationValue].toUByte()

        override val interfaces: List<UsbInterface> =
            NativeBuffer.unmanagedArrayElements(
                descriptor[NativeConfigDescriptor.`interface`],
                NativeInterface.layout,
                descriptor[NativeConfigDescriptor.bNumInterfaces].toUByte().toInt(),
            ).map {
                Interface(this@Configuration, it)
            }
    }

    private class Interface(override val configuration: Configuration, native: NativeBuffer) : UsbInterface {
        val number: UByte

        override val alternateSettings: List<AlternateSetting> =
            NativeBuffer.unmanagedArrayElements(
                native[NativeInterface.altsetting],
                NativeInterfaceDescriptor.layout,
                native[NativeInterface.numAltsetting],
            ).let { interfaceDescriptors ->
                number = interfaceDescriptors.first()[NativeInterfaceDescriptor.bInterfaceNumber].toUByte()

                interfaceDescriptors.map { interfaceDescriptor ->
                    check(interfaceDescriptor[NativeInterfaceDescriptor.bInterfaceNumber].toUByte() == number)
                    AlternateSetting(this@Interface, interfaceDescriptor)
                }
            }
    }

    private class AlternateSetting(
        override val `interface`: Interface,
        descriptor: NativeBuffer,
    ) : UsbAlternateSetting {
        val value = descriptor[NativeInterfaceDescriptor.bAlternateSetting].toUByte()
        val iInterface = descriptor[NativeInterfaceDescriptor.iInterface].toUByte()

        override val interfaceClass = descriptor[NativeInterfaceDescriptor.bInterfaceClass].toUByte()
        override val interfaceSubClass = descriptor[NativeInterfaceDescriptor.bInterfaceSubClass].toUByte()
        override val interfaceProtocol = descriptor[NativeInterfaceDescriptor.bInterfaceProtocol].toUByte()

        override val endpoints: List<Endpoint> =
            NativeBuffer.unmanagedArrayElements(
                descriptor[NativeInterfaceDescriptor.endpoint],
                NativeEndpointDescriptor.layout,
                descriptor[NativeInterfaceDescriptor.bNumEndpoints].toUByte().toInt(),
            ).mapNotNull { endpointDescriptor ->
                val address = endpointDescriptor[NativeEndpointDescriptor.bEndpointAddress].toUByte()
                val attributes = endpointDescriptor[NativeEndpointDescriptor.bmAttributes].toUByte().toInt()
                val maxPacketSize = endpointDescriptor[NativeEndpointDescriptor.wMaxPacketSize].toUShort()
                when (attributes and 0b11) {
                    0 -> null
                    1 -> null
                    2 -> when (address.toUInt() shr 7) {
                        0u -> BulkTransferOutEndpoint(this@AlternateSetting, maxPacketSize, address)
                        1u -> BulkTransferInEndpoint(this@AlternateSetting, maxPacketSize, address)
                        else -> throw AssertionError()
                    }
                    3 -> null
                    else -> throw AssertionError()
                }
            }
    }

    private sealed class Endpoint(
        override val alternateSetting: AlternateSetting,
        override val maxPacketSize: UShort,
        val address: UByte,
    ) : UsbEndpoint

    private class BulkTransferInEndpoint(
        alternateSetting: AlternateSetting,
        maxPacketSize: UShort,
        address: UByte,
    ) : Endpoint(alternateSetting, maxPacketSize, address), UsbBulkTransferInEndpoint

    private class BulkTransferOutEndpoint(
        alternateSetting: AlternateSetting,
        maxPacketSize: UShort,
        address: UByte,
    ) : Endpoint(alternateSetting, maxPacketSize, address), UsbBulkTransferOutEndpoint

    private fun checkReturn(code: Int) {
        if (code != 0) {
            throw UsbException(errorMessage(code))
        }
    }

    private fun checkSize(value: Int): Int {
        if (value < 0) {
            throw UsbException(errorMessage(value))
        }

        return value
    }

    private fun checkSize(value: Long): Long {
        if (value < 0) {
            throw UsbException(errorMessage(toIntExact(value)))
        }

        return value
    }

    private fun errorMessage(code: Int): String {
        return (nativeStrerror(code) as MemoryAddress).getUtf8String(0)
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
