package org.schism.schismatic

import org.schism.foreign.BufferEncoder
import org.schism.foreign.NativeBuffer
import org.schism.foreign.byteOffset
import org.schism.foreign.positionalDifference
import org.schism.foreign.putLeUInt
import org.schism.foreign.putUByte
import org.schism.usb.UsbBulkTransferInEndpoint
import org.schism.usb.UsbBulkTransferOutEndpoint
import org.schism.usb.UsbDevice
import org.schism.usb.UsbDeviceConnection
import org.schism.usb.receiveExact
import org.schism.usb.receiveZeroLength
import org.schism.usb.sendExact
import org.schism.usb.sendZeroLength

data class PicobootEndpoints(val inEndpoint: UsbBulkTransferInEndpoint, val outEndpoint: UsbBulkTransferOutEndpoint) {
    private suspend fun sendCommand(
        connection: UsbDeviceConnection,
        id: UByte,
        transferLength: UInt,
        fillArgs: BufferEncoder.() -> Unit,
    ) {
        NativeBuffer.withUnmanaged(32) { commandBuffer ->
            val commandSize = commandBuffer.slice(16.byteOffset).encoder().positionalDifference {
                fillArgs()
            }

            commandBuffer.encoder().run {
                putLeUInt(0x431fd10bu)
                skip(4)
                putUByte(id)
                putUByte(commandSize.toUByte())
                skip(4)
                putLeUInt(transferLength)
            }

            connection.sendExact(outEndpoint, commandBuffer)
        }
    }

    suspend fun setExclusivity(connection: UsbDeviceConnection, exclusivity: PicobootExclusivity) {
        sendCommand(connection, id = 0x01u, transferLength = 0u) {
            putUByte(exclusivity.ordinal.toUByte())
        }

        connection.receiveZeroLength(inEndpoint)
    }

    suspend fun eraseFlash(connection: UsbDeviceConnection, deviceAddress: UInt, byteCount: UInt) {
        // FIXME: bounds-check against RP2040 address space?

        require(deviceAddress % 4096u == 0u)
        require(byteCount % 4096u == 0u)
        require(byteCount != 0u)

        sendCommand(connection, id = 0x03u, transferLength = 0u) {
            putLeUInt(deviceAddress)
            putLeUInt(byteCount)
        }

        connection.receiveZeroLength(inEndpoint)
    }

    suspend fun readMemory(connection: UsbDeviceConnection, deviceAddress: UInt, destination: NativeBuffer) {
        // FIXME: bounds-check against RP2040 address space?

        require(destination.size in 1 .. UInt.MAX_VALUE.toLong())

        sendCommand(connection, id = 0x84u, transferLength = destination.size.toUInt()) {
            putLeUInt(deviceAddress)
            putLeUInt(destination.size.toUInt())
        }

        connection.receiveExact(inEndpoint, destination)
        connection.sendZeroLength(outEndpoint)
    }

    suspend fun readMemory(connection: UsbDeviceConnection, deviceAddress: UInt, byteCount: UInt): NativeBuffer {
        return NativeBuffer.allocateUninitialized(byteCount.toLong()).also {
            readMemory(connection, deviceAddress, it)
        }
    }

    suspend fun writeMemory(connection: UsbDeviceConnection, deviceAddress: UInt, data: NativeBuffer) {
        // FIXME: bounds-check against RP2040 address space?

        require(data.size in 1 .. UInt.MAX_VALUE.toLong())

        sendCommand(connection, id = 0x05u, transferLength = data.size.toUInt()) {
            putLeUInt(deviceAddress)
            putLeUInt(data.size.toUInt())
        }

        connection.sendExact(outEndpoint, data)
        connection.receiveZeroLength(inEndpoint)
    }

    suspend fun exitXip(connection: UsbDeviceConnection) {
        sendCommand(connection, id = 0x06u, transferLength = 0u) {}
        connection.receiveZeroLength(inEndpoint)
    }

    suspend fun enterXip(connection: UsbDeviceConnection) {
        sendCommand(connection, id = 0x07u, transferLength = 0u) {}
        connection.receiveZeroLength(inEndpoint)
    }

    companion object {
        fun find(device: UsbDevice): PicobootEndpoints? {
            if (device.vendorId != VENDOR_ID) {
                return null
            }

            if (device.productId != PRODUCT_ID) {
                return null
            }

            for (configuration in device.configurations) {
                for (`interface` in configuration.interfaces) {
                    for (alternateSetting in `interface`.alternateSettings) {
                        if (alternateSetting.interfaceClass != INTERFACE_CLASS) {
                            continue
                        }

                        if (alternateSetting.interfaceSubClass != INTERFACE_SUB_CLASS) {
                            continue
                        }

                        if (alternateSetting.interfaceProtocol != INTERFACE_PROTOCOL) {
                            continue
                        }

                        var inEndpoint: UsbBulkTransferInEndpoint? = null
                        var outEndpoint: UsbBulkTransferOutEndpoint? = null

                        for (endpoint in alternateSetting.endpoints) {
                            when (endpoint) {
                                is UsbBulkTransferInEndpoint -> {
                                    inEndpoint = endpoint
                                }

                                is UsbBulkTransferOutEndpoint -> {
                                    outEndpoint = endpoint
                                }

                                else -> {}
                            }
                        }

                        if (inEndpoint != null && outEndpoint != null) {
                            return PicobootEndpoints(inEndpoint, outEndpoint)
                        }
                    }
                }
            }

            return null
        }
    }
}

enum class PicobootExclusivity {
    NotExclusive,
    Exclusive,
    ExclusiveAndEject,
}

private const val VENDOR_ID: UShort = 0x2E8Au
private const val PRODUCT_ID: UShort = 0x0003u
private const val INTERFACE_CLASS: UByte = 0xFFu
private const val INTERFACE_SUB_CLASS: UByte = 0x00u
private const val INTERFACE_PROTOCOL: UByte = 0x00u
