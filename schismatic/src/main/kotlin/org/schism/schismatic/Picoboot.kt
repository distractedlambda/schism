package org.schism.schismatic

import org.schism.usb.UsbBulkTransferInEndpoint
import org.schism.usb.UsbBulkTransferOutEndpoint

internal data class PicobootEndpoints(
    val inEndpoint: UsbBulkTransferInEndpoint,
    val outEndpoint: UsbBulkTransferOutEndpoint,
) {
    // private fun UsbDeviceConnection.sendCommand(id: UByte, transferLength: UInt) {
    //     // newConfinedMemorySession().use { memorySession ->
    //     //     val commandBuffer = memorySession.allocate(32)

    //     //     // val argsSize = byteSinkInto(commandBuffer.takeLast(16)).run {
    //     //     //     encodeArgs()
    //     //     //     countWritten
    //     //     // }

    //     //     // byteSinkInto(commandBuffer.takeFirst(16)).run {
    //     //     //     writeUInt(0x431fd10bu, LITTLE_ENDIAN)
    //     //     //     skip(4)
    //     //     //     writeUByte(id)
    //     //     //     writeUByte(argsSize.toUByte())
    //     //     //     skip(2)
    //     //     //     writeUInt(transferLength)
    //     //     // }

    //     //     // sendExact(outEndpoint, commandBuffer)
    //     // }
    // }

    // fun UsbDeviceConnection.setExclusivity(exclusivity: PicobootExclusivity) {
    //     sendCommand(0x01u, 0u) {
    //         writeByte(exclusivity.ordinal.toByte())
    //     }

    //     // receiveZeroLength(inEndpoint)
    // }

    // fun UsbDeviceConnection.eraseFlash(deviceAddress: UInt, byteCount: UInt) {
    //     // FIXME: bounds-check against RP2040 address space?

    //     require(deviceAddress % 4096u == 0u)
    //     require(byteCount % 4096u == 0u)
    //     require(byteCount != 0u)

    //     sendCommand(0x03u, 0u) {
    //         writeUInt(deviceAddress)
    //         writeUInt(byteCount)
    //     }

    //     // receiveZeroLength(inEndpoint)
    // }

    // fun UsbDeviceConnection.readMemory(deviceAddress: UInt, destination: MemorySegment) {
    //     // FIXME: bounds-check against RP2040 address space?

    //     require(destination.byteSize() in 1 .. UInt.MAX_VALUE.toLong())

    //     sendCommand(0x84u, destination.byteSize().toUInt()) {
    //         writeUInt(deviceAddress)
    //         writeUInt(destination.byteSize().toUInt())
    //     }

    //     // receiveExact(inEndpoint, destination)
    //     // sendZeroLength(outEndpoint)
    // }

    // fun UsbDeviceConnection.writeMemory(deviceAddress: UInt, data: MemorySegment) {
    //     // FIXME: bounds-check against RP2040 address space?

    //     require(data.byteSize() in 1 .. UInt.MAX_VALUE.toLong())

    //     sendCommand(0x05u, data.byteSize().toUInt()) {
    //         writeUInt(deviceAddress)
    //         writeUInt(data.byteSize().toUInt())
    //     }

    //     // sendExact(outEndpoint, data)
    //     // receiveZeroLength(inEndpoint)
    // }

    // fun UsbDeviceConnection.exitXip() {
    //     sendCommand(0x06u, 0u) {}
    //     // receiveZeroLength(inEndpoint)
    // }

    // fun UsbDeviceConnection.enterXip() {
    //     sendCommand(0x07u, 0u) {}
    //     // receiveZeroLength(inEndpoint)
    // }

    // companion object {
    //     fun find(device: UsbDevice): PicobootEndpoints? {
    //         if (device.vendorId != VENDOR_ID) {
    //             return null
    //         }

    //         if (device.productId != PRODUCT_ID) {
    //             return null
    //         }

    //         // for (configuration in device.configurations) {
    //         //     for (iface in configuration.interfaces) {
    //         //         if (iface.interfaceClass != INTERFACE_CLASS) {
    //         //             continue
    //         //         }

    //         //         if (iface.interfaceSubClass != INTERFACE_SUB_CLASS) {
    //         //             continue
    //         //         }

    //         //         if (iface.interfaceProtocol != INTERFACE_PROTOCOL) {
    //         //             continue
    //         //         }

    //         //         var inEndpoint: UsbBulkTransferInEndpoint? = null
    //         //         var outEndpoint: UsbBulkTransferOutEndpoint? = null

    //         //         for (endpoint in iface.endpoints) {
    //         //             when (endpoint) {
    //         //                 is UsbBulkTransferInEndpoint -> {
    //         //                     inEndpoint = endpoint
    //         //                 }

    //         //                 is UsbBulkTransferOutEndpoint -> {
    //         //                     outEndpoint = endpoint
    //         //                 }

    //         //                 else -> {}
    //         //             }
    //         //         }

    //         //         if (inEndpoint != null && outEndpoint != null) {
    //         //             return PicobootEndpoints(inEndpoint, outEndpoint)
    //         //         }
    //         //     }
    //         // }

    //         return null
    //     }
    // }
}

internal enum class PicobootExclusivity {
    NotExclusive,
    Exclusive,
    ExclusiveAndEject,
}

private const val VENDOR_ID: UShort = 0x2E8Au
private const val PRODUCT_ID: UShort = 0x0003u
private const val INTERFACE_CLASS: UByte = 0xFFu
private const val INTERFACE_SUB_CLASS: UByte = 0x00u
private const val INTERFACE_PROTOCOL: UByte = 0x00u
