package org.schism.cousb

import java.lang.Math.toIntExact
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.GroupLayout
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

internal object Libusb {
    val alloc_transfer: MethodHandle
    val cancel_transfer: MethodHandle
    val claim_interface: MethodHandle
    val clear_halt: MethodHandle
    val close: MethodHandle
    val exit: MethodHandle
    val free_config_descriptor: MethodHandle
    val free_device_list: MethodHandle
    val free_transfer: MethodHandle
    val get_active_config_descriptor: MethodHandle
    val get_bus_number: MethodHandle
    val get_config_descriptor: MethodHandle
    val get_device_address: MethodHandle
    val get_device_descriptor: MethodHandle
    val get_device_list: MethodHandle
    val handle_events: MethodHandle
    val init: MethodHandle
    val interrupt_event_handler: MethodHandle
    val open: MethodHandle
    val ref_device: MethodHandle
    val release_interface: MethodHandle
    val reset_device: MethodHandle
    val strerror: MethodHandle
    val submit_transfer: MethodHandle
    val unref_device: MethodHandle

    init {
        val lib = SymbolLookup.libraryLookup("usb-1.0", MemorySession.openImplicit())
        val linker = Linker.nativeLinker()

        fun link(name: String, vararg argumentLayouts: MemoryLayout, returning: MemoryLayout? = null): MethodHandle {
            val descriptor = if (returning != null) {
                FunctionDescriptor.of(returning, *argumentLayouts)
            } else {
                FunctionDescriptor.ofVoid(*argumentLayouts)
            }

            return linker.downcallHandle(lib.lookup("libusb_$name").orElseThrow(), descriptor)
        }

        alloc_transfer = link("alloc_transfer", JAVA_INT, returning = ADDRESS)

        cancel_transfer = link("cancel_transfer", ADDRESS, returning = JAVA_INT)

        claim_interface = link("claim_interface", ADDRESS, JAVA_INT, returning = JAVA_INT)

        clear_halt = link("clear_halt", ADDRESS, JAVA_BYTE, returning = JAVA_INT)

        close = link("close", ADDRESS)

        exit = link("exit", ADDRESS)

        free_config_descriptor = link("free_config_descriptor", ADDRESS)

        free_device_list = link("free_device_list", ADDRESS, JAVA_INT)

        free_transfer = link("free_transfer", ADDRESS)

        get_active_config_descriptor = link("get_active_config_descriptor", ADDRESS, ADDRESS, returning = JAVA_INT)

        get_bus_number = link("get_bus_number", ADDRESS, returning = JAVA_BYTE)

        get_config_descriptor = link("get_config_descriptor", ADDRESS, JAVA_BYTE, ADDRESS, returning = JAVA_INT)

        get_device_address = link("get_device_address", ADDRESS, returning = JAVA_BYTE)

        get_device_descriptor = link("get_device_descriptor", ADDRESS, ADDRESS, returning = JAVA_INT)

        // FIXME: handle 32-bit ssize_t?
        get_device_list = link("get_device_list", ADDRESS, ADDRESS, returning = JAVA_LONG)

        handle_events = link("handle_events", ADDRESS, returning = JAVA_INT)

        init = link("init", ADDRESS, returning = JAVA_INT)

        interrupt_event_handler = link("interrupt_event_handler", ADDRESS)

        open = link("open", ADDRESS, ADDRESS, returning = JAVA_INT)

        ref_device = link("ref_device", ADDRESS, returning = ADDRESS)

        release_interface = link("release_interface", ADDRESS, JAVA_INT, returning = JAVA_INT)

        reset_device = link("reset_device", ADDRESS, returning = JAVA_INT)

        strerror = link("strerror", JAVA_INT, returning = ADDRESS)

        submit_transfer = link("submit_transfer", ADDRESS, returning = JAVA_INT)

        unref_device = link("unref_device", ADDRESS)
    }

    val transfer = cStructLayout(
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

    val transfer_dev_handle = transfer.varHandle(groupElement("dev_handle"))
    val transfer_flags = transfer.varHandle(groupElement("flags"))
    val transfer_endpoint = transfer.varHandle(groupElement("endpoint"))
    val transfer_type = transfer.varHandle(groupElement("type"))
    val transfer_timeout = transfer.varHandle(groupElement("timeout"))
    val transfer_status = transfer.varHandle(groupElement("status"))
    val transfer_length = transfer.varHandle(groupElement("length"))
    val transfer_actual_length = transfer.varHandle(groupElement("actual_length"))
    val transfer_callback = transfer.varHandle(groupElement("callback"))
    val transfer_user_data = transfer.varHandle(groupElement("user_data"))
    val transfer_buffer = transfer.varHandle(groupElement("buffer"))
    val transfer_num_iso_packets = transfer.varHandle(groupElement("num_iso_packets"))

    val device_descriptor = cStructLayout(
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

    val device_descriptor_bcdUSB = device_descriptor.varHandle(groupElement("bcdUSB"))
    val device_descriptor_bDeviceClass = device_descriptor.varHandle(groupElement("bDeviceClass"))
    val device_descriptor_bDeviceSubClass = device_descriptor.varHandle(groupElement("bDeviceSubClass"))
    val device_descriptor_bDeviceProtocol = device_descriptor.varHandle(groupElement("bDeviceProtocol"))
    val device_descriptor_bMaxPacketSize0 = device_descriptor.varHandle(groupElement("bMaxPacketSize0"))
    val device_descriptor_idVendor = device_descriptor.varHandle(groupElement("idVendor"))
    val device_descriptor_idProduct = device_descriptor.varHandle(groupElement("idProduct"))
    val device_descriptor_bcdDevice = device_descriptor.varHandle(groupElement("bcdDevice"))
    val device_descriptor_iManufacturer = device_descriptor.varHandle(groupElement("iManufacturer"))
    val device_descriptor_iProduct = device_descriptor.varHandle(groupElement("iProduct"))
    val device_descriptor_iSerialNumber = device_descriptor.varHandle(groupElement("iSerialNumber"))
    val device_descriptor_bNumConfigurations = device_descriptor.varHandle(groupElement("bNumConfigurations"))

    val config_descriptor = cStructLayout(
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

    val config_descriptor_bNumInterfaces = config_descriptor.varHandle(groupElement("bNumInterfaces"))
    val config_descriptor_bConfigurationValue = config_descriptor.varHandle(groupElement("bConfigurationValue"))
    val config_descriptor_iConfiguration = config_descriptor.varHandle(groupElement("iConfiguration"))
    val config_descriptor_bmAttributes = config_descriptor.varHandle(groupElement("bmAttributes"))
    val config_descriptor_MaxPower = config_descriptor.varHandle(groupElement("MaxPower"))
    val config_descriptor_interface = config_descriptor.varHandle(groupElement("interface"))
    val config_descriptor_extra = config_descriptor.varHandle(groupElement("extra"))
    val config_descriptor_extra_length = config_descriptor.varHandle(groupElement("extra_length"))

    val `interface` = cStructLayout(
        ADDRESS.withName("altsetting"),
        JAVA_INT.withName("num_altsetting"),
    )

    val interface_altsetting = `interface`.varHandle(groupElement("altsetting"))
    val interface_num_altsetting = `interface`.varHandle(groupElement("num_altsetting"))

    val interface_descriptor = cStructLayout(
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

    val interface_descriptor_bInterfaceNumber = interface_descriptor.varHandle(groupElement("bInterfaceNumber"))
    val interface_descriptor_bAlternateSetting = interface_descriptor.varHandle(groupElement("bAlternateSetting"))
    val interface_descriptor_bNumEndpoints = interface_descriptor.varHandle(groupElement("bNumEndpoints"))
    val interface_descriptor_bInterfaceClass = interface_descriptor.varHandle(groupElement("bInterfaceClass"))
    val interface_descriptor_bInterfaceSubClass = interface_descriptor.varHandle(groupElement("bInterfaceSubClass"))
    val interface_descriptor_bInterfaceProtocol = interface_descriptor.varHandle(groupElement("bInterfaceProtocol"))
    val interface_descriptor_iInterface = interface_descriptor.varHandle(groupElement("iInterface"))
    val interface_descriptor_endpoint = interface_descriptor.varHandle(groupElement("endpoint"))
    val interface_descriptor_extra = interface_descriptor.varHandle(groupElement("extra"))
    val interface_descriptor_extra_length = interface_descriptor.varHandle(groupElement("extra_length"))

    val endpoint_descriptor = cStructLayout(
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

    val endpoint_descriptor_bEndpointAddress = endpoint_descriptor.varHandle(groupElement("bEndpointAddress"))
    val endpoint_descriptor_bmAttributes = endpoint_descriptor.varHandle(groupElement("bmAttributes"))
    val endpoint_descriptor_wMaxPacketSize = endpoint_descriptor.varHandle(groupElement("wMaxPacketSize"))
    val endpoint_descriptor_bInterval = endpoint_descriptor.varHandle(groupElement("bInterval"))
    val endpoint_descriptor_bRefresh = endpoint_descriptor.varHandle(groupElement("bRefresh"))
    val endpoint_descriptor_bSynchAddress = endpoint_descriptor.varHandle(groupElement("bSynchAddress"))
    val endpoint_descriptor_extra = endpoint_descriptor.varHandle(groupElement("extra"))
    val endpoint_descriptor_extra_length = endpoint_descriptor.varHandle(groupElement("extra_length"))

    fun checkReturnCode(code: Int) {
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
}

private fun cStructLayout(vararg elements: MemoryLayout): GroupLayout {
    TODO()
}
