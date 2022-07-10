package org.schism.coroutines

import kotlinx.coroutines.Job
import kotlin.coroutines.coroutineContext

public suspend inline fun currentJob(): Job? {
    return coroutineContext[Job]
}
