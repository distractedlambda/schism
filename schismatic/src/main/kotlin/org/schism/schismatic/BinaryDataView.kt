package org.schism.schismatic

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import org.schism.foreign.NativeBuffer

interface BinaryData {
    fun slice(offset: Long, size: Long): NativeBuffer
}

@Composable
fun BinaryDataView() {
    LazyColumn {
    }
}
