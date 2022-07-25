package org.schism.schismatic

import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.schism.coroutines.launchWhileEachPresent
import org.schism.usb.UsbDevice

class DeviceManager(scope: CoroutineScope) {
    val connectedDevices: StateFlow<Set<ConnectedDevice>> get() = mutableConnectedDevices

    private val mutableConnectedDevices = MutableStateFlow(persistentSetOf<ConnectedDevice>())

    init {
        scope.launch {
            // UsbDevice.allDevices.launchWhileEachPresent { device ->
            //     TODO()
            // }
        }
    }

    private class ConnectedPicobootDeviceImpl(
        scope: CoroutineScope,
        override val usbDevice: UsbDevice,
        override val manufacturer: String?,
        override val product: String?,
        override val serialNumber: String?,
        val endpoints: PicobootEndpoints,
    ) : ConnectedPicobootDevice
}

sealed interface ConnectedDevice {
    val usbDevice: UsbDevice
    val manufacturer: String?
    val product: String?
    val serialNumber: String?
}

sealed interface ConnectedPicobootDevice : ConnectedDevice
