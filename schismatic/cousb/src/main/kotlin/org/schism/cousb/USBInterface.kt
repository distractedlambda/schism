package org.schism.cousb

import org.schism.cousb.Libusb.EndpointDescriptor
import org.schism.cousb.Libusb.EndpointDescriptor.B_ENDPOINT_ADDRESS
import org.schism.cousb.Libusb.EndpointDescriptor.W_MAX_PACKET_SIZE
import org.schism.cousb.Libusb.InterfaceDescriptor.B_ALTERNATE_SETTING
import org.schism.cousb.Libusb.InterfaceDescriptor.B_INTERFACE_CLASS
import org.schism.cousb.Libusb.InterfaceDescriptor.B_INTERFACE_NUMBER
import org.schism.cousb.Libusb.InterfaceDescriptor.B_INTERFACE_PROTOCOL
import org.schism.cousb.Libusb.InterfaceDescriptor.B_INTERFACE_SUB_CLASS
import org.schism.cousb.Libusb.InterfaceDescriptor.B_NUM_ENDPOINTS
import org.schism.cousb.Libusb.InterfaceDescriptor.ENDPOINT
import org.schism.cousb.Libusb.InterfaceDescriptor.I_INTERFACE
import java.lang.foreign.MemoryAddress
import java.lang.foreign.ValueLayout.ADDRESS

public class USBInterface internal constructor(public val configuration: USBConfiguration, descriptor: MemoryAddress) {
    internal val number: UByte =
        (B_INTERFACE_NUMBER[descriptor] as Byte).toUByte()

    internal val alternateSetting: UByte =
        (B_ALTERNATE_SETTING[descriptor] as Byte).toUByte()

    public val interfaceClass: UByte =
        (B_INTERFACE_CLASS[descriptor] as Byte).toUByte()

    public val interfaceSubClass: UByte =
        (B_INTERFACE_SUB_CLASS[descriptor] as Byte).toUByte()

    public val interfaceProtocol: UByte =
        (B_INTERFACE_PROTOCOL[descriptor] as Byte).toUByte()

    public val name: USBStringDescriptorIndex =
        USBStringDescriptorIndex((I_INTERFACE[descriptor] as Byte).toUByte())

    public val endpoints: List<USBEndpoint> =
        buildList {
            val endpointDescriptors = ENDPOINT[descriptor] as MemoryAddress
            for (i in 0 until (B_NUM_ENDPOINTS[descriptor] as Byte).toLong()) {
                val endpointDescriptor = endpointDescriptors.getAtIndex(ADDRESS, i)
                val address = (B_ENDPOINT_ADDRESS[endpointDescriptor] as Byte).toUByte()
                val attributes = (EndpointDescriptor.BM_ATTRIBUTES[endpointDescriptor] as Byte).toInt()
                val maxPacketSize = (W_MAX_PACKET_SIZE[endpointDescriptor] as Short).toUShort()
                add(when (attributes and 0b11) {
                    0 -> USBControlEndpoint(this@USBInterface, address, maxPacketSize)
                    1 -> continue // TODO: isochronous endpoints
                    2 -> when (address.toUInt() shr 7) {
                        0u -> USBBulkTransferOutEndpoint(this@USBInterface, address, maxPacketSize)
                        1u -> USBBulkTransferOutEndpoint(this@USBInterface, address, maxPacketSize)
                        else -> throw AssertionError()
                    }
                    3 -> continue // TODO: interrupt endpoints
                    else -> throw AssertionError()
                })
            }
        }

    public val device: USBDevice
        get() = configuration.device

    public companion object
}
