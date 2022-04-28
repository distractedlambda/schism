const arm = @import("../arm.zig");
const llvmintrin = @import("../llvmintrin.zig");
const rp2040 = @import("../rp2040.zig");

index: rp2040.sio.spinlock.Index,

pub fn init(index: rp2040.sio.spinlock.Index) @This() {
    return .{ .index = index };
}

pub fn lock(self: @This()) void {
    while (llvmintrin.expect(rp2040.sio.spinlock.read(self.index), 0) != 0) {}
    arm.dataMemoryBarrier();
}

pub fn unlock(self: @This()) void {
    arm.dataMemoryBarrier();
    rp2040.sio.spinlock.write(self.index, 0);
}
