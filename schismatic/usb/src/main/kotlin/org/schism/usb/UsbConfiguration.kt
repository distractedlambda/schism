package org.schism.usb

import org.schism.bytes.elementSegmentList
import org.schism.bytes.sequence
import org.schism.usb.Libusb.ConfigDescriptor
import org.schism.usb.Libusb.Interface
import org.schism.usb.Libusb.InterfaceDescriptor
import java.lang.foreign.MemoryAddress
import java.lang.foreign.MemorySegment

public class UsbConfiguration internal constructor(public val device: UsbDevice, descriptor: MemorySegment) {
    internal val value: UByte =
        (ConfigDescriptor.B_CONFIGURATION_VALUE[descriptor] as Byte).toUByte()

    public val name: UsbStringDescriptorIndex =
        UsbStringDescriptorIndex((ConfigDescriptor.I_CONFIGURATION[descriptor] as Byte).toUByte())

    public val selfPowered: Boolean =
        (ConfigDescriptor.BM_ATTRIBUTES[descriptor] as Byte).toInt() shr 6 and 1 != 0

    public val remoteWakeup: Boolean =
        (ConfigDescriptor.BM_ATTRIBUTES[descriptor] as Byte).toInt() shr 5 and 1 != 0

    public val maxPowerMilliamps: Int =
        (ConfigDescriptor.MAX_POWER[descriptor] as Byte).toUByte().toInt() * 2

    public val interfaces: List<UsbInterface> =
        buildList {
            Interface.LAYOUT
                .sequence((ConfigDescriptor.B_NUM_INTERFACES[descriptor] as Byte).toUByte().toLong())
                .elementSegmentList(ConfigDescriptor.INTERFACE[descriptor] as MemoryAddress)
                .forEach { iface ->
                    InterfaceDescriptor.LAYOUT
                        .sequence((Interface.NUM_ALTSETTING[iface] as Int).toLong())
                        .elementSegmentList(Interface.ALTSETTING[iface] as MemoryAddress)
                        .forEach { interfaceDescriptor ->
                            add(UsbInterface(this@UsbConfiguration, interfaceDescriptor))
                        }
                }
        }

    public companion object
}
