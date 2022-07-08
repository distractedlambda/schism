package org.schism.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

public suspend fun <E : Any> Flow<Collection<E>>.launchWhileEachPresent(
    context: CoroutineContext = EmptyCoroutineContext,
    body: suspend CoroutineScope.(E) -> Unit,
): Unit = coroutineScope {
        val jobs = ConcurrentHashMap<E, Job>()
        additionsAndRemovals().collect { change ->
            change.onAdded { element ->
                jobs[element]?.join()
                jobs[element] = launch(context) {
                    body(element)
                }.apply {
                    invokeOnCompletion {
                        jobs.remove(element)
                    }
                }
            }.onRemoved { element ->
                jobs[element]?.cancel()
            }
        }
    }
