package org.schism.usb

import org.schism.bytes.elementSegmentList
import org.schism.bytes.sequence
import org.schism.usb.Libusb.EndpointDescriptor
import org.schism.usb.Libusb.InterfaceDescriptor
import java.lang.foreign.MemoryAddress
import java.lang.foreign.MemorySegment

public class UsbInterface internal constructor(public val configuration: UsbConfiguration, descriptor: MemorySegment) {
    internal val number: UByte =
        (InterfaceDescriptor.B_INTERFACE_NUMBER[descriptor] as Byte).toUByte()

    public val alternateSetting: UByte =
        (InterfaceDescriptor.B_ALTERNATE_SETTING[descriptor] as Byte).toUByte()

    public val interfaceClass: UByte =
        (InterfaceDescriptor.B_INTERFACE_CLASS[descriptor] as Byte).toUByte()

    public val interfaceSubClass: UByte =
        (InterfaceDescriptor.B_INTERFACE_SUB_CLASS[descriptor] as Byte).toUByte()

    public val interfaceProtocol: UByte =
        (InterfaceDescriptor.B_INTERFACE_PROTOCOL[descriptor] as Byte).toUByte()

    public val name: UsbStringDescriptorIndex =
        UsbStringDescriptorIndex((InterfaceDescriptor.I_INTERFACE[descriptor] as Byte).toUByte())

    public val endpoints: List<UsbEndpoint> =
        buildList {
            EndpointDescriptor.LAYOUT
                .sequence((InterfaceDescriptor.B_NUM_ENDPOINTS[descriptor] as Byte).toUByte().toLong())
                .elementSegmentList(InterfaceDescriptor.ENDPOINT[descriptor] as MemoryAddress)
                .forEach { endpointDescriptor ->
                    val address = (EndpointDescriptor.B_ENDPOINT_ADDRESS[endpointDescriptor] as Byte).toUByte()
                    val attributes = (EndpointDescriptor.BM_ATTRIBUTES[endpointDescriptor] as Byte).toInt()
                    val maxPacketSize = (EndpointDescriptor.W_MAX_PACKET_SIZE[endpointDescriptor] as Short).toUShort()
                    add(when (attributes and 0b11) {
                        0 -> UsbControlEndpoint(this@UsbInterface, address, maxPacketSize)
                        1 -> return@forEach // TODO: isochronous endpoints
                        2 -> when (address.toUInt() shr 7) {
                            0u -> UsbBulkTransferOutEndpoint(this@UsbInterface, address, maxPacketSize)
                            1u -> UsbBulkTransferOutEndpoint(this@UsbInterface, address, maxPacketSize)
                            else -> throw AssertionError()
                        }
                        3 -> return@forEach // TODO: interrupt endpoints
                        else -> throw AssertionError()
                    })
                }
        }

    public val device: UsbDevice
        get() = configuration.device

    public companion object
}
