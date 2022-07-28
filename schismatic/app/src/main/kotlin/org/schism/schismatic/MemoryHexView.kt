package org.schism.schismatic

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import org.schism.coroutines.MemoryFlow
import org.schism.foreign.getUByte
import org.schism.math.toIntExact

@Composable
fun MemoryHexView(source: MemoryFlow, bytesPerRow: Int = 8) {
    LazyColumn(contentPadding = PaddingValues(horizontal = 1.dp)) {
        items((source.size / bytesPerRow).toIntExact()) { rowIndex ->
            Row {
                val startAddress = rowIndex.toLong() * bytesPerRow
                Text(startAddress.toString(16).padStart(16, '0'), fontFamily = FontFamily.Monospace)
                ByteValues(source.slice(startAddress, bytesPerRow.toLong()))
            }
        }
    }
}

@Composable
private fun ByteValues(bytes: MemoryFlow) {
    val currentBytes = bytes.collectAsState(initial = null).value
    for (byteIndex in 0 until bytes.size) {
        val text = currentBytes?.getUByte(byteIndex)?.toString(16)?.padStart(2, '0') ?: "--"
        Text(text, fontFamily = FontFamily.Monospace)
    }
}
