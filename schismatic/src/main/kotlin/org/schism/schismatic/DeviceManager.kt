package org.schism.schismatic

import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.schism.coroutines.Actor
import org.schism.coroutines.launchWhileEachPresent
import kotlinx.collections.immutable.plus
import kotlinx.collections.immutable.minus
import org.schism.usb.Libusb
import org.schism.usb.connect
import org.schism.util.contextual

class DeviceManager(private val scope: CoroutineScope) {
    val connectedDevices: StateFlow<Set<ConnectedDevice>>

    init {
        connectedDevices = MutableStateFlow(persistentSetOf())

        scope.launch {
            Libusb.attachedDevices.launchWhileEachPresent { device ->
                PicobootEndpoints.find(device)?.let { picobootEndpoints ->
                    device.connect {
                        picobootEndpoints.setExclusivity(PicobootExclusivity.Exclusive)

                        val connectedDevice = object : Actor(contextual<CoroutineScope>()), ConnectedPicobootDevice {

                        }

                        try {
                            connectedDevices.update { it + connectedDevice }
                            awaitCancellation()
                        } finally {
                            connectedDevices.update { it - connectedDevice }
                        }
                    }
                }
            }
        }
    }
}

sealed interface ConnectedDevice

interface ConnectedPicobootDevice : ConnectedDevice
