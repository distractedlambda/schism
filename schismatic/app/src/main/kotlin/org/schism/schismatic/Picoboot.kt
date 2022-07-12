package org.schism.schismatic

import org.schism.memory.Memory
import org.schism.memory.MemoryEncoder
import org.schism.memory.positionalDifference
import org.schism.memory.putLeUInt
import org.schism.memory.putUByte
import org.schism.usb.UsbBulkTransferInEndpoint
import org.schism.usb.UsbBulkTransferOutEndpoint
import org.schism.usb.UsbDevice
import org.schism.usb.UsbDeviceConnection

data class PicobootEndpoints(val inEndpoint: UsbBulkTransferInEndpoint, val outEndpoint: UsbBulkTransferOutEndpoint) {
    private suspend fun sendCommand(
        connection: UsbDeviceConnection,
        id: UByte,
        transferLength: UInt,
        fillArgs: MemoryEncoder.() -> Unit,
    ) {
        connection.sendPacket(outEndpoint) { packet ->
            val commandSize = packet.slice(offset = 16, size = 16).encoder().positionalDifference {
                fillArgs()
            }

            packet.slice(size = 16).encoder().run {
                putLeUInt(0x431fd10bu)
                putInt(0)
                putUByte(id)
                putUByte(commandSize.toUByte())
                putInt(0)
                putLeUInt(transferLength)
            }

            32
        }
    }

    suspend fun setExclusivity(connection: UsbDeviceConnection, exclusivity: PicobootExclusivity) {
        sendCommand(connection, id = 0x01u, transferLength = 0u) {
            putUByte(exclusivity.ordinal.toUByte())
        }

        connection.receiveZeroLengthPacket(inEndpoint)
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

        connection.receiveZeroLengthPacket(inEndpoint)
    }

    suspend fun readMemory(connection: UsbDeviceConnection, deviceAddress: UInt, destination: Memory) {
        // FIXME: bounds-check against RP2040 address space?

        TODO()
        // require(destination.size in 1 .. UInt.MAX_VALUE.toLong())

        // sendCommand(connection, id = 0x84u, transferLength = destination.size.toUInt()) {
        //     putLeUInt(deviceAddress)
        //     putLeUInt(destination.size.toUInt())
        // }

        // connection.receive(inEndpoint, destination)
        // connection.sendZeroLength(outEndpoint)
    }

    suspend fun writeMemory(connection: UsbDeviceConnection, deviceAddress: UInt, data: Memory) {
        // FIXME: bounds-check against RP2040 address space?

        TODO()

        // require(data.size in 1 .. UInt.MAX_VALUE.toLong())

        // sendCommand(connection, id = 0x05u, transferLength = data.size.toUInt()) {
        //     putLeUInt(deviceAddress)
        //     putLeUInt(data.size.toUInt())
        // }

        // connection.sendExact(outEndpoint, data)
        // connection.receiveZeroLength(inEndpoint)
    }

    suspend fun exitXip(connection: UsbDeviceConnection) {
        sendCommand(connection, id = 0x06u, transferLength = 0u) {}
        connection.receiveZeroLengthPacket(inEndpoint)
    }

    suspend fun enterXip(connection: UsbDeviceConnection) {
        sendCommand(connection, id = 0x07u, transferLength = 0u) {}
        connection.receiveZeroLengthPacket(inEndpoint)
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
