package org.schism.cousb

import java.lang.Integer.parseUnsignedInt

internal object UsbIds {
    val vendorNames: Map<UShort, String>
    val productNames: Map<UInt, String>

    init {
        vendorNames = hashMapOf()
        productNames = hashMapOf()

        try {
            // FIXME: pre-parse during build
            javaClass.getResourceAsStream("usb.ids")!!.reader(Charsets.US_ASCII).useLines { lines ->
                var vendorId: UShort = 0u

                lines.forEach { line ->
                    if (line.isBlank()) {
                        return@forEach
                    }

                    when (line[0]) {
                        '#' -> {
                            return@forEach
                        }

                        in '0'..'9' -> {
                            vendorId = parseUnsignedInt(line, 0, 4, 16).toUShort()
                            line.subSequence(4, line.length)
                                .trim()
                                .toString()
                                .takeUnless { it.startsWith("unknown", ignoreCase = true) }
                                ?.let {
                                    vendorNames[vendorId] = it
                                }
                        }

                        '\t' -> when (line[1]) {
                            in '0'..'9' -> {
                                val productId = parseUnsignedInt(line, 1, 5, 16).toUShort()
                                productNames[vendorId.toUInt() shl 16 or productId.toUInt()] = line
                                    .subSequence(5, line.length)
                                    .trim()
                                    .toString()
                            }

                            else -> return@forEach
                        }

                        else -> return@useLines
                    }
                }
            }
        } catch (exception: Throwable) {
            exception.printStackTrace()
        }
    }
}
