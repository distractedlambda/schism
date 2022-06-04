@file:JvmName("Schismatic")

package org.schism.schismatic

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.schism.cousb.USBDevice
import org.schism.cousb.attachedUSBDevices

public fun main() {
    System.setProperty("apple.awt.application.appearance", "system")
    application {
        Window(title = "Schismatic", onCloseRequest = ::exitApplication) {
            MaterialTheme(colors = if (isSystemInDarkTheme()) darkColors() else lightColors()) {
                Row(Modifier.fillMaxSize()) {
                    val devices = attachedUSBDevices().collectAsState()
                    LazyColumn(contentPadding = PaddingValues(5.dp)) {
                        items(devices.value, key = USBDevice::transientID) { device ->
                            Text("${device.vendorID.toString(16).padStart(4, '0')}:${device.productID.toString(16).padStart(4, '0')}")
                        }
                    }
                }
            }
        }
    }
}
