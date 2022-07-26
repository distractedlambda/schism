package org.schism.foreign

import java.lang.foreign.ValueLayout
import java.lang.foreign.ValueLayout.JAVA_CHAR
import java.lang.foreign.ValueLayout.JAVA_SHORT
import java.nio.ByteOrder

@PublishedApi internal val CHAR_LAYOUT: ValueLayout.OfChar = JAVA_CHAR.withBitAlignment(8)
@PublishedApi internal val LE_CHAR_LAYOUT: ValueLayout.OfChar = CHAR_LAYOUT.withOrder(ByteOrder.LITTLE_ENDIAN)
@PublishedApi internal val BE_CHAR_LAYOUT: ValueLayout.OfChar = CHAR_LAYOUT.withOrder(ByteOrder.BIG_ENDIAN)

@PublishedApi internal val SHORT_LAYOUT: ValueLayout.OfShort = JAVA_SHORT.withBitAlignment(8)
@PublishedApi internal val LE_SHORT_LAYOUT: ValueLayout.OfShort = SHORT_LAYOUT.withOrder(ByteOrder.LITTLE_ENDIAN)
@PublishedApi internal val BE_SHORT_LAYOUT: ValueLayout.OfShort = SHORT_LAYOUT.withOrder(ByteOrder.BIG_ENDIAN)

@PublishedApi internal val INT_LAYOUT: ValueLayout.OfInt = ValueLayout.JAVA_INT.withBitAlignment(8)
@PublishedApi internal val LE_INT_LAYOUT: ValueLayout.OfInt = INT_LAYOUT.withOrder(ByteOrder.LITTLE_ENDIAN)
@PublishedApi internal val BE_INT_LAYOUT: ValueLayout.OfInt = INT_LAYOUT.withOrder(ByteOrder.BIG_ENDIAN)

@PublishedApi internal val LONG_LAYOUT: ValueLayout.OfLong = ValueLayout.JAVA_LONG.withBitAlignment(8)
@PublishedApi internal val LE_LONG_LAYOUT: ValueLayout.OfLong = LONG_LAYOUT.withOrder(ByteOrder.LITTLE_ENDIAN)
@PublishedApi internal val BE_LONG_LAYOUT: ValueLayout.OfLong = LONG_LAYOUT.withOrder(ByteOrder.BIG_ENDIAN)

@PublishedApi internal val FLOAT_LAYOUT: ValueLayout.OfFloat = ValueLayout.JAVA_FLOAT.withBitAlignment(8)
@PublishedApi internal val LE_FLOAT_LAYOUT: ValueLayout.OfFloat = FLOAT_LAYOUT.withOrder(ByteOrder.LITTLE_ENDIAN)
@PublishedApi internal val BE_FLOAT_LAYOUT: ValueLayout.OfFloat = FLOAT_LAYOUT.withOrder(ByteOrder.BIG_ENDIAN)

@PublishedApi internal val DOUBLE_LAYOUT: ValueLayout.OfDouble = ValueLayout.JAVA_DOUBLE.withBitAlignment(8)
@PublishedApi internal val LE_DOUBLE_LAYOUT: ValueLayout.OfDouble = DOUBLE_LAYOUT.withOrder(ByteOrder.LITTLE_ENDIAN)
@PublishedApi internal val BE_DOUBLE_LAYOUT: ValueLayout.OfDouble = DOUBLE_LAYOUT.withOrder(ByteOrder.BIG_ENDIAN)

@PublishedApi internal val ADDRESS_LAYOUT: ValueLayout.OfAddress = ValueLayout.ADDRESS.withBitAlignment(8)
