package org.schism.memory

import java.lang.foreign.ValueLayout
import java.nio.ByteOrder

internal val UNALIGNED_NATIVE_CHAR = ValueLayout.JAVA_CHAR.withBitAlignment(8)
internal val UNALIGNED_LE_CHAR = UNALIGNED_NATIVE_CHAR.withOrder(ByteOrder.LITTLE_ENDIAN)
internal val UNALIGNED_BE_CHAR = UNALIGNED_NATIVE_CHAR.withOrder(ByteOrder.BIG_ENDIAN)

internal val UNALIGNED_NATIVE_SHORT = ValueLayout.JAVA_SHORT.withBitAlignment(8)
internal val UNALIGNED_LE_SHORT = UNALIGNED_NATIVE_SHORT.withOrder(ByteOrder.LITTLE_ENDIAN)
internal val UNALIGNED_BE_SHORT = UNALIGNED_NATIVE_SHORT.withOrder(ByteOrder.BIG_ENDIAN)

internal val UNALIGNED_NATIVE_INT = ValueLayout.JAVA_INT.withBitAlignment(8)
internal val UNALIGNED_LE_INT = UNALIGNED_NATIVE_INT.withOrder(ByteOrder.LITTLE_ENDIAN)
internal val UNALIGNED_BE_INT = UNALIGNED_NATIVE_INT.withOrder(ByteOrder.BIG_ENDIAN)

internal val UNALIGNED_NATIVE_LONG = ValueLayout.JAVA_LONG.withBitAlignment(8)
internal val UNALIGNED_LE_LONG = UNALIGNED_NATIVE_LONG.withOrder(ByteOrder.LITTLE_ENDIAN)
internal val UNALIGNED_BE_LONG = UNALIGNED_NATIVE_LONG.withOrder(ByteOrder.BIG_ENDIAN)

internal val UNALIGNED_NATIVE_FLOAT = ValueLayout.JAVA_FLOAT.withBitAlignment(8)
internal val UNALIGNED_LE_FLOAT = UNALIGNED_NATIVE_FLOAT.withOrder(ByteOrder.LITTLE_ENDIAN)
internal val UNALIGNED_BE_FLOAT = UNALIGNED_NATIVE_FLOAT.withOrder(ByteOrder.BIG_ENDIAN)

internal val UNALIGNED_NATIVE_DOUBLE = ValueLayout.JAVA_DOUBLE.withBitAlignment(8)
internal val UNALIGNED_LE_DOUBLE = UNALIGNED_NATIVE_DOUBLE.withOrder(ByteOrder.LITTLE_ENDIAN)
internal val UNALIGNED_BE_DOUBLE = UNALIGNED_NATIVE_DOUBLE.withOrder(ByteOrder.BIG_ENDIAN)
