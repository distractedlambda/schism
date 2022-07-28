package org.schism.usb

import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.schism.coroutines.SharedLifetime
import org.schism.coroutines.updateMutating
import org.schism.foreign.address
import org.schism.foreign.allocate
import org.schism.foreign.asStruct
import org.schism.foreign.getPointer
import org.schism.foreign.getUByte
import org.schism.foreign.withConfinedMemorySession
import org.schism.ref.registerCleanup
import org.schism.usb.Libusb.ConfigDescriptor
import org.schism.usb.Libusb.DeviceDescriptor
import java.lang.foreign.MemoryAddress
import java.lang.foreign.ValueLayout.ADDRESS
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

public class UsbDevice internal constructor(nativeHandle: MemoryAddress) {
    init {
        libusb.ref_device(nativeHandle)
        registerCleanup { libusb.unref_device(nativeHandle) }
    }

    private val nativeHandle = nativeHandle

    internal val iManufacturer: UByte
    internal val iProduct: UByte
    internal val iSerialNumber: UByte

    public val usbVersion: UShort
    public val deviceClass: UByte
    public val deviceSubClass: UByte
    public val deviceProtocol: UByte
    public val vendorId: UShort
    public val productId: UShort
    public val deviceVersion: UShort
    public val portNumbers: List<UByte>
    public val configurations: List<UsbConfiguration>

    init {
        val numConfigurations: UByte

        withConfinedMemorySession {
            val deviceDescriptor = allocate(DeviceDescriptor.Type)
            checkLibusbReturn(libusb.get_device_descriptor(nativeHandle, deviceDescriptor.address()))
            iManufacturer = deviceDescriptor.iManufacturer
            iProduct = deviceDescriptor.iProduct
            iSerialNumber = deviceDescriptor.iSerialNumber
            usbVersion = deviceDescriptor.bcdUSB
            deviceClass = deviceDescriptor.bDeviceClass
            deviceSubClass = deviceDescriptor.bDeviceSubClass
            deviceProtocol = deviceDescriptor.bDeviceProtocol
            vendorId = deviceDescriptor.idVendor
            productId = deviceDescriptor.idProduct
            deviceVersion = deviceDescriptor.bcdDevice
            numConfigurations = deviceDescriptor.bNumConfigurations
        }

        withConfinedMemorySession {
            val nativePortNumbers = allocate(MAX_PORT_NUMBERS.toLong())

            val portNumbersSize = checkLibusbSizeReturn(
                libusb.get_port_numbers(
                    nativeHandle,
                    nativePortNumbers,
                    MAX_PORT_NUMBERS,
                )
            )

            portNumbers = List(portNumbersSize) {
                nativePortNumbers.getUByte(it.toLong())
            }
        }

        configurations = List(numConfigurations.toInt()) { configIndex ->
            val configDescriptorAddress = withConfinedMemorySession {
                val buffer = allocate(ADDRESS)

                checkLibusbReturn(
                    libusb.get_config_descriptor(
                        nativeHandle,
                        configIndex.toUByte(),
                        buffer,
                    )
                )

                buffer.getPointer()
            }

            try {
                UsbConfiguration(this, configDescriptorAddress.asStruct(ConfigDescriptor.Type))
            } finally {
                libusb.free_config_descriptor(configDescriptorAddress)
            }
        }
    }

    @OptIn(ExperimentalContracts::class)
    public suspend fun <R> connect(block: suspend (UsbDeviceConnection) -> R): R {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }

        val lifetime = SharedLifetime()

        val nativeConnectionHandle = withConfinedMemorySession {
            val buffer = allocate(ADDRESS)
            checkLibusbReturn(libusb.open(nativeHandle, buffer))
            buffer.getPointer()
        }

        try {
            return block(UsbDeviceConnection(this, nativeConnectionHandle, lifetime))
        } finally {
            lifetime.end()
            libusb.close(nativeConnectionHandle)
        }
    }

    public companion object {
        private const val MAX_PORT_NUMBERS = 7

        private val mutableAllDevices = MutableStateFlow(persistentSetOf<UsbDevice>())

        public val allDevices: StateFlow<PersistentSet<UsbDevice>> = mutableAllDevices.asStateFlow()

        init {
            @OptIn(DelicateCoroutinesApi::class)
            GlobalScope.launch(CoroutineName("UsbDevice enumerator")) {
                var lastDevices = emptySet<UsbDevice>()
                var lastDevicesByHandle = emptyMap<MemoryAddress, Result<UsbDevice>>()

                withConfinedMemorySession {
                    val deviceListPointer = allocate(ADDRESS)

                    while (true) {
                        val newDevicesByHandle: MutableMap<MemoryAddress, Result<UsbDevice>>

                        val listSize = checkLibusbSizeReturn(
                            libusb.get_device_list(
                                libusbContext,
                                deviceListPointer,
                            )
                        )

                        val deviceList = deviceListPointer.getPointer()

                        try {
                            newDevicesByHandle = hashMapOf()

                            for (handleIndex in 0 until listSize) {
                                val handle = deviceList.getPointer(handleIndex * ADDRESS.byteSize())
                                newDevicesByHandle[handle] = lastDevicesByHandle[handle]
                                    ?: try {
                                        Result.success(UsbDevice(handle))
                                    } catch (exception: UsbException) {
                                        Result.failure(exception)
                                    }
                            }
                        } finally {
                            libusb.free_device_list(deviceList, unref_devices = 1)
                        }

                        val newDevices = buildSet {
                            newDevicesByHandle.values.forEach {
                                it.getOrNull()?.let(::add)
                            }
                        }

                        val addedDevices = newDevices - lastDevices
                        val removedDevices = lastDevices - newDevices

                        mutableAllDevices.updateMutating {
                            removeAll(removedDevices)
                            addAll(addedDevices)
                        }

                        lastDevices = newDevices
                        lastDevicesByHandle = newDevicesByHandle

                        delay(500)
                    }
                }
            }
        }
    }
}
