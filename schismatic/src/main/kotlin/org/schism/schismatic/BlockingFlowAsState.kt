package org.schism.schismatic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import org.schism.concurrent.BlockingFlow
import org.schism.concurrent.BlockingStateFlow
import org.schism.concurrent.collect
import org.schism.concurrent.virtualThread

@Composable
internal fun <T> BlockingFlow<T>.collectAsState(initialValue: T): State<T> {
    val state = remember { mutableStateOf(initialValue) }

    remember(this) {
        object : RememberObserver {
            private var collectionThread: Thread? = null

            override fun onRemembered() {
                collectionThread = virtualThread {
                    try {
                        collect { state.value = it }
                    } catch (_: InterruptedException) {}
                }
            }

            override fun onForgotten() {
                collectionThread?.let {
                    it.interrupt()
                    collectionThread = null
                }
            }

            override fun onAbandoned() {
                collectionThread?.let {
                    it.interrupt()
                    collectionThread = null
                }
            }
        }
    }

    return state
}

@Composable
internal fun <T> BlockingStateFlow<T>.collectAsState(): State<T> = collectAsState(value)
