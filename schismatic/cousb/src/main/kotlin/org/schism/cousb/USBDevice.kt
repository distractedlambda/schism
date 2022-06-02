package org.schism.cousb

import org.schism.cousb.Libusb.DeviceDescriptor
import java.lang.foreign.MemoryAddress
import java.lang.foreign.MemorySession
import java.lang.foreign.ValueLayout.ADDRESS
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind.EXACTLY_ONCE
import kotlin.contracts.contract

public class USBDevice internal constructor(handle: MemoryAddress) {
    init {
        Libusb.refDevice(handle) as MemoryAddress
        USBContext.cleaner.register(this) { Libusb.unrefDevice(handle) }
    }

    internal val handle = handle

    public val usbVersion: UShort
    public val deviceClass: UByte
    public val deviceSubClass: UByte
    public val deviceProtocol: UByte
    public val endpoint0MaxPacketSize: UByte
    public val vendorID: UShort
    public val productID: UShort
    public val deviceVersion: UShort
    public val manufacturer: USBStringDescriptorIndex
    public val product: USBStringDescriptorIndex
    public val serialNumber: USBStringDescriptorIndex
    public val configurations: List<USBConfiguration>

    init {
        MemorySession.openConfined().use { memorySession ->
            val descriptor = memorySession.allocate(DeviceDescriptor.LAYOUT)
            Libusb.checkReturn(Libusb.getDeviceDescriptor(handle, descriptor) as Int)

            usbVersion = (DeviceDescriptor.BCD_USB[descriptor] as Short).toUShort()
            deviceClass = (DeviceDescriptor.B_DEVICE_CLASS[descriptor] as Byte).toUByte()
            deviceSubClass = (DeviceDescriptor.B_DEVICE_SUB_CLASS[descriptor] as Byte).toUByte()
            deviceProtocol = (DeviceDescriptor.B_DEVICE_PROTOCOL[descriptor] as Byte).toUByte()
            endpoint0MaxPacketSize = (DeviceDescriptor.B_MAX_PACKET_SIZE_0[descriptor] as Byte).toUByte()
            vendorID = (DeviceDescriptor.ID_VENDOR[descriptor] as Short).toUShort()
            productID = (DeviceDescriptor.ID_PRODUCT[descriptor] as Short).toUShort()
            deviceVersion = (DeviceDescriptor.BCD_DEVICE[descriptor] as Short).toUShort()
            manufacturer = USBStringDescriptorIndex((DeviceDescriptor.I_MANUFACTURER[descriptor] as Byte).toUByte())
            product = USBStringDescriptorIndex((DeviceDescriptor.I_PRODUCT[descriptor] as Byte).toUByte())
            serialNumber = USBStringDescriptorIndex((DeviceDescriptor.I_SERIAL_NUMBER[descriptor] as Byte).toUByte())

            val numConfigurations = (DeviceDescriptor.B_NUM_CONFIGURATIONS[descriptor] as Byte).toUByte()
            val configDescriptorStorage = memorySession.allocate(ADDRESS)

            configurations = buildList {
                for (i in 0 until numConfigurations.toInt()) {
                    Libusb.checkReturn(Libusb.getConfigDescriptor(handle, i.toByte(), configDescriptorStorage) as Int)
                    val configDescriptor = configDescriptorStorage[ADDRESS, 0]
                    try {
                        add(USBConfiguration(this@USBDevice, configDescriptor))
                    } finally {
                        Libusb.freeConfigDescriptor(configDescriptor)
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalContracts::class)
    public suspend inline fun <R> connect(block: (USBDeviceConnection) -> R): R {
        contract {
            callsInPlace(block, EXACTLY_ONCE)
        }

        val connection = USBDeviceConnection(this@USBDevice)

        try {
            return block(connection)
        } finally {
            connection.close()
        }
    }

    public companion object
}
