package org.schism.foreign

interface MeteredBufferEncoder : BufferEncoder {
    val bytesWritten: Long
}
