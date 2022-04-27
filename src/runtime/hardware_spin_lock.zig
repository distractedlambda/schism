const arm = @import("../arm.zig");
const llvmintrin = @import("../llvmintrin.zig");
const rp2040 = @import("../rp2040.zig");

pub const HardwareSpinLock = struct {
    index: rp2040.sio.spinlock.Index,

    pub fn init(index: rp2040.sio.spinlock.Index) @This() {
        return .{ .index = index };
    }

    pub fn lock(self: @This()) void {
        lockIndexed(self.index);
    }

    pub fn unlock(self: @This()) void {
        unlockIndexed(self.index);
    }
};

pub fn lockIndexed(index: rp2040.sio.spinlock.Index) void {
    while (llvmintrin.expect(rp2040.sio.spinlock.read(index), 0) != 0) {}
    arm.dmb();
}

pub fn unlockIndexed(index: rp2040.sio.spinlock.Index) void {
    arm.dmb();
    rp2040.sio.spinlock.write(index, 0);
}
