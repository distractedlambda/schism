package org.schism.foreign

interface MeteredBufferDecoder : BufferDecoder {
    val bytesRead: Long
}
