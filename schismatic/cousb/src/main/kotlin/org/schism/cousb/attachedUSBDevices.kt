package org.schism.cousb

import kotlinx.coroutines.flow.StateFlow

public fun attachedUSBDevices(): StateFlow<List<USBDevice>> =
    USBContext.attachedDevices
