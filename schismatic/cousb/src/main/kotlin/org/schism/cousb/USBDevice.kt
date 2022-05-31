package org.schism.cousb

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import org.schism.cousb.Libusb.DeviceDescriptor
import org.schism.cousb.Libusb.DeviceDescriptor.BCD_DEVICE
import org.schism.cousb.Libusb.DeviceDescriptor.BCD_USB
import org.schism.cousb.Libusb.DeviceDescriptor.B_DEVICE_CLASS
import org.schism.cousb.Libusb.DeviceDescriptor.B_DEVICE_PROTOCOL
import org.schism.cousb.Libusb.DeviceDescriptor.B_DEVICE_SUB_CLASS
import org.schism.cousb.Libusb.DeviceDescriptor.B_MAX_PACKET_SIZE_0
import org.schism.cousb.Libusb.DeviceDescriptor.B_NUM_CONFIGURATIONS
import org.schism.cousb.Libusb.DeviceDescriptor.ID_PRODUCT
import org.schism.cousb.Libusb.DeviceDescriptor.ID_VENDOR
import org.schism.cousb.Libusb.DeviceDescriptor.I_MANUFACTURER
import org.schism.cousb.Libusb.DeviceDescriptor.I_PRODUCT
import org.schism.cousb.Libusb.DeviceDescriptor.I_SERIAL_NUMBER
import org.schism.cousb.Libusb.checkReturnCode
import org.schism.cousb.Libusb.freeConfigDescriptor
import org.schism.cousb.Libusb.getConfigDescriptor
import org.schism.cousb.Libusb.getDeviceDescriptor
import org.schism.cousb.Libusb.refDevice
import org.schism.cousb.Libusb.unrefDevice
import java.lang.foreign.MemoryAddress
import java.lang.foreign.MemorySession
import java.lang.foreign.ValueLayout.ADDRESS
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind.EXACTLY_ONCE
import kotlin.contracts.contract

public class USBDevice internal constructor(handle: MemoryAddress) {
    init {
        refDevice(handle) as MemoryAddress
        USBContext.cleaner.register(this) { unrefDevice(handle) }
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
            checkReturnCode(getDeviceDescriptor(handle, descriptor) as Int)

            usbVersion = (BCD_USB[descriptor] as Short).toUShort()
            deviceClass = (B_DEVICE_CLASS[descriptor] as Byte).toUByte()
            deviceSubClass = (B_DEVICE_SUB_CLASS[descriptor] as Byte).toUByte()
            deviceProtocol = (B_DEVICE_PROTOCOL[descriptor] as Byte).toUByte()
            endpoint0MaxPacketSize = (B_MAX_PACKET_SIZE_0[descriptor] as Byte).toUByte()
            vendorID = (ID_VENDOR[descriptor] as Short).toUShort()
            productID = (ID_PRODUCT[descriptor] as Short).toUShort()
            deviceVersion = (BCD_DEVICE[descriptor] as Short).toUShort()
            manufacturer = USBStringDescriptorIndex((I_MANUFACTURER[descriptor] as Byte).toUByte())
            product = USBStringDescriptorIndex((I_PRODUCT[descriptor] as Byte).toUByte())
            serialNumber = USBStringDescriptorIndex((I_SERIAL_NUMBER[descriptor] as Byte).toUByte())

            val numConfigurations = (B_NUM_CONFIGURATIONS[descriptor] as Byte).toUByte()
            val configDescriptorStorage = memorySession.allocate(ADDRESS)

            configurations = buildList {
                for (i in 0 until numConfigurations.toInt()) {
                    checkReturnCode(getConfigDescriptor(handle, i.toByte(), configDescriptorStorage) as Int)
                    val configDescriptor = configDescriptorStorage[ADDRESS, 0]
                    try {
                        add(USBConfiguration(this@USBDevice, configDescriptor))
                    } finally {
                        freeConfigDescriptor(configDescriptor)
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
