package org.schism.schismatic

import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.schism.coroutines.Actor
import org.schism.coroutines.launchWhileEachPresent
import org.schism.coroutines.updateMutating
import org.schism.usb.Libusb
import org.schism.usb.UsbDevice
import org.schism.usb.connect

context (CoroutineScope) class DeviceManager {
    val connectedDevices: StateFlow<Set<ConnectedDevice>>

    init {
        connectedDevices = MutableStateFlow(persistentSetOf())

        launch {
            Libusb.attachedDevices.launchWhileEachPresent { device ->
                PicobootEndpoints.find(device)?.let { picobootEndpoints ->
                    device.connect {
                        picobootEndpoints.setExclusivity(PicobootExclusivity.Exclusive)

                        val connectedDevice = object : Actor(), ConnectedPicobootDevice {
                            override val usbDevice get() = device
                        }

                        try {
                            connectedDevices.updateMutating {
                                add(connectedDevice)
                            }

                            awaitCancellation()
                        } finally {
                            connectedDevices.updateMutating {
                                remove(connectedDevice)
                            }
                        }
                    }
                }
            }
        }
    }
}

sealed interface ConnectedDevice {
    val usbDevice: UsbDevice
}

interface ConnectedPicobootDevice : ConnectedDevice
