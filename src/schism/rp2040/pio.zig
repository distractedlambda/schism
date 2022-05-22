const std = @import("std");

pub const InstructionConfig = struct {
    sideset_count: u3,
};

pub const JmpCondition = enum(u3) {
    Always,
    NotX,
    XPostdecrement,
    NotY,
    YPostdecrement,
    XNotEqualToY,
    Pin,
    NotOsr,
};

pub const WaitSource = enum(u2) {
    Gpio,
    Pin,
    Irq,
};

pub const InSource = enum(u3) {
    Pins = 0,
    X = 1,
    Y = 2,
    Null = 3,
    Isr = 6,
    Osr = 7,
};

pub const OutDestination = enum(u3) {
    Pins,
    X,
    Y,
    Null,
    PinDirs,
    Pc,
    Isr,
    Exec,
};

pub const MovDestination = enum(u3) {
    Pins = 0,
    X = 1,
    Y = 2,
    Exec = 4,
    Pc = 5,
    Isr = 6,
    Osr = 7,
};

pub const MovOperation = enum(u2) {
    None,
    Invert,
    BitReverse,
};

pub const MovSource = enum(u3) {
    Pins = 0,
    X = 1,
    Y = 2,
    Null = 3,
    Status = 5,
    Isr = 6,
    Osr = 7,
};

pub fn Instruction(comptime config: InstructionConfig) type {
    return union(enum) {
        Jmp: Jmp,
        Wait: Wait,
        In: In,
        Out: Out,
        Push: Push,
        Pull: Pull,
        Mov: Mov,

        pub const Delay = std.meta.Int(.unsigned, 5 - config.sideset_count);

        pub const SideSet = std.meta.Int(.unsigned, config.sideset_count);

        pub const Jmp = struct {
            delay: Delay = 0,
            sideset: SideSet = 0,
            condition: JmpCondition = .Always,
            address: u5,
        };

        pub const Wait = struct {
            delay: Delay = 0,
            sideset: SideSet = 0,
            polarity: u1,
            source: WaitSource,
            index: u5,
        };

        pub const In = struct {
            delay: Delay = 0,
            sideset: SideSet = 0,
            source: InSource,
            bit_count: u6,
        };

        pub const Out = struct {
            delay: Delay = 0,
            setset: SideSet = 0,
            destination: OutDestination,
            bit_count: u6,
        };

        pub const Push = struct {
            delay: Delay = 0,
            sideset: SideSet = 0,
            if_full: bool = false,
            block: bool = true,
        };

        pub const Pull = struct {
            delay: Delay = 0,
            sideset: SideSet = 0,
            if_empty: bool = false,
            block: bool = true,
        };

        pub const Mov = struct {
            delay: Delay = 0,
            sideset: SideSet = 0,
            destination: MovDestination,
            op: MovOperation = .None,
            source: MovSource,
        };
    };
}
