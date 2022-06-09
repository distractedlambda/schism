package org.schism.schismatic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import org.schism.concurrent.Observable
import java.lang.Thread.startVirtualThread

@Composable
internal fun <T> Observable<T>.observeAsState(): State<T> {
    val state = remember { mutableStateOf(value) }

    remember(this) {
        object : RememberObserver {
            private var subscriptionThread: Thread? = null

            override fun onRemembered() {
                subscriptionThread = startVirtualThread {
                    subscribe().use { subscription ->
                        while (!Thread.interrupted()) {
                            state.value = subscription.next()
                        }
                    }
                }
            }

            override fun onForgotten() {
                subscriptionThread?.let {
                    it.interrupt()
                    subscriptionThread = null
                }
            }

            override fun onAbandoned() {
                subscriptionThread?.let {
                    it.interrupt()
                    subscriptionThread = null
                }
            }
        }
    }

    return state
}
