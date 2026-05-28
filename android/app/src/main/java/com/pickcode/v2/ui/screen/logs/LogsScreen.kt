package com.pickcode.v2.ui.screen.logs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pickcode.v2.ui.components.GradientHeader
import com.pickcode.v2.ui.util.LogBuffer
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LogsScreen() {
    val version by LogBuffer.version.collectAsState()
    val logs = remember(version) { LogBuffer.snapshot() }
    val listState = rememberLazyListState()
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(version) {
        if (logs.isNotEmpty() && listState.firstVisibleItemIndex >= logs.size - 5) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        GradientHeader(title = "调试日志") {
            IconButton(onClick = { LogBuffer.clear() }) {
                Icon(Icons.Default.Delete, contentDescription = "清除")
            }
        }

        if (logs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("暂无日志", color = colorScheme.outline)
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(
                    count = logs.size,
                    key = { it }
                ) { index ->
                    val entry = logs[index]
                    LogItem(entry = entry)
                }
            }
        }
    }
}

@Composable
private fun LogItem(
    entry: com.pickcode.v2.ui.util.LogEntry,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val time = remember(entry.timestamp) {
        SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date(entry.timestamp))
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Text(
            text = time,
            fontSize = 10.sp,
            color = colorScheme.outline,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(75.dp)
        )
        Text(
            text = entry.tag,
            fontSize = 10.sp,
            color = colorScheme.primary,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(60.dp)
        )
        Text(
            text = entry.message,
            fontSize = 10.sp,
            color = colorScheme.onSurface,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.weight(1f)
        )
    }
}
