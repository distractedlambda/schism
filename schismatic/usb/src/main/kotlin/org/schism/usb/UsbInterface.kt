package org.schism.usb

public class UsbInterface internal constructor(
    public val configuration: UsbConfiguration,
) {
    public val alternateSettings: List<UsbAlternateSetting>
}
