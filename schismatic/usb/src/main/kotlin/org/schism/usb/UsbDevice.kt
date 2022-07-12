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
import org.schism.coroutines.updateMutating
import org.schism.ffi.address
import org.schism.ffi.withNativeStruct
import org.schism.ffi.wrap
import org.schism.memory.NativeAddress
import org.schism.memory.nativePointers
import org.schism.memory.withNativePointer
import org.schism.memory.withNativeUBytes
import org.schism.ref.registerCleanup
import org.schism.usb.Libusb.Companion.checkReturn
import org.schism.usb.Libusb.Companion.checkSize
import org.schism.usb.Libusb.Companion.freeConfigDescriptor
import org.schism.usb.Libusb.Companion.freeDeviceList
import org.schism.usb.Libusb.Companion.getConfigDescriptor
import org.schism.usb.Libusb.Companion.getDeviceDescriptor
import org.schism.usb.Libusb.Companion.getDeviceList
import org.schism.usb.Libusb.Companion.getPortNumbers
import org.schism.usb.Libusb.Companion.open
import org.schism.usb.Libusb.Companion.refDevice
import org.schism.usb.Libusb.Companion.unrefDevice
import org.schism.usb.Libusb.DeviceDescriptor
import org.schism.usb.Libusb.ConfigDescriptor

public class UsbDevice internal constructor(nativeHandle: NativeAddress) {
    init {
        refDevice(nativeHandle)
        registerCleanup { unrefDevice(nativeHandle) }
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

        withNativeStruct(DeviceDescriptor) { deviceDescriptor ->
            checkReturn(getDeviceDescriptor(nativeHandle, deviceDescriptor.address()))
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
            val portNumbersSize = checkSize(
                getPortNumbers(
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
                checkReturn(
                    getConfigDescriptor(
                        nativeHandle,
                        configIndex.toUByte(),
                        configDescriptorPointer.address,
                    )
                )

                configDescriptorPointer.value
            }

            try {
                UsbConfiguration(this, ConfigDescriptor.wrap(configDescriptorAddress))
            } finally {
                freeConfigDescriptor(configDescriptorAddress)
            }
        }
    }

    public fun connect(): UsbDeviceConnection {
        return UsbDeviceConnection(
            device = this,
            nativeHandle = withNativePointer {
                checkReturn(open(nativeHandle, it.address))
                it.value
            },
        )
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
                        val listSize = checkSize(getDeviceList(Libusb.context, deviceListPointer.address))

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
                            freeDeviceList(deviceListPointer.value, unrefDevices = 1)
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
