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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pickcode.v2.ui.components.GradientHeader
import com.pickcode.v2.ui.theme.*
import com.pickcode.v2.ui.util.LogBuffer
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LogsScreen() {
    val logs by LogBuffer.logs.collectAsState()
    val listState = rememberLazyListState()
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()) }

    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty() && listState.firstVisibleItemIndex >= logs.size - 5) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        GradientHeader(title = "调试日志") {
            IconButton(onClick = { LogBuffer.clear() }) {
                Icon(Icons.Default.Delete, contentDescription = "清除", tint = Surface)
            }
        }

        if (logs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("暂无日志", color = TextTertiary)
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(logs.size) { index ->
                    val entry = logs[index]
                    val time = timeFormat.format(Date(entry.timestamp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8F8F8), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = time,
                            fontSize = 10.sp,
                            color = TextTertiary,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.width(75.dp)
                        )
                        Text(
                            text = entry.tag,
                            fontSize = 10.sp,
                            color = Primary,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.width(60.dp)
                        )
                        Text(
                            text = entry.message,
                            fontSize = 10.sp,
                            color = TextPrimary,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
