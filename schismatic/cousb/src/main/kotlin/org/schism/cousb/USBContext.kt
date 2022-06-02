package org.schism.cousb

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import java.lang.Thread.sleep
import java.lang.foreign.MemoryAddress
import java.lang.foreign.MemorySession
import java.lang.foreign.ValueLayout.ADDRESS
import java.lang.ref.Cleaner
import kotlin.concurrent.thread

internal object USBContext {
    val attachedDevices = MutableStateFlow<Set<USBDevice>>(linkedSetOf())

    val handle: MemoryAddress =
        MemorySession.openConfined().use {
            val handle = it.allocate(ADDRESS)
            Libusb.checkReturn(Libusb.init(handle) as Int)
            handle[ADDRESS, 0]
        }

    val cleaner: Cleaner =
        Cleaner.create { body ->
            thread(name = "libusb cleaner") {
                body.run()
            }
        }

    init {
        thread(isDaemon = true, name = "libusb event handler") {
            while (true) {
                Libusb.checkReturn(Libusb.handleEvents(handle) as Int)
            }
        }

        thread(isDaemon = true, name = "libusb device enumerator") {
            var devicesByHandle = hashMapOf<MemoryAddress, USBDevice>()

            MemorySession.openConfined().use { memorySession ->
                val listStorage = memorySession.allocate(ADDRESS)

                while (true) {
                    val listSize = Libusb.checkSize(Libusb.getDeviceList(handle, listStorage) as Long)
                    val list = listStorage[ADDRESS, 0]
                    val newDevicesByHandle = hashMapOf<MemoryAddress, USBDevice>()

                    try {
                        for (i in 0 until listSize) {
                            val deviceHandle = list.getAtIndex(ADDRESS, i)
                            newDevicesByHandle[deviceHandle] = devicesByHandle[deviceHandle] ?: USBDevice(deviceHandle)
                        }
                    } finally {
                        Libusb.freeDeviceList(list, 1)
                    }

                    devicesByHandle = newDevicesByHandle
                    attachedDevices.getAndUpdate { it + devicesByHandle.values }

                    sleep(500)
                }
            }
        }
    }
}
