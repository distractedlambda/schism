const arm = @import("arm.zig");
const rp2040 = @import("rp2040.zig");

const CoreLocal = @import("core_local.zig").CoreLocal;

var lock_counts = CoreLocal([32]u8).init([_]u8{0} ** 32);

fn lockRecursive(key: u5) void {
    lock_counts.ptr()[key] += 1;
    if (lock_counts.ptr()[key] == 1) {
        while (rp2040.sio.spinlock.read(key) == 0) {}
        arm.dataMemoryBarrier();
    }
}

fn unlockRecursive(key: u5) void {
    lock_counts.ptr()[key] -= 1;
    if (lock_counts.ptr()[key] == 0) {
        arm.dataMemoryBarrier();
        rp2040.sio.spinlock.write(key, 0);
    }
}

fn addressToKey(ptr: anytype) u5 {
    return @truncate(u5, @ptrToInt(ptr) / comptime @typeInfo(@TypeOf(ptr)).Pointer.alignment);
}

pub fn lockAddress(ptr: anytype) void {
    lockRecursive(addressToKey(ptr));
}

pub fn unlockAddress(ptr: anytype) void {
    unlockRecursive(addressToKey(ptr));
}
