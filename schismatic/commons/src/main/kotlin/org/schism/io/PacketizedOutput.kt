package org.schism.io

import org.schism.memory.MemoryEncoder

public interface PacketizedOutput {
    public val maxPacketSize: Long

    public suspend fun <R> sendPacket(encode: suspend MemoryEncoder.() -> R): R
}
