package org.schism.schismatic

import kotlinx.coroutines.coroutineScope
import org.schism.coroutines.Actor
import org.schism.memory.Memory
import org.schism.memory.MemoryEncoder
import org.schism.memory.positionalDifference
import org.schism.memory.putLeUInt
import org.schism.memory.putUByte
import org.schism.usb.UsbBulkTransferInEndpoint
import org.schism.usb.UsbBulkTransferOutEndpoint
import org.schism.usb.UsbDevice
import org.schism.usb.UsbDeviceConnection
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class PicobootConnection private constructor(
    private val actor: Actor,
    private val deviceConnection: UsbDeviceConnection,
    private val inEndpoint: UsbBulkTransferInEndpoint,
    private val outEndpoint: UsbBulkTransferOutEndpoint,
) {
    @OptIn(ExperimentalContracts::class)
    private suspend fun sendCommand(id: UByte, transferLength: UInt, fillArgs: MemoryEncoder.() -> Unit) {
        contract {
            callsInPlace(fillArgs, InvocationKind.EXACTLY_ONCE)
        }

        deviceConnection.sendPacket(outEndpoint) { packet ->
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

    suspend fun setExclusivity(exclusivity: PicobootExclusivity) {
        actor.within {
            sendCommand(id = 0x01u, transferLength = 0u) {
                putUByte(exclusivity.ordinal.toUByte())
            }

            deviceConnection.receiveZeroLengthPacket(inEndpoint)
        }
    }

    suspend fun enterXip() {
        actor.within {
            sendCommand(id = 0x06u, transferLength = 0u) {}
            deviceConnection.receiveZeroLengthPacket(inEndpoint)
        }
    }

    suspend fun exitXip() {
        actor.within {
            sendCommand(id = 0x07u, transferLength = 0u) {}
            deviceConnection.receiveZeroLengthPacket(inEndpoint)
        }
    }

    suspend fun eraseFlash(deviceAddress: UInt, byteCount: UInt) {
        require(deviceAddress % 4096u == 0u)
        require(byteCount % 4096u == 0u)
        require(byteCount != 0u)

        actor.within {
            sendCommand(id = 0x03u, transferLength = 0u) {
                putLeUInt(deviceAddress)
                putLeUInt(byteCount)
            }

            deviceConnection.receiveZeroLengthPacket(inEndpoint)
        }
    }

    @OptIn(ExperimentalContracts::class)
    suspend fun readMemory(deviceAddress: UInt, size: UInt, onEachPart: suspend (Memory) -> Unit) {
        contract {
            callsInPlace(onEachPart, InvocationKind.AT_LEAST_ONCE)
        }

        require(size >= 1u)

        actor.within {
            sendCommand(id = 0x84u, transferLength = size) {
                putLeUInt(deviceAddress)
                putLeUInt(size)
            }

            var countReceived = 0L

            while (countReceived < size.toLong()) {
                deviceConnection.receivePacket(inEndpoint) {
                    countReceived += it.size
                    onEachPart(it)
                }
            }

            deviceConnection.sendZeroLengthPacket(outEndpoint)
        }
    }

    @OptIn(ExperimentalContracts::class)
    suspend fun writeMemory(deviceAddress: UInt, size: UInt, writePart: suspend (Memory) -> Unit) {
        contract {
            callsInPlace(writePart, InvocationKind.AT_LEAST_ONCE)
        }

        require(size >= 1u)

        actor.within {
            sendCommand(id = 0x05u, transferLength = size) {
                putLeUInt(deviceAddress)
                putLeUInt(size)
            }

            var countRemaining = size.toLong()

            while (countRemaining > 0) {
                deviceConnection.sendPacket(outEndpoint) { memory ->
                    minOf(memory.size, countRemaining).also { packetSize ->
                        countRemaining -= packetSize
                        writePart(memory.slice(size = packetSize))
                    }
                }
            }

            deviceConnection.receiveZeroLengthPacket(inEndpoint)
        }
    }

    companion object {
        @OptIn(ExperimentalContracts::class)
        suspend fun <R> open(endpoints: PicobootEndpoints, block: suspend (PicobootConnection) -> R): R {
            contract {
                callsInPlace(block, InvocationKind.EXACTLY_ONCE)
            }

            return endpoints.inEndpoint.device.connect { deviceConnection ->
                deviceConnection.withClaim(endpoints.inEndpoint.iface) {
                    coroutineScope {
                        block(
                            PicobootConnection(
                                Actor(),
                                deviceConnection,
                                endpoints.inEndpoint,
                                endpoints.outEndpoint,
                            )
                        )
                    }
                }
            }
        }
    }
}

class PicobootEndpoints private constructor(
    val inEndpoint: UsbBulkTransferInEndpoint,
    val outEndpoint: UsbBulkTransferOutEndpoint,
) {
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
