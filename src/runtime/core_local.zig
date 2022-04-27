const rp2040 = @import("../rp2040.zig");

pub fn CoreLocal(comptime T: type) type {
    return struct {
        pub const T = T;

        storage: [2]T,

        fn storageIndex() u1 {
            return @intCast(u1, rp2040.sio.cpuid.readNonVolatile());
        }

        pub fn init(value: T) @This() {
            return .{ .storage = [2]T{ value, value } };
        }

        pub fn constPtr(self: *const @This()) *const T {
            return &self.storage[storageIndex()];
        }

        pub fn ptr(self: *@This()) *T {
            return &self.storage[storageIndex()];
        }

        pub fn get(self: *const @This()) T {
            return self.storage[storageIndex()];
        }

        pub fn set(self: *@This(), value: T) void {
            self.storage[storageIndex()] = value;
        }
    };
}