package org.schism.cousb

import org.schism.cousb.Libusb.ConfigDescriptor
import org.schism.cousb.Libusb.ConfigDescriptor.B_CONFIGURATION_VALUE
import org.schism.cousb.Libusb.ConfigDescriptor.B_NUM_INTERFACES
import org.schism.cousb.Libusb.ConfigDescriptor.INTERFACE
import org.schism.cousb.Libusb.ConfigDescriptor.I_CONFIGURATION
import org.schism.cousb.Libusb.ConfigDescriptor.MAX_POWER
import org.schism.cousb.Libusb.Interface.ALTSETTING
import org.schism.cousb.Libusb.Interface.NUM_ALTSETTING
import java.lang.foreign.MemoryAddress
import java.lang.foreign.ValueLayout.ADDRESS

public class USBConfiguration internal constructor(public val device: USBDevice, descriptor: MemoryAddress) {
    internal val value: UByte =
        (B_CONFIGURATION_VALUE[descriptor] as Byte).toUByte()

    public val name: USBStringDescriptorIndex =
        USBStringDescriptorIndex((I_CONFIGURATION[descriptor] as Byte).toUByte())

    public val selfPowered: Boolean =
        (ConfigDescriptor.BM_ATTRIBUTES[descriptor] as Byte).toInt() shr 6 and 1 != 0

    public val remoteWakeup: Boolean =
        (ConfigDescriptor.BM_ATTRIBUTES[descriptor] as Byte).toInt() shr 5 and 1 != 0

    public val maxPowerMilliamps: Int =
        (MAX_POWER[descriptor] as Byte).toUByte().toInt() * 2

    public val interfaces: List<USBInterface> =
        buildList {
            for (interfaceIndex in 0 until (B_NUM_INTERFACES[descriptor] as Byte).toUByte().toLong()) {
                val libusbInterface = (INTERFACE[descriptor] as MemoryAddress).getAtIndex(ADDRESS, interfaceIndex)
                val interfaceDescriptors = ALTSETTING[libusbInterface] as MemoryAddress
                for (alternateSettingIndex in 0 until (NUM_ALTSETTING[libusbInterface] as Int).toLong()) {
                    val interfaceDescriptor = interfaceDescriptors.getAtIndex(ADDRESS, alternateSettingIndex)
                    add(USBInterface(this@USBConfiguration, interfaceDescriptor))
                }
            }
        }

    public companion object
}
