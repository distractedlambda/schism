package org.schism.cousb

import kotlinx.coroutines.flow.MutableStateFlow
import org.schism.bytes.newConfinedMemorySession
import java.lang.Thread.sleep
import java.lang.foreign.MemoryAddress
import java.lang.foreign.ValueLayout.ADDRESS
import java.lang.ref.Cleaner
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success
import kotlin.concurrent.thread

internal object USBContext {
    val attachedDevices = MutableStateFlow<List<USBDevice>>(emptyList())

    val handle: MemoryAddress =
        newConfinedMemorySession().use {
            val handle = it.allocate(ADDRESS)
            Libusb.checkReturn(Libusb.init(handle) as Int)
            handle[ADDRESS, 0]
        }

    val cleaner: Cleaner =
        Cleaner.create { body ->
            thread(name = "libusb cleaner", start = false) {
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
            var devicesByHandle = hashMapOf<MemoryAddress, Result<USBDevice>>()

            newConfinedMemorySession().use { memorySession ->
                val listStorage = memorySession.allocate(ADDRESS)

                while (true) {
                    val listSize = Libusb.checkSize(Libusb.getDeviceList(handle, listStorage) as Long)
                    val list = listStorage[ADDRESS, 0]
                    val newDevicesByHandle = hashMapOf<MemoryAddress, Result<USBDevice>>()

                    try {
                        for (i in 0 until listSize) {
                            val deviceHandle = list.getAtIndex(ADDRESS, i)
                            newDevicesByHandle[deviceHandle] = devicesByHandle[deviceHandle]
                                ?: try {
                                    success(USBDevice(deviceHandle))
                                } catch (exception: LibusbErrorException) {
                                    failure(exception)
                                }
                        }
                    } finally {
                        Libusb.freeDeviceList(list, 1)
                    }

                    devicesByHandle = newDevicesByHandle

                    attachedDevices.value = devicesByHandle.values
                        .mapNotNull { it.getOrNull() }
                        .sortedBy(USBDevice::transientID)

                    sleep(500)
                }
            }
        }
    }
}
