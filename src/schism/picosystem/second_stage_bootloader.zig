comptime {
    asm (@embedFile("second_stage_bootloader.S"));
}
