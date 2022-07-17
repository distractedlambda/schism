package org.schism.usb

import org.schism.ffi.CInt
import org.schism.ffi.CSSizeT
import org.schism.ffi.CUnsignedChar
import org.schism.ffi.CUnsignedInt
import org.schism.ffi.NativeFunction
import org.schism.ffi.Struct
import org.schism.ffi.StructField
import org.schism.ffi.StructType
import org.schism.ffi.linkNativeLibrary
import org.schism.memory.NativeAddress
import org.schism.memory.readUtf8CString
import org.schism.memory.withNativePointer
import java.lang.foreign.MemorySession
import java.lang.foreign.SymbolLookup.libraryLookup
import java.nio.file.Path
import kotlin.concurrent.thread

internal interface Libusb {
    @NativeFunction("libusb_alloc_transfer")
    fun alloc_transfer(iso_packets: @CInt Int): NativeAddress

    @NativeFunction("libusb_cancel_transfer")
    fun cancel_transfer(transfer: NativeAddress): @CInt Int

    @NativeFunction("libusb_claim_interface")
    fun claim_interface(dev_handle: NativeAddress, interface_number: @CInt Int): @CInt Int

    @NativeFunction("libusb_clear_halt")
    fun clear_halt(dev_handle: NativeAddress, endpoint: @CUnsignedChar UByte): @CInt Int

    @NativeFunction("libusb_close")
    fun close(dev_handle: NativeAddress)

    @NativeFunction("libusb_free_config_descriptor")
    fun free_config_descriptor(config: NativeAddress)

    @NativeFunction("libusb_free_device_list")
    fun free_device_list(list: NativeAddress, unref_devices: @CInt Int)

    @NativeFunction("libusb_get_config_descriptor")
    fun get_config_descriptor(dev: NativeAddress, config_index: UByte, config: NativeAddress): @CInt Int

    @NativeFunction("libusb_get_configuration")
    fun get_configuration(dev_handle: NativeAddress, config: NativeAddress): @CInt Int

    @NativeFunction("libusb_get_device_descriptor")
    fun get_device_descriptor(dev: NativeAddress, desc: NativeAddress): @CInt Int

    @NativeFunction("libusb_get_device_list")
    fun get_device_list(ctx: NativeAddress, list: NativeAddress): @CSSizeT Long

    @NativeFunction("libusb_get_port_numbers")
    fun get_port_numbers(dev: NativeAddress, port_numbers: NativeAddress, port_numbers_len: @CInt Int): @CInt Int

    @NativeFunction("libusb_handle_events")
    fun handle_events(ctx: NativeAddress): @CInt Int

    @NativeFunction("libusb_init")
    fun init(ctx: NativeAddress): @CInt Int

    @NativeFunction("libusb_open")
    fun open(dev: NativeAddress, dev_handle: NativeAddress): @CInt Int

    @NativeFunction("libusb_ref_device")
    fun ref_device(dev: NativeAddress): NativeAddress

    @NativeFunction("libusb_release_interface")
    fun release_interface(dev_handle: NativeAddress, interface_number: @CInt Int): @CInt Int

    @NativeFunction("libusb_reset_device")
    fun reset_device(dev_handle: NativeAddress): @CInt Int

    @NativeFunction("libusb_set_interface_alt_setting")
    fun set_interface_alt_setting(
        dev_handle: NativeAddress,
        interface_number: @CInt Int,
        alternate_setting: @CInt Int,
    ): @CInt Int

    @NativeFunction("libusb_strerror")
    fun strerror(errcode: @CInt Int): NativeAddress

    @NativeFunction("libusb_submit_transfer")
    fun submit_transfer(transfer: NativeAddress): @CInt Int

    @NativeFunction("libusb_unref_device")
    fun unref_device(dev: NativeAddress)

    interface ConfigDescriptor : Struct {
        @StructField(0) val bLength: UByte
        @StructField(1) val bDescriptorType: UByte
        @StructField(2) val wTotalLength: UShort
        @StructField(3) val bNumInterfaces: UByte
        @StructField(4) val bConfigurationValue: UByte
        @StructField(5) val iConfiguration: UByte
        @StructField(6) val bmAttributes: UByte
        @StructField(7) val MaxPower: UByte
        @StructField(8) val iface: NativeAddress
        @StructField(9) val extra: NativeAddress
        @StructField(10) val extra_length: @CInt Int

        companion object {
            val Type = StructType<ConfigDescriptor>()
        }
    }

    interface DeviceDescriptor : Struct {
        @StructField(0) val bLength: UByte
        @StructField(1) val bDescriptorType: UByte
        @StructField(2) val bcdUSB: UShort
        @StructField(3) val bDeviceClass: UByte
        @StructField(4) val bDeviceSubClass: UByte
        @StructField(5) val bDeviceProtocol: UByte
        @StructField(6) val bMaxPacketSize0: UByte
        @StructField(7) val idVendor: UShort
        @StructField(8) val idProduct: UShort
        @StructField(9) val bcdDevice: UShort
        @StructField(10) val iManufacturer: UByte
        @StructField(11) val iProduct: UByte
        @StructField(12) val iSerialNumber: UByte
        @StructField(13) val bNumConfigurations: UByte

        companion object {
            val Type = StructType<DeviceDescriptor>()
        }
    }

    interface EndpointDescriptor : Struct {
        @StructField(0) val bLength: UByte
        @StructField(1) val bDescriptorType: UByte
        @StructField(2) val bEndpointAddress: UByte
        @StructField(3) val bmAttributes: UByte
        @StructField(4) val wMaxPacketSize: UShort
        @StructField(5) val bInterval: UByte
        @StructField(6) val bRefresh: UByte
        @StructField(7) val bSynchAddress: UByte
        @StructField(8) val extra: NativeAddress
        @StructField(9) val extra_length: @CInt Int

        companion object {
            val Type = StructType<EndpointDescriptor>()
        }
    }

    interface Interface : Struct {
        @StructField(0) val altsetting: NativeAddress
        @StructField(1) val num_altsetting: @CInt Int

        companion object {
            val Type = StructType<Interface>()
        }
    }

    interface InterfaceDescriptor : Struct {
        @StructField(0) val bLength: UByte
        @StructField(1) val bDescriptorType: UByte
        @StructField(2) val bInterfaceNumber: UByte
        @StructField(3) val bAlternateSetting: UByte
        @StructField(4) val bNumEndpoints: UByte
        @StructField(5) val bInterfaceClass: UByte
        @StructField(6) val bInterfaceSubClass: UByte
        @StructField(7) val bInterfaceProtocol: UByte
        @StructField(8) val iInterface: UByte
        @StructField(9) val endpoint: NativeAddress
        @StructField(10) val extra: NativeAddress
        @StructField(11) val extra_length: @CInt Int

        companion object {
            val Type = StructType<InterfaceDescriptor>()
        }
    }

    interface Transfer : Struct {
        @StructField(0) var dev_handle: NativeAddress
        @StructField(1) var flags: UByte
        @StructField(2) var endpoint: @CUnsignedChar UByte
        @StructField(3) var type: @CUnsignedChar UByte
        @StructField(4) var timeout: @CUnsignedInt UInt
        @StructField(5) var status: @CInt Int
        @StructField(6) var length: @CInt Int
        @StructField(7) var actual_length: @CInt Int
        @StructField(8) var callback: NativeAddress
        @StructField(9) var user_data: NativeAddress
        @StructField(10) var buffer: NativeAddress
        @StructField(11) var num_iso_packets: @CInt Int

        companion object {
            val Type = StructType<Transfer>()
        }
    }

    object TransferStatus {
        const val COMPLETED = 0
        const val ERROR = 1
        const val TIMED_OUT = 2
        const val CANCELED = 3
        const val STALL = 4
        const val NO_DEVICE = 5
        const val OVERFLOW = 6
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
}

internal val libusb = linkNativeLibrary<Libusb>(
    libraryLookup(
        Path.of("/opt/homebrew/lib/libusb-1.0.dylib"),
        MemorySession.openImplicit()
    )
)

internal fun libusbErrorMessage(code: Int): String {
    return libusb.strerror(code).readUtf8CString()
}

internal fun checkLibusbReturn(code: Int) {
    if (code != 0) {
        throw UsbException(libusbErrorMessage(code))
    }
}

internal fun checkLibusbSizeReturn(value: Int): Int {
    if (value < 0) {
        throw UsbException(libusbErrorMessage(value))
    }

    return value
}

internal fun checkLibusbSizeReturn(value: Long): Long {
    if (value < 0) {
        throw UsbException(libusbErrorMessage(value.toInt()))
    }

    return value
}

internal val libusbContext = withNativePointer {
    checkLibusbReturn(libusb.init(it.memory.startAddress))
    it.value
}

internal val libusbEventHandlerThread = thread(isDaemon = true, name = "libusb event handler") {
    while (true) {
        checkLibusbReturn(libusb.handle_events(libusbContext))
    }
}
