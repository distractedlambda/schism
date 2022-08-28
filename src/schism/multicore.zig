const rp2040 = @import("rp2040.zig");

pub const CoreLocal = @import("multicore/core_local.zig").CoreLocal;

pub inline fn currentCore() u1 {
    return @intCast(u1, rp2040.sio.cpuid.readNonVolatile());
}
