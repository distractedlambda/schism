const std = @import("std");

const device = @import("device.zig");

const Config = @import("../Config.zig");
const Mutex = @import("../Mutex.zig");

const interface_number = blk: {
    for (Config.resolved.usb.?.Device.interfaces) |interface, inumber| {
        if (!std.mem.eql(u8, interface.name orelse &.{}, "Schism Logging")) {
            continue;
        }

        std.debug.assert(interface.endpoints.len == 1);
        std.debug.assert(interface.endpoints[0].direction == .In);

        break :blk inumber;
    }

    unreachable;
};

var mutex = Mutex.init();

const WriterContext = struct {
    connection_id: device.ConnectionId,
    buffer: device.TransmitBuffer,
};

fn write(context: *WriterContext, data: []const u8) device.Error!usize {
    const amount_sent = context.buffer.write(data);

    if (context.buffer.isFull()) {
        context.buffer.submit();
        context.buffer = try device.nextTransmitBuffer(context.connection_id, interface_number, 0);
    }

    return amount_sent;
}

pub fn log(
    comptime level: std.log.Level,
    comptime scope: @Type(.EnumLiteral),
    comptime format: []const u8,
    args: anytype,
) void {
    mutex.lock();
    defer mutex.unlock();

    const connection_id = device.tryConnect() orelse return;

    var writer_context = WriterContext{
        .connection_id = connection_id,
        .buffer = device.nextTransmitBuffer(connection_id, interface_number, 0) catch return,
    };

    const writer = std.io.Writer(*WriterContext, device.Error, write){ .context = &writer_context };

    writer.writeByte(
        comptime @bitCast(
            u8,
            MessageHeader{
                .level = @intToEnum(Level, @enumToInt(level)),
                .scope_len_minus_one = @tagName(scope).len - 1,
            },
        ),
    ) catch return;

    writer.writeAll(@tagName(scope)) catch return;

    writer.print(format, args) catch return;

    writer_context.buffer.submit();
}

const Level = enum(u2) {
    err,
    warn,
    info,
    debug,
};

const MessageHeader = packed struct {
    level: Level,
    scope_len_minus_one: u6,
};
