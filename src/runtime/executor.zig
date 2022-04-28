const std = @import("std");

const arm = @import("../arm.zig");
const bootrom = @import("bootrom.zig");
const config = @import("config.zig");

const Continuation = @import("Continuation.zig");
const ContinuationSet = @import("ContinuationSet.zig");
const CoreLocal = @import("core_local.zig").CoreLocal;
const HardwareSpinLock = @import("HardwareSpinLock.zig");

const ready_continuations_lock = HardwareSpinLock.init(0);
var ready_continuations = ContinuationSet.init();
var current_priority_level = CoreLocal(?config.PriorityLevel).init(null);

pub fn submit(continuation: *Continuation, priority_level: config.PriorityLevel) void {
    arm.disableInterrupts();
    defer arm.enableInterrupts();

    ready_continuations_lock.lock();
    defer ready_continuations_lock.unlock();

    ready_continuations.add(continuation, priority_level);
}

pub fn run() noreturn {
    while (true) {
        const prioritized_continuation = while (true) {
            if (blk: {
                arm.disableInterrupts();
                defer arm.enableInterrupts();

                ready_continuations_lock.lock();
                defer ready_continuations_lock.unlock();

                break :blk ready_continuations.removePrioritized();
            }) |pc| {
                break pc;
            }

            arm.waitForEvent();
        };

        current_priority_level.set(prioritized_continuation.priority_level);
        resume prioritized_continuation.continuation.frame;
        current_priority_level.set(null);
    }
}
