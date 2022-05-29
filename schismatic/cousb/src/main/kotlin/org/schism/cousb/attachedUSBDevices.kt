package org.schism.cousb

import kotlinx.coroutines.flow.StateFlow

public val attachedUSBDevices: StateFlow<Set<USBDevice>> get() = USBContext.attachedDevices
