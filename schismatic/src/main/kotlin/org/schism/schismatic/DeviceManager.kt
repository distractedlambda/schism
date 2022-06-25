package org.schism.schismatic

import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.schism.coroutines.Actor
import org.schism.coroutines.launchWhileEachPresent
import org.schism.coroutines.updateMutating
import org.schism.usb.Libusb
import org.schism.usb.UsbDevice
import org.schism.usb.connect
import org.schism.usb.`interface`
import org.schism.usb.withClaim
import org.schism.util.contextual

class DeviceManager(scope: CoroutineScope) {
    val connectedDevices: StateFlow<Set<ConnectedDevice>> get() = mutableConnectedDevices

    private val mutableConnectedDevices = MutableStateFlow(persistentSetOf<ConnectedDevice>())

    init {
        scope.launch {
            Libusb.attachedDevices.launchWhileEachPresent { device ->
                PicobootEndpoints.find(device)?.let { endpoints ->
                    device.connect {
                        endpoints.inEndpoint.`interface`.withClaim {
                            coroutineScope {
                                val connectedDevice = ConnectedPicobootDeviceImpl(
                                    scope = contextual(),
                                    device,
                                    getManufacturerName(),
                                    getProductName(),
                                    getSerialNumber(),
                                    endpoints,
                                )

                                try {
                                    mutableConnectedDevices.updateMutating {
                                        add(connectedDevice)
                                    }

                                    awaitCancellation()
                                } finally {
                                    mutableConnectedDevices.updateMutating {
                                        remove(connectedDevice)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private class ConnectedPicobootDeviceImpl(
        scope: CoroutineScope,
        override val usbDevice: UsbDevice,
        override val manufacturer: String?,
        override val product: String?,
        override val serialNumber: String?,
        val endpoints: PicobootEndpoints,
    ) : Actor(scope), ConnectedPicobootDevice
}

sealed interface ConnectedDevice {
    val usbDevice: UsbDevice
    val manufacturer: String?
    val product: String?
    val serialNumber: String?
}

sealed interface ConnectedPicobootDevice : ConnectedDevice
