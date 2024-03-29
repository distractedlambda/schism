const std = @import("std");

pub fn build(builder: *std.build.Builder) void {
    const rp2040_target = std.zig.CrossTarget{
        .cpu_arch = .thumb,
        .cpu_model = .{ .explicit = &std.Target.arm.cpu.cortex_m0plus },
        .os_tag = .freestanding,
        .abi = .gnueabi,
    };

    const build_mode = builder.standardReleaseOptions();

    const exe = builder.addExecutable("schism.elf", "src/main.zig");
    exe.single_threaded = true;
    exe.link_function_sections = true;
    exe.setTarget(rp2040_target);
    exe.setBuildMode(build_mode);
    exe.setLinkerScriptPath(std.build.FileSource.relative("src/schism/picosystem/lscript.ld"));
    exe.install();

    const exe_tests = builder.addTest("src/main.zig");
    exe_tests.setBuildMode(build_mode);

    const test_step = builder.step("test", "Run unit tests");
    test_step.dependOn(&exe_tests.step);
}
