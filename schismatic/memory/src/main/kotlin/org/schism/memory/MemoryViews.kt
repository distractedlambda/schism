@file:Suppress("NOTHING_TO_INLINE")

package org.schism.memory

import org.schism.math.timesExact

@JvmInline public value class MemoryBytes internal constructor(public val memory: Memory) {
    public inline val size: Long get() {
        return memory.size
    }

    public inline val indices: LongRange get() {
        return 0 until size
    }

    public inline operator fun get(index: Long): Byte {
        return memory.getByte(index)
    }

    public inline operator fun set(index: Long, value: Byte) {
        memory.setByte(value, index)
    }

    

    public class Iterator internal constructor(private val bytes: MemoryBytes) {
        private var index = 0L

        public operator fun hasNext(): Boolean {
            return index < bytes.size
        }

        public operator fun next(): Byte {
            return bytes[index++]
        }
    }
}

@JvmInline public value class MemoryChars internal constructor(public val memory: Memory) {
    public inline val size: Long get() {
        return memory.size / 2
    }

    public inline val indices: LongRange get() {
        return 0 until size
    }

    public inline operator fun get(index: Long): Char {
        return memory.getChar(index timesExact 2)
    }

    public inline operator fun set(index: Long, value: Char) {
        memory.setChar(value, index timesExact 2)
    }

    public class Iterator internal constructor(private val elements: MemoryChars) {
        private var index = 0L

        public operator fun hasNext(): Boolean {
            return index < elements.size
        }


    }
}
