const multicore = @import("../multicore.zig");

pub fn CoreLocal(comptime T_: type) type {
    return struct {
        pub const T = T_;

        storage: [2]T,

        pub inline fn init(value: T) @This() {
            return .{ .storage = [2]T{ value, value } };
        }

        pub inline fn constPtr(self: *const @This()) *const T {
            return &self.storage[multicore.currentCore()];
        }

        pub inline fn ptr(self: *@This()) *T {
            return &self.storage[multicore.currentCore()];
        }

        pub inline fn get(self: *const @This()) T {
            return self.storage[multicore.currentCore()];
        }

        pub inline fn set(self: *@This(), value: T) void {
            self.storage[multicore.currentCore()] = value;
        }
    };
}
