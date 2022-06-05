package org.schism.cousb

import org.schism.bytes.newConfinedMemorySession
import org.schism.bytes.segment
import org.schism.cousb.Libusb.ConfigDescriptor
import org.schism.cousb.Libusb.DeviceDescriptor
import java.lang.foreign.MemoryAddress
import java.lang.foreign.ValueLayout.ADDRESS
import java.util.concurrent.atomic.AtomicLong
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind.EXACTLY_ONCE
import kotlin.contracts.contract

public class UsbDevice internal constructor(handle: MemoryAddress) {
    init {
        Libusb.refDevice(handle) as MemoryAddress
        UsbContext.cleaner.register(this) { Libusb.unrefDevice(handle) }
    }

    internal val handle = handle

    public val transientID: ULong = nextID.getAndIncrement().toULong()

    public val usbVersion: UShort
    public val deviceClass: UByte
    public val deviceSubClass: UByte
    public val deviceProtocol: UByte
    public val endpoint0MaxPacketSize: UByte
    public val vendorId: UShort
    public val productId: UShort
    public val deviceVersion: UShort
    public val manufacturer: UsbStringDescriptorIndex
    public val product: UsbStringDescriptorIndex
    public val serialNumber: UsbStringDescriptorIndex
    public val configurations: List<UsbConfiguration>

    init {
        newConfinedMemorySession().use { memorySession ->
            val descriptor = memorySession.allocate(DeviceDescriptor.LAYOUT)
            Libusb.checkReturn(Libusb.getDeviceDescriptor(handle, descriptor) as Int)

            usbVersion = (DeviceDescriptor.BCD_USB[descriptor] as Short).toUShort()
            deviceClass = (DeviceDescriptor.B_DEVICE_CLASS[descriptor] as Byte).toUByte()
            deviceSubClass = (DeviceDescriptor.B_DEVICE_SUB_CLASS[descriptor] as Byte).toUByte()
            deviceProtocol = (DeviceDescriptor.B_DEVICE_PROTOCOL[descriptor] as Byte).toUByte()
            endpoint0MaxPacketSize = (DeviceDescriptor.B_MAX_PACKET_SIZE_0[descriptor] as Byte).toUByte()
            vendorId = (DeviceDescriptor.ID_VENDOR[descriptor] as Short).toUShort()
            productId = (DeviceDescriptor.ID_PRODUCT[descriptor] as Short).toUShort()
            deviceVersion = (DeviceDescriptor.BCD_DEVICE[descriptor] as Short).toUShort()
            manufacturer = UsbStringDescriptorIndex((DeviceDescriptor.I_MANUFACTURER[descriptor] as Byte).toUByte())
            product = UsbStringDescriptorIndex((DeviceDescriptor.I_PRODUCT[descriptor] as Byte).toUByte())
            serialNumber = UsbStringDescriptorIndex((DeviceDescriptor.I_SERIAL_NUMBER[descriptor] as Byte).toUByte())

            val numConfigurations = (DeviceDescriptor.B_NUM_CONFIGURATIONS[descriptor] as Byte).toUByte()
            val configDescriptorStorage = memorySession.allocate(ADDRESS)

            configurations = buildList {
                for (i in 0 until numConfigurations.toInt()) {
                    Libusb.checkReturn(Libusb.getConfigDescriptor(handle, i.toByte(), configDescriptorStorage) as Int)
                    val configDescriptor = configDescriptorStorage[ADDRESS, 0]
                    try {
                        add(
                            UsbConfiguration(
                                this@UsbDevice,
                                ConfigDescriptor.LAYOUT.segment(configDescriptor),
                            )
                        )
                    } finally {
                        Libusb.freeConfigDescriptor(configDescriptor)
                    }
                }
            }
        }
    }

    public val knownVendorName: String?
        get() = UsbIds.vendorNames[vendorId]

    public val knownProductName: String?
        get() = UsbIds.productNames[vendorId.toUInt() shl 16 or productId.toUInt()]

    @OptIn(ExperimentalContracts::class)
    public suspend inline fun <R> connect(block: (UsbDeviceConnection) -> R): R {
        contract {
            callsInPlace(block, EXACTLY_ONCE)
        }

        val connection = UsbDeviceConnection(this@UsbDevice)

        try {
            return block(connection)
        } finally {
            connection.close()
        }
    }

    public companion object {
        private val nextID = AtomicLong()
    }
}
