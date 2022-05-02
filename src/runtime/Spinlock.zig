locked: bool,

pub fn init() @This() {
    return .{ .locked = false };
}

pub fn lock(self: *@This()) void {
    while (@atomicRmw(bool, &self.locked, .Xchg, true, .Acquire)) {}
}

pub fn unlock(self: *@This()) void {
    @atomicStore(bool, &self.locked, false, .Release);
}
