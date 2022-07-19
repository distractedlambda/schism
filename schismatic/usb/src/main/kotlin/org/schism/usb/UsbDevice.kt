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
import org.schism.ffi.address
import org.schism.ffi.withNativeStruct
import org.schism.ffi.wrap
import org.schism.memory.NativeAddress
import org.schism.memory.nativePointers
import org.schism.memory.withNativePointer
import org.schism.memory.withNativeUBytes
import org.schism.ref.registerCleanup
import org.schism.usb.Libusb.ConfigDescriptor
import org.schism.usb.Libusb.DeviceDescriptor
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

public class UsbDevice internal constructor(nativeHandle: NativeAddress) {
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

        withNativeStruct(DeviceDescriptor.Type) { deviceDescriptor ->
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

        withNativeUBytes(MAX_PORT_NUMBERS.toLong()) { nativePortNumbers ->
            val portNumbersSize = checkLibusbSizeReturn(
                libusb.get_port_numbers(
                    nativeHandle,
                    nativePortNumbers.startAddress,
                    MAX_PORT_NUMBERS,
                )
            )

            portNumbers = List(portNumbersSize) {
                nativePortNumbers[it.toLong()]
            }
        }

        configurations = List(numConfigurations.toInt()) { configIndex ->
            val configDescriptorAddress = withNativePointer { configDescriptorPointer ->
                checkLibusbReturn(
                    libusb.get_config_descriptor(
                        nativeHandle,
                        configIndex.toUByte(),
                        configDescriptorPointer.address,
                    )
                )

                configDescriptorPointer.value
            }

            try {
                UsbConfiguration(this, ConfigDescriptor.Type.wrap(configDescriptorAddress))
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

        val nativeConnectionHandle = withNativePointer {
            checkLibusbReturn(libusb.open(nativeHandle, it.address))
            it.value
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
                var lastDevicesByHandle = emptyMap<NativeAddress, Result<UsbDevice>>()

                withNativePointer { deviceListPointer ->
                    while (true) {
                        val newDevicesByHandle: MutableMap<NativeAddress, Result<UsbDevice>>

                        val listSize = checkLibusbSizeReturn(
                            libusb.get_device_list(
                                libusbContext,
                                deviceListPointer.address,
                            )
                        )

                        try {
                            val deviceList = nativePointers(deviceListPointer.value, listSize.toLong())
                            newDevicesByHandle = hashMapOf()

                            for (handle in deviceList) {
                                newDevicesByHandle[handle] = lastDevicesByHandle[handle]
                                    ?: try {
                                        Result.success(UsbDevice(handle))
                                    } catch (exception: UsbException) {
                                        Result.failure(exception)
                                    }
                            }
                        } finally {
                            libusb.free_device_list(deviceListPointer.value, unref_devices = 1)
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
