package org.schism.usb

import org.schism.ffi.CInt
import org.schism.ffi.CSSizeT
import org.schism.ffi.CUnsignedChar
import org.schism.ffi.CUnsignedInt
import org.schism.ffi.NativeLibrary
import org.schism.ffi.Struct
import org.schism.memory.NativeAddress

@NativeLibrary.Name("usb-1.0")
internal interface Libusb : NativeLibrary {
    @NativeLibrary.Function("libusb_alloc_transfer")
    fun allocTransfer(isoPackets: CInt): NativeAddress

    @NativeLibrary.Function("libusb_cancel_transfer")
    fun cancelTransfer(transfer: NativeAddress): CInt

    @NativeLibrary.Function("libusb_claim_interface")
    fun claimInterface(devHandle: NativeAddress, interfaceNumber: CInt): CInt

    @NativeLibrary.Function("libusb_clear_halt")
    fun clearHalt(devHandle: NativeAddress, endpoint: CUnsignedChar): CInt

    @NativeLibrary.Function("libusb_close")
    fun close(devHandle: NativeAddress)

    @NativeLibrary.Function("libusb_free_config_descriptor")
    fun freeConfigDescriptor(config: NativeAddress)

    @NativeLibrary.Function("libusb_free_device_list")
    fun freeDeviceList(list: NativeAddress, unrefDevices: CInt)

    @NativeLibrary.Function("libusb_free_transfer")
    fun freeTransfer(transfer: NativeAddress)

    @NativeLibrary.Function("libusb_get_bus_number")
    fun getBusNumber(dev: NativeAddress): UByte

    @NativeLibrary.Function("libusb_get_config_descriptor")
    fun getConfigDescriptor(dev: NativeAddress, configIndex: UByte, config: NativeAddress): CInt

    @NativeLibrary.Function("libusb_get_configuration")
    fun getConfiguration(devHandle: NativeAddress, config: NativeAddress): CInt

    @NativeLibrary.Function("libusb_get_device_address")
    fun getDeviceAddress(dev: NativeAddress): UByte

    @NativeLibrary.Function("libusb_get_device_descriptor")
    fun getDeviceDescriptor(dev: NativeAddress, desc: NativeAddress): CInt

    @NativeLibrary.Function("libusb_get_device_list")
    fun getDeviceList(ctx: NativeAddress, list: NativeAddress): CSSizeT

    @NativeLibrary.Function("libusb_get_port_numbers")
    fun getPortNumbers(dev: NativeAddress, portNumbers: NativeAddress, portNumbersLen: CInt): CInt

    @NativeLibrary.Function("libusb_handle_events")
    fun handleEvents(ctx: NativeAddress): CInt

    @NativeLibrary.Function("libusb_init")
    fun init(ctx: NativeAddress): CInt

    @NativeLibrary.Function("libusb_open")
    fun open(dev: NativeAddress, devHandle: NativeAddress): CInt

    @NativeLibrary.Function("libusb_ref_device")
    fun refDevice(dev: NativeAddress): NativeAddress

    @NativeLibrary.Function("libusb_release_interface")
    fun releaseInterface(devHandle: NativeAddress, interfaceNumber: CInt): CInt

    @NativeLibrary.Function("libusb_reset_device")
    fun resetDevice(devHandle: NativeAddress): CInt

    @NativeLibrary.Function("libusb_set_interface_alt_setting")
    fun setInterfaceAltSetting(devHandle: NativeAddress, interfaceNumber: CInt, alternateSetting: CInt): CInt

    @NativeLibrary.Function("libusb_strerror")
    fun strerror(errCode: CInt): NativeAddress

    @NativeLibrary.Function("libusb_submit_transfer")
    fun submitTransfer(transfer: NativeAddress): CInt

    @NativeLibrary.Function("libusb_unref_device")
    fun unrefDevice(dev: NativeAddress)

    @Struct.Fields(
        "bLength",
        "bDescriptorType",
        "wTotalLength",
        "bNumInterfaces",
        "bConfigurationValue",
        "iConfiguration",
        "bmAttributes",
        "MaxPower",
        "interface",
        "extra",
        "extra_length",
    )
    interface ConfigDescriptor : Struct {
        val bLength: UByte
        val bDescriptorType: UByte
        val wTotalLength: UShort
        val bNumInterfaces: UByte
        val bConfigurationValue: UByte
        val iConfiguration: UByte
        val bmAttributes: UByte
        val MaxPower: UByte
        val `interface`: NativeAddress
        val extra: NativeAddress
        val extra_length: CInt

        companion object : Struct.Type<ConfigDescriptor> by Struct.type()
    }

    @Struct.Fields(
        "bLength",
        "bDescriptorType",
        "bcdUSB",
        "bDeviceClass",
        "bDeviceSubClass",
        "bDeviceProtocol",
        "bMaxPacketSize0",
        "idVendor",
        "idProduct",
        "bcdDevice",
        "iManufacturer",
        "iProduct",
        "iSerialNumber",
        "bNumConfigurations",
    )
    interface DeviceDescriptor : Struct {
        val bLength: UByte
        val bDescriptorType: UByte
        val bcdUSB: UShort
        val bDeviceClass: UByte
        val bDeviceSubClass: UByte
        val bDeviceProtocol: UByte
        val bMaxPacketSize0: UByte
        val idVendor: UShort
        val idProduct: UShort
        val bcdDevice: UShort
        val iManufacturer: UByte
        val iProduct: UByte
        val iSerialNumber: UByte
        val bNumConfigurations: UByte

        companion object : Struct.Type<DeviceDescriptor> by Struct.type()
    }

    @Struct.Fields(
        "bLength",
        "bDescriptorType",
        "bEndpointAddress",
        "bmAttributes",
        "wMaxPacketSize",
        "bInterval",
        "bRefresh",
        "bSynchAddress",
        "extra",
        "extra_length",
    )
    interface EndpointDescriptor : Struct {
        val bLength: UByte
        val bDescriptorType: UByte
        val bEndpointAddress: UByte
        val bmAttributes: UByte
        val wMaxPacketSize: UShort
        val bInterval: UByte
        val bRefresh: UByte
        val bSynchAddress: UByte
        val extra: NativeAddress
        val extra_length: CInt

        companion object : Struct.Type<EndpointDescriptor> by Struct.type()
    }

    @Struct.Fields(
        "altsetting",
        "num_altsetting",
    )
    interface Interface : Struct {
        val altsetting: NativeAddress
        val num_altsetting: CInt

        companion object : Struct.Type<Interface> by Struct.type()
    }

    @Struct.Fields(
        "bLength",
        "bDescriptorType",
        "bInterfaceNumber",
        "bAlternateSetting",
        "bNumEndpoints",
        "bInterfaceClass",
        "bInterfaceSubClass",
        "bInterfaceProtocol",
        "iInterface",
        "endpoint",
        "extra",
        "extra_length",
    )
    interface InterfaceDescriptor : Struct {
        val bLength: UByte
        val bDescriptorType: UByte
        val bInterfaceNumber: UByte
        val bAlternateSetting: UByte
        val bNumEndpoints: UByte
        val bInterfaceClass: UByte
        val bInterfaceSubClass: UByte
        val bInterfaceProtocol: UByte
        val iInterface: UByte
        val endpoint: NativeAddress
        val extra: NativeAddress
        val extra_length: CInt

        companion object : Struct.Type<InterfaceDescriptor> by Struct.type()
    }

    @Struct.Fields(
        "dev_handle",
        "flags",
        "endpoint",
        "type",
        "timeout",
        "status",
        "length",
        "actual_length",
        "callback",
        "user_data",
        "buffer",
        "num_iso_packets",
    )
    interface Transfer : Struct {
        var dev_handle: NativeAddress
        var flags: UByte
        var endpoint: CUnsignedChar
        var type: CUnsignedChar
        var timeout: CUnsignedInt
        var status: CInt
        var length: CInt
        var actual_length: CInt
        var callback: NativeAddress
        var user_data: NativeAddress
        var buffer: NativeAddress
        var num_iso_packets: CInt

        companion object : Struct.Type<Transfer> by Struct.type()
    }

    object TransferStatus {
        const val COMPLETED: CInt = 0
        const val ERROR: CInt = 1
        const val TIMED_OUT: CInt = 2
        const val CANCELED: CInt = 3
        const val STALL: CInt = 4
        const val NO_DEVICE: CInt = 5
        const val OVERFLOW: CInt = 6
    }

    object TransferFlags {
        const val SHORT_NOT_OK: UByte = 0x1u
        const val FREE_BUFFER: UByte = 0x2u
        const val FREE_TRANSFER: UByte = 0x4u
        const val ADD_ZERO_PACKET: UByte = 0x8u
    }

    object TransferType {
        const val CONTROL: UByte = 0u
        const val ISOCHRONOUS: UByte = 1u
        const val BULK: UByte = 2u
        const val INTERRUPT: UByte = 3u
        const val BULK_STREAM: UByte = 4u
    }

    companion object : Libusb by NativeLibrary.link() {
        private fun errorMessage(code: CInt): String {
            return strerror(code).readUtf8CString()
        }

        fun checkReturn(code: CInt) {
            if (code != 0) {
                throw UsbException(errorMessage(code))
            }
        }

        fun checkSize(value: CInt): CInt {
            if (value < 0) {
                throw UsbException(errorMessage(value))
            }

            return value
        }

        fun checkSize(value: CSSizeT): CSSizeT {
            if (value < 0) {
                throw UsbException(errorMessage(value.toIntExact()))
            }

            return value
        }
    }
}
