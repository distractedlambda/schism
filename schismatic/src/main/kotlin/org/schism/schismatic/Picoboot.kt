package org.schism.schismatic

import org.schism.bytes.ByteSink
import org.schism.bytes.HeapSegmentAllocator
import org.schism.bytes.byteSinkInto
import org.schism.bytes.takeFirst
import org.schism.bytes.takeLast
import org.schism.bytes.writeUByte
import org.schism.bytes.writeUInt
import org.schism.cousb.UsbBulkTransferInEndpoint
import org.schism.cousb.UsbBulkTransferOutEndpoint
import org.schism.cousb.UsbDevice
import java.nio.ByteOrder.LITTLE_ENDIAN

internal data class PicobootEndpoints(
    val inEndpoint: UsbBulkTransferInEndpoint,
    val outEndpoint: UsbBulkTransferOutEndpoint,
) {
    private suspend fun sendCommand(id: UByte, transferLength: UInt, encodeArgs: suspend ByteSink.() -> Unit) {
        val commandBuffer = HeapSegmentAllocator.allocate(32)

        val argsSize = byteSinkInto(commandBuffer.takeLast(16)).run {
            encodeArgs()
            countWritten
        }

        byteSinkInto(commandBuffer.takeFirst(16)).run {
            writeUInt(0x431fd10bu, LITTLE_ENDIAN)
            skip(4)
            writeUByte(id)
            writeUByte(argsSize.toUByte())
            skip(2)
            writeUInt(transferLength)
        }
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
                for (iface in configuration.interfaces) {
                    if (iface.interfaceClass != INTERFACE_CLASS) {
                        continue
                    }

                    if (iface.interfaceSubClass != INTERFACE_SUB_CLASS) {
                        continue
                    }

                    if (iface.interfaceProtocol != INTERFACE_PROTOCOL) {
                        continue
                    }

                    var inEndpoint: UsbBulkTransferInEndpoint? = null
                    var outEndpoint: UsbBulkTransferOutEndpoint? = null

                    for (endpoint in iface.endpoints) {
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

            return null
        }
    }
}

private const val VENDOR_ID: UShort = 0x2E8Au
private const val PRODUCT_ID: UShort = 0x0003u
private const val INTERFACE_CLASS: UByte = 0xFFu
private const val INTERFACE_SUB_CLASS: UByte = 0x00u
private const val INTERFACE_PROTOCOL: UByte = 0x00u
