package org.schism.cousb

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.getAndUpdate
import java.lang.Thread.sleep
import java.lang.foreign.MemoryAddress
import java.lang.foreign.MemorySession
import java.lang.foreign.ValueLayout.ADDRESS
import java.lang.ref.Cleaner
import kotlin.concurrent.thread

internal object USBContext {
    private val mutableAttachedDevices = MutableStateFlow<Set<USBDevice>>(linkedSetOf())

    val attachedDevices: StateFlow<Set<USBDevice>> get() = mutableAttachedDevices

    val handle = MemorySession.openConfined().use {
        val handle = it.allocate(ADDRESS)
        Libusb.checkReturnCode(Libusb.init.invokeExact(handle) as Int)
        handle[ADDRESS, 0]
    }

    val cleaner = Cleaner.create { body ->
        thread(name = "libusb context cleaner") {
            body.run()
        }
    }

    init {
        thread(isDaemon = true, name = "libusb context event handler") {
            while (true) {
                Libusb.checkReturnCode(Libusb.handle_events.invokeExact(handle) as Int)
            }
        }

        thread(isDaemon = true, name = "libusb context device enumerator") {
            var devicesByHandle = hashMapOf<MemoryAddress, USBDevice>()

            MemorySession.openConfined().use { memorySession ->
                val listStorage = memorySession.allocate(ADDRESS)

                while (true) {
                    val listSize = Libusb.checkSize(Libusb.get_device_list.invokeExact(handle, listStorage) as Long)
                    val list = listStorage[ADDRESS, 0]
                    val newDevicesByHandle = hashMapOf<MemoryAddress, USBDevice>()

                    try {
                        for (i in 0 until listSize) {
                            val deviceHandle = list.getAtIndex(ADDRESS, i)
                            newDevicesByHandle[deviceHandle] = devicesByHandle[deviceHandle] ?: USBDevice(deviceHandle)
                        }
                    } finally {
                        Libusb.free_device_list.invokeExact(list, 1)
                    }

                    devicesByHandle = newDevicesByHandle
                    mutableAttachedDevices.getAndUpdate { it + devicesByHandle.values }

                    sleep(500)
                }
            }
        }
    }
}
