@file:JvmName("Schismatic")

package org.schism.schismatic

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.schism.usb.Libusb
import org.schism.usb.UsbDevice

fun main() {
    System.setProperty("apple.awt.application.appearance", "system")
    application { MainWindow() }
}

@Composable
private fun ApplicationScope.MainWindow() {
    Window(title = "Schismatic", onCloseRequest = ::exitApplication) {
        SchismaticMaterialTheme {
            Surface(color = MaterialTheme.colors.background) {
                val selectedDevice = remember { mutableStateOf<UsbDevice?>(null) }
                Row {
                    DeviceList(selectedDevice)
                    DeviceContent(selectedDevice.value)
                }
            }
        }
    }
}

@Composable
private fun DeviceList(selectedDevice: MutableState<UsbDevice?>) {
    val devices = Libusb.attachedDevices.collectAsState()

    Surface(
        Modifier.width(300.dp),
        color = MaterialTheme.colors.primarySurface,
        elevation = 1.dp,
    ) {
        LazyColumn(Modifier.fillMaxSize()) {
            /// /items(devices.value, key = UsbDevice::transientID) { device ->
            /// /    DeviceListEntry(device, selectedDevice)
            /// /}
        }
    }
}

@Composable
private fun DeviceListEntry(device: UsbDevice, selectedDevice: MutableState<UsbDevice?>) {
    val isSelected = device == selectedDevice.value

    @OptIn(ExperimentalMaterialApi::class) Surface(
        modifier = Modifier.fillMaxSize(),
        onClick = { selectedDevice.value = device },
        elevation = if (isSelected) 2.dp else 0.dp,
    ) {
        // Column {
        //     Text(
        //         device.knownProductName ?: "Unknown Device",
        //         style = MaterialTheme.typography.h6,
        //     )

        //     Row {
        //         fun hex4(value: UShort): String {
        //             return value.toString(16).padStart(4, '0')
        //         }

        //         Text("${hex4(device.vendorId)}:${hex4(device.productId)}")

        //         device.knownVendorName?.let {
        //             Text(it)
        //         }
        //     }
        // }
    }
}

@Composable
private fun DeviceContent(selectedDevice: UsbDevice?) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Hi")
    }
}
