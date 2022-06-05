@file:JvmName("Schismatic")

package org.schism.schismatic

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.schism.cousb.UsbDevice
import org.schism.cousb.attachedUsbDevices

public fun main() {
    System.setProperty("apple.awt.application.appearance", "system")
    application { MainWindow() }
}

@Composable
private fun ApplicationScope.MainWindow() {
    Window(title = "Schismatic", onCloseRequest = ::exitApplication) {
        SchismMaterialTheme {
            Surface(color = MaterialTheme.colors.background) {
                Row(Modifier.fillMaxSize()) {
                    DeviceList()
                }
            }
        }
    }
}

@Composable
private fun DeviceList() {
    val devices = attachedUsbDevices().collectAsState()
    Surface(color = MaterialTheme.colors.primarySurface, elevation = 1.dp) {
        LazyColumn(Modifier.fillMaxHeight(), contentPadding = PaddingValues(5.dp)) {
            items(devices.value, key = UsbDevice::transientID) { device ->
                DeviceListEntry(device)
            }
        }
    }
}

@Composable
private fun DeviceListEntry(device: UsbDevice) {
    Column(Modifier.padding(5.dp)) {
        Text(device.knownProductName ?: "Unknown Device")
        Text(device.knownVendorName ?: "Unknown Vendor")
    }
}
