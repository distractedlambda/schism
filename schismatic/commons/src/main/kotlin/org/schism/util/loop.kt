package org.schism.util

public inline fun loop(block: () -> Unit) {
    while (true) {
        block()
    }
}
