const std = @import("std");

pub fn build(builder: *std.build.Builder) void {
    const rp2040_target = std.zig.CrossTarget{
        .cpu_arch = .thumb,
        .cpu_model = .{ .explicit = &std.Target.arm.cpu.cortex_m0plus },
        .os_tag = .freestanding,
        .abi = .eabi,
    };

    const exe = builder.addExecutable("schism.elf", "src/main.zig");
    exe.setTarget(rp2040_target);
    exe.setBuildMode(.ReleaseSafe);
    exe.setLinkerScriptPath(std.build.FileSource.relative("src/picosystem/lscript.ld"));
    exe.install();

    const exe_tests = builder.addTest("src/main.zig");
    exe_tests.setBuildMode(.ReleaseSafe);

    const test_step = builder.step("test", "Run unit tests");
    test_step.dependOn(&exe_tests.step);
}
