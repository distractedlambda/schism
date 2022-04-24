MEMORY
{
    FLASH(rx) : ORIGIN = 0x10000000,  LENGTH = 12288k /* User Flash (12MiB) */
    FAT(r)    : ORIGIN = 0x10003000,  LENGTH = 4096K  /* Reserved for FAT (4MiB) */
    RAM(rwx)  : ORIGIN = 0x20000000,  LENGTH = 256k
}

SECTIONS {
    .boot2 : {
        __boot2_start__ = .;
        KEEP (*(.boot2))
        __boot2_end__ = .;
    } > FLASH

    .text : {
        __logical_binary_start = .;
        KEEP (*(.vectors))
        . = ALIGN(4);
        *(.text*)
        . = ALIGN(4);
    } > FLASH

    .rodata : {
        *(.rodata*)
        . = ALIGN(4);
    } > FLASH

    .data : {
        __data_start__ = .;
        *(.data*)
        . = ALIGN(4);
        __data_end__ = .;
    } > RAM AT> FLASH
    __data_source__ = LOADADDR(.data);

    .bss : {
        __bss_start__ = .;
        *(.bss*)
        . = ALIGN(4);
        __bss_end__ = .;
    } > RAM
}
