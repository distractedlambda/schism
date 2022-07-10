package org.schism.io

import org.schism.memory.MemoryDecoder

public interface PacketizedInput {
    public val maxPacketSize: Long

    public suspend fun <R> receivePacket(decode: suspend MemoryDecoder.() -> R): R
}
