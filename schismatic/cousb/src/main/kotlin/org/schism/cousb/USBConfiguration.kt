package org.schism.cousb

import org.schism.cousb.Libusb.ConfigDescriptor
import org.schism.cousb.Libusb.Interface
import java.lang.foreign.MemoryAddress
import java.lang.foreign.ValueLayout.ADDRESS

public class USBConfiguration internal constructor(public val device: USBDevice, descriptor: MemoryAddress) {
    internal val value: UByte =
        (ConfigDescriptor.B_CONFIGURATION_VALUE[descriptor] as Byte).toUByte()

    public val name: USBStringDescriptorIndex =
        USBStringDescriptorIndex((ConfigDescriptor.I_CONFIGURATION[descriptor] as Byte).toUByte())

    public val selfPowered: Boolean =
        (ConfigDescriptor.BM_ATTRIBUTES[descriptor] as Byte).toInt() shr 6 and 1 != 0

    public val remoteWakeup: Boolean =
        (ConfigDescriptor.BM_ATTRIBUTES[descriptor] as Byte).toInt() shr 5 and 1 != 0

    public val maxPowerMilliamps: Int =
        (ConfigDescriptor.MAX_POWER[descriptor] as Byte).toUByte().toInt() * 2

    public val interfaces: List<USBInterface> =
        buildList {
            for (ifaceIndex in 0 until (ConfigDescriptor.B_NUM_INTERFACES[descriptor] as Byte).toUByte().toLong()) {
                val iface = (ConfigDescriptor.INTERFACE[descriptor] as MemoryAddress).getAtIndex(ADDRESS, ifaceIndex)
                val ifaceDescriptors = Interface.ALTSETTING[iface] as MemoryAddress
                for (altsettingIndex in 0 until (Interface.NUM_ALTSETTING[iface] as Int).toLong()) {
                    add(USBInterface(this@USBConfiguration, ifaceDescriptors.getAtIndex(ADDRESS, altsettingIndex)))
                }
            }
        }

    public companion object
}
