package org.schism.cousb

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.suspendCancellableCoroutine
import org.schism.cousb.Libusb.Transfer
import org.schism.cousb.Libusb.TransferStatus
import org.schism.cousb.Libusb.allocTransfer
import org.schism.cousb.Libusb.cancelTransfer
import org.schism.cousb.Libusb.checkReturnCode
import org.schism.cousb.Libusb.checkSize
import org.schism.cousb.Libusb.freeDeviceList
import org.schism.cousb.Libusb.freeTransfer
import org.schism.cousb.Libusb.getDeviceList
import org.schism.cousb.Libusb.handleEvents
import org.schism.cousb.Libusb.init
import org.schism.cousb.Libusb.submitTransfer
import java.lang.Thread.sleep
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker
import java.lang.foreign.MemoryAddress
import java.lang.foreign.MemorySegment
import java.lang.foreign.MemorySession
import java.lang.foreign.ValueLayout.ADDRESS
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType.methodType
import java.lang.ref.Cleaner
import java.lang.ref.Reference.reachabilityFence
import java.util.concurrent.ConcurrentHashMap
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success
import kotlin.concurrent.thread
import kotlin.coroutines.Continuation
import kotlin.coroutines.resumeWithException

internal object USBContext {
    val attachedDevices = MutableStateFlow<Set<USBDevice>>(linkedSetOf())

    val handle: MemoryAddress =
        MemorySession.openConfined().use {
            val handle = it.allocate(ADDRESS)
            checkReturnCode(init(handle) as Int)
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
                checkReturnCode(handleEvents(handle) as Int)
            }
        }

        thread(isDaemon = true, name = "libusb device enumerator") {
            var devicesByHandle = hashMapOf<MemoryAddress, USBDevice>()

            MemorySession.openConfined().use { memorySession ->
                val listStorage = memorySession.allocate(ADDRESS)

                while (true) {
                    val listSize = checkSize(getDeviceList(handle, listStorage) as Long)
                    val list = listStorage[ADDRESS, 0]
                    val newDevicesByHandle = hashMapOf<MemoryAddress, USBDevice>()

                    try {
                        for (i in 0 until listSize) {
                            val deviceHandle = list.getAtIndex(ADDRESS, i)
                            newDevicesByHandle[deviceHandle] = devicesByHandle[deviceHandle] ?: USBDevice(deviceHandle)
                        }
                    } finally {
                        freeDeviceList(list, 1)
                    }

                    devicesByHandle = newDevicesByHandle
                    attachedDevices.getAndUpdate { it + devicesByHandle.values }

                    sleep(500)
                }
            }
        }
    }
}
