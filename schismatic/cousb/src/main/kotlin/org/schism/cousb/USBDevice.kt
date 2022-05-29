package org.schism.cousb

import java.lang.foreign.MemoryAddress
import java.lang.foreign.MemorySession
import java.lang.foreign.ValueLayout.ADDRESS

public class USBDevice internal constructor(handle: MemoryAddress) {
    init {
        Libusb.ref_device.invokeExact(handle) as MemoryAddress
        USBContext.cleaner.register(this) { Libusb.unref_device.invokeExact(handle) }
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
    public val configurations: Map<USBConfigurationValue, USBConfiguration>

    init {
        MemorySession.openConfined().use { memorySession ->
            val descriptor = memorySession.allocate(Libusb.device_descriptor)
            Libusb.checkReturnCode(Libusb.get_device_descriptor.invokeExact(handle, descriptor) as Int)
            usbVersion = (Libusb.device_descriptor_bcdUSB[descriptor] as Short).toUShort()
            deviceClass = (Libusb.device_descriptor_bDeviceClass[descriptor] as Byte).toUByte()
            deviceSubClass = (Libusb.device_descriptor_bDeviceSubClass[descriptor] as Byte).toUByte()
            deviceProtocol = (Libusb.device_descriptor_bDeviceProtocol[descriptor] as Byte).toUByte()
            endpoint0MaxPacketSize = (Libusb.device_descriptor_bMaxPacketSize0[descriptor] as Byte).toUByte()
            vendorID = (Libusb.device_descriptor_idVendor[descriptor] as Short).toUShort()
            productID = (Libusb.device_descriptor_idProduct[descriptor] as Short).toUShort()
            deviceVersion = (Libusb.device_descriptor_bcdDevice[descriptor] as Short).toUShort()
            manufacturer = USBStringDescriptorIndex((Libusb.device_descriptor_iManufacturer[descriptor] as Byte).toUByte())
            product = USBStringDescriptorIndex((Libusb.device_descriptor_iProduct[descriptor] as Byte).toUByte())
            serialNumber = USBStringDescriptorIndex((Libusb.device_descriptor_iSerialNumber[descriptor] as Byte).toUByte())

            val numConfigurations = (Libusb.device_descriptor_bNumConfigurations[descriptor] as Byte).toUByte()
            val configDescriptorStorage = memorySession.allocate(ADDRESS)
            configurations = buildMap {
                for (i in 0 until numConfigurations.toInt()) {
                    Libusb.checkReturnCode(Libusb.get_config_descriptor.invokeExact(handle, i.toByte(), configDescriptorStorage) as Int)
                    val configDescriptor = configDescriptorStorage[ADDRESS, 0]
                    try {
                        val configurationValue = USBConfigurationValue((Libusb.config_descriptor_bConfigurationValue[configDescriptor] as Byte).toUByte())
                        val attributes = (Libusb.config_descriptor_bmAttributes[configDescriptor] as Byte).toInt()
                        set(
                            configurationValue,
                            USBConfiguration(
                                device = this@USBDevice,
                                value = configurationValue,
                                name = USBStringDescriptorIndex((Libusb.config_descriptor_iConfiguration[configDescriptor] as Byte).toUByte()),
                                selfPowered = (attributes shr 6) and 1 != 0,
                                remoteWakeup = (attributes shr 5) and 1 != 0,
                                maxPowerMilliamps = (Libusb.config_descriptor_MaxPower[configDescriptor] as Byte).toUByte().toInt() * 2,
                                interfaces = Libusb.config_descriptor_interface[configDescriptor] as MemoryAddress,
                                interfaceCount = (Libusb.config_descriptor_bNumInterfaces[configDescriptor] as Byte).toUByte(),
                            ),
                        )
                    } finally {
                        Libusb.free_config_descriptor(configDescriptor)
                    }
                }
            }
        }
    }
}
