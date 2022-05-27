const executor = @import("../../executor.zig");

const Continuation = executor.Continuation;
const ContinuationQueue = executor.ContinuationQueue;

pub const Id = enum(usize) { _ };

pub var configured = false;
pub var current = @intToEnum(Id, 0);
pub var waiters = ContinuationQueue{};

pub fn connect() Id {
    while (!configured) {
        var continuation = Continuation.init(@frame());

        suspend {
            waiters.pushBack(&continuation);
        }
    }

    return current;
}

pub fn tryConnect() ?Id {
    if (!configured) {
        return null;
    }

    return current;
}

pub fn invalidate() void {
    current = @intToEnum(Id, @enumToInt(current) +% 1);
    configured = false;
}
