package org.schism.concurrent

public fun checkInterruption() {
    if (Thread.interrupted()) {
        throw InterruptedException()
    }
}
