package org.schism.usb

import org.schism.bytes.cStructLayout
import java.lang.Math.toIntExact
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker
import java.lang.foreign.MemoryLayout
import java.lang.foreign.MemoryLayout.PathElement.groupElement
import java.lang.foreign.MemorySession
import java.lang.foreign.SymbolLookup
import java.lang.foreign.ValueLayout.ADDRESS
import java.lang.foreign.ValueLayout.JAVA_BYTE
import java.lang.foreign.ValueLayout.JAVA_INT
import java.lang.foreign.ValueLayout.JAVA_LONG
import java.lang.foreign.ValueLayout.JAVA_SHORT
import java.lang.invoke.MethodHandle
import kotlin.io.path.Path

internal object Libusb {
    val allocTransfer: MethodHandle
    val cancelTransfer: MethodHandle
    val claimInterface: MethodHandle
    val clearHalt: MethodHandle
    val close: MethodHandle
    val exit: MethodHandle
    val freeConfigDescriptor: MethodHandle
    val freeDeviceList: MethodHandle
    val freeTransfer: MethodHandle
    val getActiveConfigDescriptor: MethodHandle
    val getBusNumber: MethodHandle
    val getConfigDescriptor: MethodHandle
    val getDeviceAddress: MethodHandle
    val getDeviceDescriptor: MethodHandle
    val getDeviceList: MethodHandle
    val handleEvents: MethodHandle
    val init: MethodHandle
    val interruptEventHandler: MethodHandle
    val open: MethodHandle
    val refDevice: MethodHandle
    val releaseInterface: MethodHandle
    val resetDevice: MethodHandle
    val strerror: MethodHandle
    val submitTransfer: MethodHandle
    val unrefDevice: MethodHandle

    init {
        val lib = SymbolLookup.libraryLookup(Path("/opt/homebrew/lib/libusb-1.0.dylib"), MemorySession.openImplicit())
        val linker = Linker.nativeLinker()

        fun link(name: String, vararg argumentLayouts: MemoryLayout, returning: MemoryLayout? = null): MethodHandle {
            val descriptor = if (returning != null) {
                FunctionDescriptor.of(returning, *argumentLayouts)
            } else {
                FunctionDescriptor.ofVoid(*argumentLayouts)
            }

            return linker.downcallHandle(lib.lookup("libusb_$name").orElseThrow(), descriptor)
        }

        allocTransfer = link("alloc_transfer", JAVA_INT, returning = ADDRESS)

        cancelTransfer = link("cancel_transfer", ADDRESS, returning = JAVA_INT)

        claimInterface = link("claim_interface", ADDRESS, JAVA_INT, returning = JAVA_INT)

        clearHalt = link("clear_halt", ADDRESS, JAVA_BYTE, returning = JAVA_INT)

        close = link("close", ADDRESS)

        exit = link("exit", ADDRESS)

        freeConfigDescriptor = link("free_config_descriptor", ADDRESS)

        freeDeviceList = link("free_device_list", ADDRESS, JAVA_INT)

        freeTransfer = link("free_transfer", ADDRESS)

        getActiveConfigDescriptor = link("get_active_config_descriptor", ADDRESS, ADDRESS, returning = JAVA_INT)

        getBusNumber = link("get_bus_number", ADDRESS, returning = JAVA_BYTE)

        getConfigDescriptor = link("get_config_descriptor", ADDRESS, JAVA_BYTE, ADDRESS, returning = JAVA_INT)

        getDeviceAddress = link("get_device_address", ADDRESS, returning = JAVA_BYTE)

        getDeviceDescriptor = link("get_device_descriptor", ADDRESS, ADDRESS, returning = JAVA_INT)

        getDeviceList = link("get_device_list", ADDRESS, ADDRESS, returning = JAVA_LONG) // FIXME: 32-bit ssize_t?

        handleEvents = link("handle_events", ADDRESS, returning = JAVA_INT)

        init = link("init", ADDRESS, returning = JAVA_INT)

        interruptEventHandler = link("interrupt_event_handler", ADDRESS)

        open = link("open", ADDRESS, ADDRESS, returning = JAVA_INT)

        refDevice = link("ref_device", ADDRESS, returning = ADDRESS)

        releaseInterface = link("release_interface", ADDRESS, JAVA_INT, returning = JAVA_INT)

        resetDevice = link("reset_device", ADDRESS, returning = JAVA_INT)

        strerror = link("strerror", JAVA_INT, returning = ADDRESS)

        submitTransfer = link("submit_transfer", ADDRESS, returning = JAVA_INT)

        unrefDevice = link("unref_device", ADDRESS)
    }

    fun checkReturn(code: Int) {
        if (code != 0) {
            throw LibusbErrorException(code)
        }
    }

    fun checkSize(size: Long): Long {
        if (size < 0) {
            throw LibusbErrorException(toIntExact(size))
        }

        return size
    }

    internal object Transfer {
        val LAYOUT = cStructLayout(
            ADDRESS.withName("dev_handle"),
            JAVA_BYTE.withName("flags"),
            JAVA_BYTE.withName("endpoint"),
            JAVA_BYTE.withName("type"),
            JAVA_INT.withName("timeout"),
            JAVA_INT.withName("status"),
            JAVA_INT.withName("length"),
            JAVA_INT.withName("actual_length"),
            ADDRESS.withName("callback"),
            ADDRESS.withName("user_data"),
            ADDRESS.withName("buffer"),
            JAVA_INT.withName("num_iso_packets"),
        )

        val DEV_HANDLE = LAYOUT.varHandle(groupElement("dev_handle"))!!
        val FLAGS = LAYOUT.varHandle(groupElement("flags"))!!
        val ENDPOINT = LAYOUT.varHandle(groupElement("endpoint"))!!
        val TYPE = LAYOUT.varHandle(groupElement("type"))!!
        val TIMEOUT = LAYOUT.varHandle(groupElement("timeout"))!!
        val STATUS = LAYOUT.varHandle(groupElement("status"))!!
        val LENGTH = LAYOUT.varHandle(groupElement("length"))!!
        val ACTUAL_LENGTH = LAYOUT.varHandle(groupElement("actual_length"))!!
        val CALLBACK = LAYOUT.varHandle(groupElement("callback"))!!
        val USER_DATA = LAYOUT.varHandle(groupElement("user_data"))!!
        val BUFFER = LAYOUT.varHandle(groupElement("buffer"))!!
        val NUM_ISO_PACKETS = LAYOUT.varHandle(groupElement("num_iso_packets"))!!
    }

    internal object TransferStatus {
        const val COMPLETED = 0
        const val ERROR = 1
        const val TIMED_OUT = 2
        const val CANCELED = 3
        const val STALL = 4
        const val NO_DEVICE = 5
        const val OVERFLOW = 6
    }

    internal object TransferFlags {
        const val SHORT_NOT_OK: UByte = 0x1u
        const val FREE_BUFFER: UByte = 0x2u
        const val FREE_TRANSFER: UByte = 0x4u
        const val ADD_ZERO_PACKET: UByte = 0x8u
    }

    internal object TransferType {
        const val CONTROL: UByte = 0u
        const val ISOCHRONOUS: UByte = 1u
        const val BULK: UByte = 2u
        const val INTERRUPT: UByte = 3u
        const val BULK_STREAM: UByte = 4u
    }

    internal object DeviceDescriptor {
        val LAYOUT = cStructLayout(
            JAVA_BYTE.withName("bLength"),
            JAVA_BYTE.withName("bDescriptorType"),
            JAVA_SHORT.withName("bcdUSB"),
            JAVA_BYTE.withName("bDeviceClass"),
            JAVA_BYTE.withName("bDeviceSubClass"),
            JAVA_BYTE.withName("bDeviceProtocol"),
            JAVA_BYTE.withName("bMaxPacketSize0"),
            JAVA_SHORT.withName("idVendor"),
            JAVA_SHORT.withName("idProduct"),
            JAVA_SHORT.withName("bcdDevice"),
            JAVA_BYTE.withName("iManufacturer"),
            JAVA_BYTE.withName("iProduct"),
            JAVA_BYTE.withName("iSerialNumber"),
            JAVA_BYTE.withName("bNumConfigurations"),
        )

        val BCD_USB = LAYOUT.varHandle(groupElement("bcdUSB"))!!
        val B_DEVICE_CLASS = LAYOUT.varHandle(groupElement("bDeviceClass"))!!
        val B_DEVICE_SUB_CLASS = LAYOUT.varHandle(groupElement("bDeviceSubClass"))!!
        val B_DEVICE_PROTOCOL = LAYOUT.varHandle(groupElement("bDeviceProtocol"))!!
        val B_MAX_PACKET_SIZE_0 = LAYOUT.varHandle(groupElement("bMaxPacketSize0"))!!
        val ID_VENDOR = LAYOUT.varHandle(groupElement("idVendor"))!!
        val ID_PRODUCT = LAYOUT.varHandle(groupElement("idProduct"))!!
        val BCD_DEVICE = LAYOUT.varHandle(groupElement("bcdDevice"))!!
        val I_MANUFACTURER = LAYOUT.varHandle(groupElement("iManufacturer"))!!
        val I_PRODUCT = LAYOUT.varHandle(groupElement("iProduct"))!!
        val I_SERIAL_NUMBER = LAYOUT.varHandle(groupElement("iSerialNumber"))!!
        val B_NUM_CONFIGURATIONS = LAYOUT.varHandle(groupElement("bNumConfigurations"))!!
    }

    internal object ConfigDescriptor {
        val LAYOUT = cStructLayout(
            JAVA_BYTE.withName("bLength"),
            JAVA_BYTE.withName("bDescriptorType"),
            JAVA_SHORT.withName("wTotalLength"),
            JAVA_BYTE.withName("bNumInterfaces"),
            JAVA_BYTE.withName("bConfigurationValue"),
            JAVA_BYTE.withName("iConfiguration"),
            JAVA_BYTE.withName("bmAttributes"),
            JAVA_BYTE.withName("MaxPower"),
            ADDRESS.withName("interface"),
            ADDRESS.withName("extra"),
            JAVA_INT.withName("extra_length"),
        )

        val B_NUM_INTERFACES = LAYOUT.varHandle(groupElement("bNumInterfaces"))!!
        val B_CONFIGURATION_VALUE = LAYOUT.varHandle(groupElement("bConfigurationValue"))!!
        val I_CONFIGURATION = LAYOUT.varHandle(groupElement("iConfiguration"))!!
        val BM_ATTRIBUTES = LAYOUT.varHandle(groupElement("bmAttributes"))!!
        val MAX_POWER = LAYOUT.varHandle(groupElement("MaxPower"))!!
        val INTERFACE = LAYOUT.varHandle(groupElement("interface"))!!
        val EXTRA = LAYOUT.varHandle(groupElement("extra"))!!
        val EXTRA_LENGTH = LAYOUT.varHandle(groupElement("extra_length"))!!
    }

    internal object Interface {
        val LAYOUT = cStructLayout(
            ADDRESS.withName("altsetting"),
            JAVA_INT.withName("num_altsetting"),
        )

        val ALTSETTING = LAYOUT.varHandle(groupElement("altsetting"))!!
        val NUM_ALTSETTING = LAYOUT.varHandle(groupElement("num_altsetting"))!!
    }

    internal object InterfaceDescriptor {
        val LAYOUT = cStructLayout(
            JAVA_BYTE.withName("bLength"),
            JAVA_BYTE.withName("bDescriptorType"),
            JAVA_BYTE.withName("bInterfaceNumber"),
            JAVA_BYTE.withName("bAlternateSetting"),
            JAVA_BYTE.withName("bNumEndpoints"),
            JAVA_BYTE.withName("bInterfaceClass"),
            JAVA_BYTE.withName("bInterfaceSubClass"),
            JAVA_BYTE.withName("bInterfaceProtocol"),
            JAVA_BYTE.withName("iInterface"),
            ADDRESS.withName("endpoint"),
            ADDRESS.withName("extra"),
            JAVA_INT.withName("extra_length"),
        )

        val B_INTERFACE_NUMBER = LAYOUT.varHandle(groupElement("bInterfaceNumber"))!!
        val B_ALTERNATE_SETTING = LAYOUT.varHandle(groupElement("bAlternateSetting"))!!
        val B_NUM_ENDPOINTS = LAYOUT.varHandle(groupElement("bNumEndpoints"))!!
        val B_INTERFACE_CLASS = LAYOUT.varHandle(groupElement("bInterfaceClass"))!!
        val B_INTERFACE_SUB_CLASS = LAYOUT.varHandle(groupElement("bInterfaceSubClass"))!!
        val B_INTERFACE_PROTOCOL = LAYOUT.varHandle(groupElement("bInterfaceProtocol"))!!
        val I_INTERFACE = LAYOUT.varHandle(groupElement("iInterface"))!!
        val ENDPOINT = LAYOUT.varHandle(groupElement("endpoint"))!!
        val EXTRA = LAYOUT.varHandle(groupElement("extra"))!!
        val EXTRA_LENGTH = LAYOUT.varHandle(groupElement("extra_length"))!!
    }

    internal object EndpointDescriptor {
        val LAYOUT = cStructLayout(
            JAVA_BYTE.withName("bLength"),
            JAVA_BYTE.withName("bDescriptorType"),
            JAVA_BYTE.withName("bEndpointAddress"),
            JAVA_BYTE.withName("bmAttributes"),
            JAVA_SHORT.withName("wMaxPacketSize"),
            JAVA_BYTE.withName("bInterval"),
            JAVA_BYTE.withName("bRefresh"),
            JAVA_BYTE.withName("bSynchAddress"),
            ADDRESS.withName("extra"),
            JAVA_INT.withName("extra_length")
        )

        val B_ENDPOINT_ADDRESS = LAYOUT.varHandle(groupElement("bEndpointAddress"))!!
        val BM_ATTRIBUTES = LAYOUT.varHandle(groupElement("bmAttributes"))!!
        val W_MAX_PACKET_SIZE = LAYOUT.varHandle(groupElement("wMaxPacketSize"))!!
        val B_INTERVAL = LAYOUT.varHandle(groupElement("bInterval"))!!
        val B_REFRESH = LAYOUT.varHandle(groupElement("bRefresh"))!!
        val B_SYNCH_ADDRESS = LAYOUT.varHandle(groupElement("bSynchAddress"))!!
        val EXTRA = LAYOUT.varHandle(groupElement("extra"))!!
        val EXTRA_LENGTH = LAYOUT.varHandle(groupElement("extra_length"))!!
    }

    internal object ControlSetup {
        val LAYOUT = MemoryLayout.structLayout(
            JAVA_BYTE.withName("bmRequestType"),
            JAVA_BYTE.withName("bRequest"),
            JAVA_SHORT.withName("wValue"),
            JAVA_SHORT.withName("wIndex"),
            JAVA_SHORT.withName("wLength"),
        )!!

        val BM_REQUEST_TYPE = LAYOUT.varHandle(groupElement("bmRequestType"))!!
        val B_REQUEST = LAYOUT.varHandle(groupElement("bRequest"))!!
        val W_VALUE = LAYOUT.varHandle(groupElement("wValue"))!!
        val W_INDEX = LAYOUT.varHandle(groupElement("wIndex"))!!
        val W_LENGTH = LAYOUT.varHandle(groupElement("wLength"))!!
    }
}
