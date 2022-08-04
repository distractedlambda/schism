const bits = @import("../bits.zig");

const PeripheralRegister = @import("peripheral_register.zig").PeripheralRegister;
const Register = @import("register.zig").Register;
const RegisterArray = @import("register_array.zig").RegisterArray;

const base_address = 0x50110000;

pub const dpram_base_address = 0x50100000;

pub const IntepDir = enum(u1) {
    In,
    Out,
};

pub const addr_endp = RegisterArray(16, base_address, 0x04, .{
    .Record = &.{
        .{
            .name = "intep_preamble",
            .type = bool,
            .lsb = 26,
            .default = &false,
        },
        .{
            .name = "intep_dir",
            .type = IntepDir,
            .lsb = 25,
            .default = &IntepDir.In,
        },
        .{
            .name = "endpoint",
            .type = u4,
            .lsb = 16,
            .default = &@as(u4, 0),
        },
        .{
            .name = "address",
            .type = u7,
            .lsb = 0,
            .default = &@as(u7, 0),
        },
    },
});

pub const HostNdevice = enum(u1) {
    Device,
    Host,
};

pub const main_ctrl = Register(base_address + 0x40, .{
    .Record = &.{
        .{
            .name = "sim_timing",
            .type = bool,
            .lsb = 31,
            .default = &false,
        },
        .{
            .name = "host_ndevice",
            .type = HostNdevice,
            .lsb = 1,
            .default = &HostNdevice.Device,
        },
        .{
            .name = "controller_en",
            .type = bool,
            .lsb = 0,
            .default = &false,
        },
    },
});

pub const sof_wr = Register(base_address + 0x44, u11);

pub const sof_rd = Register(base_address + 0x48, u11);

pub const sie_ctrl = PeripheralRegister(base_address + 0x4c, .{
    .Record = &.{
        .{
            .name = "ep0_int_stall",
            .type = bool,
            .lsb = 31,
            .default = &false,
        },
        .{
            .name = "ep0_double_buf",
            .type = bool,
            .lsb = 30,
            .default = &false,
        },
        .{
            .name = "ep0_int_1buf",
            .type = bool,
            .lsb = 29,
            .default = &false,
        },
        .{
            .name = "ep0_int_2buf",
            .type = bool,
            .lsb = 28,
            .default = &false,
        },
        .{
            .name = "ep0_int_nak",
            .type = bool,
            .lsb = 27,
            .default = &false,
        },
        .{
            .name = "direct_en",
            .type = bool,
            .lsb = 26,
            .default = &false,
        },
        .{
            .name = "direct_dp",
            .type = bool,
            .lsb = 25,
            .default = &false,
        },
        .{
            .name = "direct_dm",
            .type = bool,
            .lsb = 24,
            .default = &false,
        },
        .{
            .name = "transceiver_pd",
            .type = bool,
            .lsb = 18,
            .default = &false,
        },
        .{
            .name = "rpu_opt",
            .type = bool,
            .lsb = 17,
            .default = &false,
        },
        .{
            .name = "pullup_en",
            .type = bool,
            .lsb = 16,
            .default = &false,
        },
        .{
            .name = "pulldown_en",
            .type = bool,
            .lsb = 15,
            .default = &false,
        },
        .{
            .name = "reset_bus",
            .type = bool,
            .lsb = 13,
            .default = &false,
        },
        .{
            .name = "resume",
            .type = bool,
            .lsb = 12,
            .default = &false,
        },
        .{
            .name = "vbus_en",
            .type = bool,
            .lsb = 11,
            .default = &false,
        },
        .{
            .name = "keep_alive_en",
            .type = bool,
            .lsb = 10,
            .default = &false,
        },
        .{
            .name = "sof_en",
            .type = bool,
            .lsb = 9,
            .default = &false,
        },
        .{
            .name = "sof_sync",
            .type = bool,
            .lsb = 8,
            .default = &false,
        },
        .{
            .name = "preamble_en",
            .type = bool,
            .lsb = 6,
            .default = &false,
        },
        .{
            .name = "stop_trans",
            .type = bool,
            .lsb = 4,
            .default = &false,
        },
        .{
            .name = "receive_data",
            .type = bool,
            .lsb = 3,
            .default = &false,
        },
        .{
            .name = "send_data",
            .type = bool,
            .lsb = 2,
            .default = &false,
        },
        .{
            .name = "send_setup",
            .type = bool,
            .lsb = 1,
            .default = &false,
        },
        .{
            .name = "start_trans",
            .type = bool,
            .lsb = 0,
            .default = &false,
        },
    },
});

pub const Speed = enum(u2) {
    Disconnected,
    LowSpeed,
    FullSpeed,
    _,
};

pub const sie_status = PeripheralRegister(base_address + 0x50, .{
    .Record = &.{
        .{
            .name = "data_seq_error",
            .type = bool,
            .lsb = 31,
            .default = &false,
        },
        .{
            .name = "ack_rec",
            .type = bool,
            .lsb = 30,
            .default = &false,
        },
        .{
            .name = "stall_rec",
            .type = bool,
            .lsb = 29,
            .default = &false,
        },
        .{
            .name = "nak_rec",
            .type = bool,
            .lsb = 28,
            .default = &false,
        },
        .{
            .name = "rx_timeout",
            .type = bool,
            .lsb = 27,
            .default = &false,
        },
        .{
            .name = "rx_overflow",
            .type = bool,
            .lsb = 26,
            .default = &false,
        },
        .{
            .name = "bit_stuff_error",
            .type = bool,
            .lsb = 25,
            .default = &false,
        },
        .{
            .name = "crc_error",
            .type = bool,
            .lsb = 24,
            .default = &false,
        },
        .{
            .name = "bus_reset",
            .type = bool,
            .lsb = 19,
            .default = &false,
        },
        .{
            .name = "trans_complete",
            .type = bool,
            .lsb = 18,
            .default = &false,
        },
        .{
            .name = "setup_rec",
            .type = bool,
            .lsb = 17,
            .default = &false,
        },
        .{
            .name = "connected",
            .type = bool,
            .lsb = 16,
            .default = &false,
        },
        .{
            .name = "resume",
            .type = bool,
            .lsb = 11,
            .default = &false,
        },
        .{
            .name = "vbus_over_curr",
            .type = bool,
            .lsb = 10,
            .default = &false,
        },
        .{
            .name = "speed",
            .type = Speed,
            .lsb = 8,
            .default = &Speed.Disconnected,
        },
        .{
            .name = "suspended",
            .type = bool,
            .lsb = 4,
            .default = &false,
        },
        .{
            .name = "line_state",
            .type = u2,
            .lsb = 2,
            .default = &@as(u2, 0),
        },
        .{
            .name = "vbus_detected",
            .type = bool,
            .lsb = 0,
            .default = &false,
        },
    },
});

pub const int_ep_ctrl = Register(base_address + 0x54, .{
    .{
        .name = "int_ep_active",
        .type = u15,
        .lsb = 1,
        .default = 0,
    },
});

pub const buff_status = Register(base_address + 0x58, .{ .Scalar = u32 });

pub const buff_cpu_should_handle = Register(base_address + 0x5c, .{ .Scalar = u32 });

pub const ep_abort = Register(base_address + 0x60, .{ .Scalar = u32 });

pub const ep_abort_done = Register(base_address + 0x64, .{ .Scalar = u32 });

pub const ep_stall_arm = Register(base_address + 0x68, .{ .Scalar = u1 });

pub const nak_poll = Register(base_address + 0x6c, .{
    .{
        .name = "delay_fs",
        .type = u10,
        .lsb = 16,
        .default = 0x10,
    },
    .{
        .name = "delay_ls",
        .type = u10,
        .lsb = 0,
        .default = 0x10,
    },
});

pub const ep_status_stall_nak = Register(base_address + 0x70, u32);

pub const usb_muxing = Register(base_address + 0x74, .{
    .Record = &.{
        .{
            .name = "softcon",
            .type = bool,
            .lsb = 3,
            .default = &false,
        },
        .{
            .name = "to_digital_pad",
            .type = bool,
            .lsb = 2,
            .default = &false,
        },
        .{
            .name = "to_extphy",
            .type = bool,
            .lsb = 1,
            .default = &false,
        },
        .{
            .name = "to_phy",
            .type = bool,
            .lsb = 0,
            .default = &false,
        },
    },
});

pub const usb_pwr = Register(base_address + 0x78, .{
    .Record = &.{
        .{
            .name = "overcurr_detect_en",
            .type = bool,
            .lsb = 5,
            .default = &false,
        },
        .{
            .name = "overcurr_detect",
            .type = bool,
            .lsb = 4,
            .default = &false,
        },
        .{
            .name = "vbus_detect_override_en",
            .type = bool,
            .lsb = 3,
            .default = &false,
        },
        .{
            .name = "vbus_detect",
            .type = bool,
            .lsb = 2,
            .default = &false,
        },
        .{
            .name = "vbus_en_override_en",
            .type = bool,
            .lsb = 1,
            .default = &false,
        },
        .{
            .name = "vbus_en",
            .type = bool,
            .lsb = 0,
            .default = &false,
        },
    },
});

const interrupt_spec = bits.BitStructSpec{
    .Record = &.{
        .{
            .name = "ep_stall_nak",
            .type = bool,
            .lsb = 19,
            .default = &false,
        },
        .{
            .name = "abort_done",
            .type = bool,
            .lsb = 18,
            .default = &false,
        },
        .{
            .name = "dev_sof",
            .type = bool,
            .lsb = 17,
            .default = &false,
        },
        .{
            .name = "setup_req",
            .type = bool,
            .lsb = 16,
            .default = &false,
        },
        .{
            .name = "dev_resume_from_host",
            .type = bool,
            .lsb = 15,
            .default = &false,
        },
        .{
            .name = "dev_suspend",
            .type = bool,
            .lsb = 14,
            .default = &false,
        },
        .{
            .name = "dev_conn_dis",
            .type = bool,
            .lsb = 13,
            .default = &false,
        },
        .{
            .name = "bus_reset",
            .type = bool,
            .lsb = 12,
            .default = &false,
        },
        .{
            .name = "vbus_detect",
            .type = bool,
            .lsb = 11,
            .default = &false,
        },
        .{
            .name = "stall",
            .type = bool,
            .lsb = 10,
            .default = &false,
        },
        .{
            .name = "error_crc",
            .type = bool,
            .lsb = 9,
            .default = &false,
        },
        .{
            .name = "error_bit_stuff",
            .type = bool,
            .lsb = 8,
            .default = &false,
        },
        .{
            .name = "error_rx_overflow",
            .type = bool,
            .lsb = 7,
            .default = &false,
        },
        .{
            .name = "error_rx_timeout",
            .type = bool,
            .lsb = 6,
            .default = &false,
        },
        .{
            .name = "error_data_seq",
            .type = bool,
            .lsb = 5,
            .default = &false,
        },
        .{
            .name = "buff_status",
            .type = bool,
            .lsb = 4,
            .default = &false,
        },
        .{
            .name = "trans_complete",
            .type = bool,
            .lsb = 3,
            .default = &false,
        },
        .{
            .name = "host_sof",
            .type = bool,
            .lsb = 2,
            .default = &false,
        },
        .{
            .name = "host_resume",
            .type = bool,
            .lsb = 1,
            .default = &false,
        },
        .{
            .name = "host_conn_dis",
            .type = bool,
            .lsb = 0,
            .default = &false,
        },
    },
};

pub const intr = Register(base_address + 0x8c, interrupt_spec);

pub const inte = Register(base_address + 0x90, interrupt_spec);

pub const intf = Register(base_address + 0x94, interrupt_spec);

pub const ints = Register(base_address + 0x98, interrupt_spec);

pub const DeviceEpCtrlType = enum(u2) {
    Control,
    Isochronous,
    Bulk,
    Interrupt,
};

pub const device_ep_ctrl = RegisterArray(30, dpram_base_address + 0x08, 0x04, .{
    .Record = &.{
        .{
            .name = "en",
            .type = bool,
            .lsb = 31,
            .default = &false,
        },
        .{
            .name = "double_buf",
            .type = bool,
            .lsb = 30,
            .default = &false,
        },
        .{
            .name = "int_1buf",
            .type = bool,
            .lsb = 29,
            .default = &false,
        },
        .{
            .name = "int_2buf",
            .type = bool,
            .lsb = 28,
            .default = &false,
        },
        .{
            .name = "type",
            .type = DeviceEpCtrlType,
            .lsb = 26,
            .default = &DeviceEpCtrlType.Control,
        },
        .{
            .name = "int_stall",
            .type = bool,
            .lsb = 17,
            .default = &false,
        },
        .{
            .name = "int_nak",
            .type = bool,
            .lsb = 16,
            .default = &false,
        },
        .{
            .name = "buf_address",
            .type = u10,
            .lsb = 6,
            .default = &@as(u10, 0),
        },
    },
});

pub const IsochronousOffset = enum(u2) {
    @"128",
    @"256",
    @"512",
    @"1024",
};

pub const device_ep_buf_ctrl = RegisterArray(32, dpram_base_address + 0x80, 0x04, .{
    .Record = &.{
        .{
            .name = "buf1_full",
            .type = bool,
            .lsb = 31,
            .default = &false,
        },
        .{
            .name = "buf1_last_in_transfer",
            .type = bool,
            .lsb = 30,
            .default = &false,
        },
        .{
            .name = "buf1_data_pid",
            .type = u1,
            .lsb = 29,
            .default = &@as(u1, 0),
        },
        .{
            .name = "buf1_isochronous_offset",
            .type = IsochronousOffset,
            .lsb = 27,
            .default = &IsochronousOffset.@"128",
        },
        .{
            .name = "buf1_available",
            .type = bool,
            .lsb = 26,
            .default = &false,
        },
        .{
            .name = "buf1_len",
            .type = u10,
            .lsb = 16,
            .default = &@as(u10, 0),
        },
        .{
            .name = "buf0_full",
            .type = bool,
            .lsb = 15,
            .default = &false,
        },
        .{
            .name = "buf0_last_in_transfer",
            .type = bool,
            .lsb = 14,
            .default = &false,
        },
        .{
            .name = "buf0_data_pid",
            .type = u1,
            .lsb = 13,
            .default = &@as(u1, 0),
        },
        .{
            .name = "reset_buffer_select",
            .type = bool,
            .lsb = 12,
            .default = &false,
        },
        .{
            .name = "stall",
            .type = bool,
            .lsb = 11,
            .default = &false,
        },
        .{
            .name = "buf0_available",
            .type = bool,
            .lsb = 10,
            .default = &false,
        },
        .{
            .name = "buf0_len",
            .type = u10,
            .lsb = 0,
            .default = &@as(u10, 0),
        },
    },
});
