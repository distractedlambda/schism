const std = @import("std");

const project_name = "Schism";

pub fn build(builder: *std.build.Builder) void {
    const pico_sdk_path = getPicoSdkPath();

    const rp2040_target = std.zig.CrossTarget {
        .cpu_arch = .thumb,
        .cpu_model = .{ .explicit = &std.Target.arm.cpu.cortex_m0plus },
        .os_tag = .freestanding,
        .abi = .eabi,
    };

    const build_mode = builder.standardReleaseOptions();

    const second_stage_bootloader = builder.addAssemble("second_stage_bootloader.o", std.fs.path.join(builder.allocator, [][]u8{pico_sdk_path, "src", "rp2_common", "boot_stage2", "boot2_w25q080.S"}));
    second_stage_bootloader.setTarget(rp2040_target);
    second_stage_bootloader.setBuildMode(build_mode);
    second_stage_bootloader.addIncludePath(std.fs.path.join(builder.allocator, [][]u8{pico_sdk_path, "src", "rp2_common", "pico_platform", "include"}));
    second_stage_bootloader.addIncludePath(std.fs.path.join(builder.allocator, [][]u8{pico_sdk_path, "src", "rp2040", "hardware_regs", "include"}));

    const exe = builder.addExecutable("schism.elf", "src/main.zig");
    exe.setTarget(rp2040_target);
    exe.setBuildMode(mode);
    exe.install();

    const exe_tests = builder.addTest("src/main.zig");
    exe_tests.setBuildMode(mode);

    const test_step = builder.step("test", "Run unit tests");
    test_step.dependOn(&exe_tests.step);
}

fn getPicoSdkPath(builder: *std.build.Builder) []u8 {
    const option_name = "pico_sdk_path";
    const env_name = "PICO_SDK_PATH";

    if (builder.option([]u8, option_name, "Path to Raspberry Pi Pico SDK")) |path| {
        return path;
    }

    if (std.os.getenv(env_name)) |path| {
        return path;
    }

    std.debug.panic("Building " ++ project_name ++ " requires the Raspberry Pi Pico SDK to be available");
}
