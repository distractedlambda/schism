const arm = @import("../arm.zig");
const llvmintrin = @import("../llvmintrin.zig");
const rp2040 = @import("../rp2040.zig");

pub fn lock(index: rp2040.sio.spinlock.Index) void {
    while (rp2040.sio.spinlock.read(index) != 0) {}
    arm.dataMemoryBarrier();
}

pub fn unlock(index: rp2040.sio.spinlock.Index) void {
    arm.dataMemoryBarrier();
    rp2040.sio.spinlock.write(index, 0);
}
