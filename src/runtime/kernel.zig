const std = @import("std");

const arm = @import("../arm.zig");
const bootrom = @import("bootrom.zig");
const core_local = @import("core_local.zig");
const hardware_spin_lock = @import("hardware_spin_lock.zig");
const task = @import("task.zig");

const CoreLocal = core_local.CoreLocal;
const HardwareSpinLock = hardware_spin_lock.HardwareSpinLock;
const Task = task.Task;
const TaskSet = task.TaskSet;

pub const Configuration = struct {
    core0_stack_top: usize = 0x20042000,
    core0_stack_size: usize = 0x1000,
    core1_stack_top: usize = 0x20041000,
    core1_stack_size: usize = 0x1000,
    num_priority_levels: comptime_int = 1,
};

pub fn Kernel(comptime config: Configuration) type {
    return struct {
        const TaskSet = task.TaskSet(config.num_priority_levels);

        const ready_tasks_lock = HardwareSpinLock.init(0);
        var ready_tasks = TaskSet(config.num_priority_levels).init();

        var current_task_priority = CoreLocal(?TaskSet.Priority).init(null);

        pub fn setPriority(priority: TaskSet.Priority) void {
            std.debug.assert(current_task_priority.get() != null);
            current_task_priority.set(priority);
        }

        pub fn currentPriority() TaskSet.Priority {
            return current_task_priority.get().*;
        }

        pub fn yield() void {
            var task = Task.init(@frame());
            suspend {
                scheduleTask(task, current_task_priority.get().*);
            }
        }

        fn scheduleTask(task: *Task, priority: TaskSet.Priority) void {
            arm.disableInterrupts();
            defer arm.enableInterrupts();

            ready_tasks_lock.lock();
            defer ready_tasks_lock.unlock();

            ready_tasks.add(task, priority);
        }

        fn runTasks() noreturn {
            while (true) {
                const prioritized_task = blk: {
                    arm.disableInterrupts();
                    defer arm.enableInterrupts();

                    ready_tasks_lock.lock();
                    defer ready_tasks_lock.unlock();

                    break :blk ready_tasks.removePrioritized();
                } orelse std.debug.todo("");

                current_task_priority.set(prioritized_task.priority);
                resume prioritized_task.task.frame; // FIXME: is this safe if the Task object's lifetime ends?
                current_task_priority.set(null);
            }
        }

        pub fn exportVectorTable() void {
            @export(vector_table, .{ .name = "__vectors", .section = "vectors" });
            @export(vector_table, .{ .name = "__VECTOR_TABLE", .section = "vectors" });
        }

        const vector_table = VectorTable{
            .stack_top = config.core0_stack_top,
            .reset = handleReset,
            .nmi = handleNmi,
            .hardfault = handleHardfault,
            .svcall = handleSvcall,
            .pendsv = handlePendsv,
            .systick = handleSystick,
            .irq0 = handleTimerIrq0,
            .irq1 = handleTimerIrq1,
            .irq2 = handleTimerIrq2,
            .irq3 = handleTimerIrq3,
            .irq4 = handlePwmIrqWrap,
            .irq5 = handleUsbctrlIrq,
            .irq6 = handleXipIrq,
            .irq7 = handlePio0Irq0,
            .irq8 = handlePio0Irq1,
            .irq9 = handlePio1Irq0,
            .irq10 = handlePio1Irq1,
            .irq11 = handleDmaIrq0,
            .irq12 = handleDmaIrq1,
            .irq13 = handleIoIrqBank0,
            .irq14 = handleIoIrqQspi,
            .irq15 = handleSioIrqProc0,
            .irq16 = handleSioIrqProc1,
            .irq17 = handleClocksIrq,
            .irq18 = handleSpi0Irq,
            .irq19 = handleSpi1Irq,
            .irq20 = handleUart0Irq,
            .irq21 = handleUart1Irq,
            .irq22 = handleAdcIrqFifo,
            .irq23 = handleI2c0Irq,
            .irq24 = handleI2c1Irq,
            .irq25 = handleRtcIrq,
            .irq26 = handleUnconnectedIrq,
            .irq27 = handleUnconnectedIrq,
            .irq28 = handleUnconnectedIrq,
            .irq29 = handleUnconnectedIrq,
            .irq30 = handleUnconnectedIrq,
            .irq31 = handleUnconnectedIrq,
        };

        fn handleReset() callconv(.C) noreturn {
            // Zero out .bss
            const bss_start = @extern([*]volatile u32, .{ .name = "__bss_start__" });
            const bss_end = @extern([*]volatile u32, .{ .name = "__bss_end__" });
            for (bss_start[0 .. (@ptrToInt(bss_end) - @ptrToInt(bss_start)) / @sizeOf(u32)]) |*word| word.* = 0;

            // Initialize .data
            const data_source = @extern([*]const u32, .{ .name = "__data_source__" });
            const data_start = @extern([*]volatile u32, .{ .name = "__data_start__" });
            const data_end = @extern([*]volatile u32, .{ .name = "__data_end__" });
            for (data_start[0 .. (@ptrToInt(data_end) - @ptrToInt(data_start)) / @sizeOf(u32)]) |*word, i| word.* = data_source[i];

            // Link library routines from bootrom
            bootrom.link();
        }

        fn handleNmi() callconv(.C) void {
            return;
        }

        fn handleHardfault() callconv(.C) void {
            asm volatile (
                \\ 1: bkpt 0x0000
                \\    b.n 1b
            );

            unreachable;
        }

        fn handleSvcall() callconv(.C) void {
            return;
        }

        fn handlePendsv() callconv(.C) void {
            return;
        }

        fn handleSystick() callconv(.C) void {
            return;
        }

        fn handleTimerIrq0() callconv(.C) void {
            return;
        }

        fn handleTimerIrq1() callconv(.C) void {
            return;
        }

        fn handleTimerIrq2() callconv(.C) void {
            return;
        }

        fn handleTimerIrq3() callconv(.C) void {
            return;
        }

        fn handlePwmIrqWrap() callconv(.C) void {
            return;
        }

        fn handleUsbctrlIrq() callconv(.C) void {
            return;
        }

        fn handleXipIrq() callconv(.C) void {
            return;
        }

        fn handlePio0Irq0() callconv(.C) void {
            return;
        }

        fn handlePio0Irq1() callconv(.C) void {
            return;
        }

        fn handlePio1Irq0() callconv(.C) void {
            return;
        }

        fn handlePio1Irq1() callconv(.C) void {
            return;
        }

        fn handleDmaIrq0() callconv(.C) void {
            return;
        }

        fn handleDmaIrq1() callconv(.C) void {
            return;
        }

        fn handleIoIrqBank0() callconv(.C) void {
            return;
        }

        fn handleIoIrqQspi() callconv(.C) void {
            return;
        }

        fn handleSioIrqProc0() callconv(.C) void {
            return;
        }

        fn handleSioIrqProc1() callconv(.C) void {
            return;
        }

        fn handleClocksIrq() callconv(.C) void {
            return;
        }

        fn handleSpi0Irq() callconv(.C) void {
            return;
        }

        fn handleSpi1Irq() callconv(.C) void {
            return;
        }

        fn handleUart0Irq() callconv(.C) void {
            return;
        }

        fn handleUart1Irq() callconv(.C) void {
            return;
        }

        fn handleAdcIrqFifo() callconv(.C) void {
            return;
        }

        fn handleI2c0Irq() callconv(.C) void {
            return;
        }

        fn handleI2c1Irq() callconv(.C) void {
            return;
        }

        fn handleRtcIrq() callconv(.C) void {
            return;
        }

        fn handleUnconnectedIrq() callconv(.C) void {
            return;
        }
    };
}

const VectorTable = extern struct {
    stack_top: usize,
    reset: fn () callconv(.C) void,
    nmi: fn () callconv(.C) void,
    hardfault: fn () callconv(.C) void,
    reserved_3: fn () callconv(.C) void = reservedVector,
    reserved_4: fn () callconv(.C) void = reservedVector,
    reserved_5: fn () callconv(.C) void = reservedVector,
    reserved_6: fn () callconv(.C) void = reservedVector,
    reserved_7: fn () callconv(.C) void = reservedVector,
    reserved_8: fn () callconv(.C) void = reservedVector,
    reserved_9: fn () callconv(.C) void = reservedVector,
    svcall: fn () callconv(.C) void,
    reserved_11: fn () callconv(.C) void = reservedVector,
    reserved_12: fn () callconv(.C) void = reservedVector,
    pendsv: fn () callconv(.C) void,
    systick: fn () callconv(.C) void,
    irq0: fn () callconv(.C) void,
    irq1: fn () callconv(.C) void,
    irq2: fn () callconv(.C) void,
    irq3: fn () callconv(.C) void,
    irq4: fn () callconv(.C) void,
    irq5: fn () callconv(.C) void,
    irq6: fn () callconv(.C) void,
    irq7: fn () callconv(.C) void,
    irq8: fn () callconv(.C) void,
    irq9: fn () callconv(.C) void,
    irq10: fn () callconv(.C) void,
    irq11: fn () callconv(.C) void,
    irq12: fn () callconv(.C) void,
    irq13: fn () callconv(.C) void,
    irq14: fn () callconv(.C) void,
    irq15: fn () callconv(.C) void,
    irq16: fn () callconv(.C) void,
    irq17: fn () callconv(.C) void,
    irq18: fn () callconv(.C) void,
    irq19: fn () callconv(.C) void,
    irq20: fn () callconv(.C) void,
    irq21: fn () callconv(.C) void,
    irq22: fn () callconv(.C) void,
    irq23: fn () callconv(.C) void,
    irq24: fn () callconv(.C) void,
    irq25: fn () callconv(.C) void,
    irq26: fn () callconv(.C) void,
    irq27: fn () callconv(.C) void,
    irq28: fn () callconv(.C) void,
    irq29: fn () callconv(.C) void,
    irq30: fn () callconv(.C) void,
    irq31: fn () callconv(.C) void,
};

fn reservedVector() callconv(.C) void {
    @panic("called from reserved entry in vector table");
}
