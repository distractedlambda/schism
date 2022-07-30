package org.schism.foreign

import java.lang.foreign.ValueLayout
import java.lang.foreign.ValueLayout.JAVA_CHAR
import java.lang.foreign.ValueLayout.JAVA_SHORT
import java.nio.ByteOrder.BIG_ENDIAN
import java.nio.ByteOrder.LITTLE_ENDIAN

@PublishedApi internal val UNALIGNED_CHAR: ValueLayout.OfChar = JAVA_CHAR.withBitAlignment(8)
@PublishedApi internal val UNALIGNED_LE_CHAR: ValueLayout.OfChar = UNALIGNED_CHAR.withOrder(LITTLE_ENDIAN)
@PublishedApi internal val UNALIGNED_BE_CHAR: ValueLayout.OfChar = UNALIGNED_CHAR.withOrder(BIG_ENDIAN)

@PublishedApi internal val UNALIGNED_SHORT: ValueLayout.OfShort = JAVA_SHORT.withBitAlignment(8)
@PublishedApi internal val UNALIGNED_LE_SHORT: ValueLayout.OfShort = UNALIGNED_SHORT.withOrder(LITTLE_ENDIAN)
@PublishedApi internal val UNALIGNED_BE_SHORT: ValueLayout.OfShort = UNALIGNED_SHORT.withOrder(BIG_ENDIAN)

@PublishedApi internal val UNALIGNED_INT: ValueLayout.OfInt = ValueLayout.JAVA_INT.withBitAlignment(8)
@PublishedApi internal val UNALIGNED_LE_INT: ValueLayout.OfInt = UNALIGNED_INT.withOrder(LITTLE_ENDIAN)
@PublishedApi internal val UNALIGNED_BE_INT: ValueLayout.OfInt = UNALIGNED_INT.withOrder(BIG_ENDIAN)

@PublishedApi internal val UNALIGNED_LONG: ValueLayout.OfLong = ValueLayout.JAVA_LONG.withBitAlignment(8)
@PublishedApi internal val UNALIGNED_LE_LONG: ValueLayout.OfLong = UNALIGNED_LONG.withOrder(LITTLE_ENDIAN)
@PublishedApi internal val UNALIGNED_BE_LONG: ValueLayout.OfLong = UNALIGNED_LONG.withOrder(BIG_ENDIAN)

@PublishedApi internal val UNALIGNED_FLOAT: ValueLayout.OfFloat = ValueLayout.JAVA_FLOAT.withBitAlignment(8)
@PublishedApi internal val UNALIGNED_LE_FLOAT: ValueLayout.OfFloat = UNALIGNED_FLOAT.withOrder(LITTLE_ENDIAN)
@PublishedApi internal val UNALIGNED_BE_FLOAT: ValueLayout.OfFloat = UNALIGNED_FLOAT.withOrder(BIG_ENDIAN)

@PublishedApi internal val UNALIGNED_DOUBLE: ValueLayout.OfDouble = ValueLayout.JAVA_DOUBLE.withBitAlignment(8)
@PublishedApi internal val UNALIGNED_LE_DOUBLE: ValueLayout.OfDouble = UNALIGNED_DOUBLE.withOrder(LITTLE_ENDIAN)
@PublishedApi internal val UNALIGNED_BE_DOUBLE: ValueLayout.OfDouble = UNALIGNED_DOUBLE.withOrder(BIG_ENDIAN)

@PublishedApi internal val UNALIGNED_ADDRESS: ValueLayout.OfAddress = ValueLayout.ADDRESS.withBitAlignment(8)
