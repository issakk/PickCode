package com.pickcode.v2.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pickcode.v2.ui.theme.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TagGrid(
    tags: List<String>,
    selectedTags: List<String>,
    onToggle: (String) -> Unit,
    onLongPress: (String) -> Unit,
    columns: Int = 5,
    modifier: Modifier = Modifier
) {
    val rows = tags.chunked(columns)
    Column(modifier = modifier) {
        rows.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { tag ->
                    val selected = tag in selectedTags
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (selected) Primary else Divider,
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .combinedClickable(
                                onClick = { onToggle(tag) },
                                onLongClick = { onLongPress(tag) }
                            )
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tag,
                                fontSize = 13.sp,
                                color = if (selected) Surface else TextPrimary
                            )
                        }
                    }
                }
                // Fill remaining space
                repeat(columns - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
