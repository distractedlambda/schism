package org.schism.schismatic

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.font.FontFamily
import org.schism.coroutines.MemoryFlow
import org.schism.math.toIntExact

@Composable
fun MemoryHexView(source: MemoryFlow, bytesPerRow: Int = 8) {
    LazyColumn {
        items((source.size / bytesPerRow).toIntExact()) { rowIndex ->
            Row {
                val startAddress = rowIndex.toLong() * bytesPerRow

                Text(startAddress.toString(16).padStart(16, '0'), fontFamily = FontFamily.Monospace)

                val bytes = source.slice(startAddress, bytesPerRow.toLong()).collectAsState(initial = null)

                for (byteIndex in 0 until bytesPerRow) {
                    
                }
            }
        }
    }
}
