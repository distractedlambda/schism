package org.schism.usb

import org.schism.ffi.address
import org.schism.ffi.withNativeStruct
import org.schism.ffi.wrap
import org.schism.memory.NativeAddress
import org.schism.memory.withNativePointer
import org.schism.memory.withNativeUBytes
import org.schism.ref.registerCleanup
import org.schism.usb.Libusb.Companion.checkReturn
import org.schism.usb.Libusb.Companion.checkSize
import org.schism.usb.Libusb.Companion.freeConfigDescriptor
import org.schism.usb.Libusb.Companion.getConfigDescriptor
import org.schism.usb.Libusb.Companion.getDeviceDescriptor
import org.schism.usb.Libusb.Companion.getPortNumbers
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
                val configDescriptor = ConfigDescriptor.wrap(configDescriptorAddress)
                TODO()
            } finally {
                freeConfigDescriptor(configDescriptorAddress)
            }
        }
    }

    public companion object {
        private const val MAX_PORT_NUMBERS = 7
    }
}
