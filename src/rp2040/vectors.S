// FIXME is this the correct way to attribute copyright?
/*
 * Copyright (c) 2020 Raspberry Pi (Trading) Ltd.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

.syntax unified
.cpu cortex-m0plus
.thumb

.section .vectors, "ax"
.align 2

.global __vectors, __VECTOR_TABLE
__VECTOR_TABLE:
__vectors:
.word 0x21030000
.word handleReset
.word handleNmi
.word handleHardfault
.word 0 // Reserved, should never fire
.word 0 // Reserved, should never fire
.word 0 // Reserved, should never fire
.word 0 // Reserved, should never fire
.word 0 // Reserved, should never fire
.word 0 // Reserved, should never fire
.word 0 // Reserved, should never fire
.word handleSvcall
.word 0 // Reserved, should never fire
.word 0 // Reserved, should never fire
.word handlePendsv
.word handleSystick
.word handleTimerIrq0
.word handleTimerIrq1
.word handleTimerIrq2
.word handleTimerIrq3
.word handlePwmIrqWrap
.word handleUsbctrlIrq
.word handleXipIrq
.word handlePio0Irq0
.word handlePio0Irq1
.word handlePio1Irq0
.word handlePio1Irq1
.word handleDmaIrq0
.word handleDmaIrq1
.word handleIoIrqBank0
.word handleIoIrqQspi
.word handleSioIrqProc0
.word handleSioIrqProc1
.word handleClocksIrq
.word handleSpi0Irq
.word handleSpi1Irq
.word handleUart0Irq
.word handleUart1Irq
.word handleAdcIrqFifo
.word handleI2c0Irq
.word handleI2c1Irq
.word handleRtcIrq
.word handleUnconnectedIrq
.word handleUnconnectedIrq
.word handleUnconnectedIrq
.word handleUnconnectedIrq
.word handleUnconnectedIrq
.word handleUnconnectedIrq
