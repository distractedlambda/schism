package org.schism.usb

import org.schism.foreign.asStructArray
import org.schism.usb.Libusb.Interface
import org.schism.usb.Libusb.InterfaceDescriptor

public class UsbInterface internal constructor(public val configuration: UsbConfiguration, native: Interface) {
    internal val number: UByte

    public val alternateSettings: List<UsbAlternateSetting>

    public val device: UsbDevice get() {
        return configuration.device
    }

    init {
        val interfaceDescriptors = native.altsetting.asStructArray(
            InterfaceDescriptor.Type,
            native.num_altsetting.toLong(),
        )

        number = interfaceDescriptors[0].bInterfaceNumber

        alternateSettings = List(native.num_altsetting) {
            UsbAlternateSetting(this, interfaceDescriptors[it.toLong()])
        }
    }
}
