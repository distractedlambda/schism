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
import org.schism.usb.UsbDeviceConnection
import org.schism.usb.connect
import org.schism.util.contextual

context (CoroutineScope)
class DeviceManager {
    val connectedDevices: StateFlow<Set<ConnectedDevice>>
        get() = mutableConnectedDevices

    private val mutableConnectedDevices = MutableStateFlow(persistentSetOf<ConnectedDevice>())

    init {
        launch {
            Libusb.attachedDevices.launchWhileEachPresent { device ->
                val factory: ConnectedDeviceFactory = run {
                    PicobootEndpoints.find(device)?.let { endpoints ->
                        return@run { manufacturer, product, serialNumber ->
                            endpoints.setExclusivity(PicobootExclusivity.Exclusive)
                            ConnectedPicobootDeviceImpl(device, manufacturer, product, serialNumber, endpoints)
                        }
                    }

                    return@launchWhileEachPresent
                }

                device.connect {
                    val connectedDevice = factory(
                        contextual(),
                        getManufacturerName(),
                        getProductName(),
                        getSerialNumber(),
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

    context (CoroutineScope, UsbDeviceConnection)
    private class ConnectedPicobootDeviceImpl(
        override val usbDevice: UsbDevice,
        override val manufacturer: String?,
        override val product: String?,
        override val serialNumber: String?,
        val endpoints: PicobootEndpoints,
    ) : Actor(), ConnectedDevice
}

sealed interface ConnectedDevice {
    val usbDevice: UsbDevice
    val manufacturer: String?
    val product: String?
    val serialNumber: String?
}

sealed interface ConnectedPicobootDevice : ConnectedDevice

private typealias ConnectedDeviceFactory = suspend context (UsbDeviceConnection) (
    manufacturer: String?,
    product: String?,
    serialNumber: String?,
) -> ConnectedDevice
