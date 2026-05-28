package com.pickcode.v2.ui.screen.package_record

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.pickcode.v2.domain.model.PackageRecord
import com.pickcode.v2.navigation.Routes
import com.pickcode.v2.ui.components.GradientHeader
import com.pickcode.v2.ui.components.PlatformIcon
import com.pickcode.v2.ui.theme.success
import com.pickcode.v2.ui.theme.specialty
import com.pickcode.v2.ui.util.openPlatformApp

@Composable
fun PackageRecordScreen(
    rootNavController: NavHostController,
    viewModel: PackageRecordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    var showDeleteDialog by remember { mutableStateOf<PackageRecord?>(null) }
    var showDateDeleteDialog by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            GradientHeader(title = "包裹记录")

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                uiState.groupedRecords.forEach { (date, records) ->
                    val checkedCount = records.count { it.checked }

                    item(key = "header_$date") {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = date,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${records.size}个包裹，${checkedCount}个已签收",
                                fontSize = 12.sp,
                                color = colorScheme.onSurfaceVariant
                            )
                            IconButton(onClick = { showDateDeleteDialog = date }) {
                                Icon(Icons.Outlined.Delete, contentDescription = "删除", tint = colorScheme.error, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    // Group by platform within date
                    val byPlatform = records.groupBy { it.platform }
                    byPlatform.forEach { (platform, platformRecords) ->
                        item(key = "platform_${date}_$platform") {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        PlatformIcon(platform, modifier = Modifier.size(24.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text(platform, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                                        val total = platformRecords.sumOf { it.price.toDoubleOrNull() ?: 0.0 }
                                        if (total > 0) {
                                            Text("¥${String.format("%.2f", total)}", color = colorScheme.specialty, fontSize = 14.sp)
                                        }
                                    }

                                    platformRecords.forEach { record ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = record.checked,
                                                onCheckedChange = { viewModel.toggleChecked(record) },
                                                colors = CheckboxDefaults.colors(checkedColor = colorScheme.success)
                                            )
                                            Text(
                                                text = record.name,
                                                modifier = Modifier.weight(1f),
                                                fontSize = 14.sp,
                                                color = if (record.checked) colorScheme.outline else colorScheme.onSurface
                                            )
                                            if (record.price.isNotEmpty() && record.price != "0.00") {
                                                Text("¥${record.price}", fontSize = 13.sp, color = colorScheme.onSurfaceVariant)
                                            }
                                            IconButton(onClick = {
                                                rootNavController.navigate(Routes.editPackage(record.id, "edit"))
                                            }) {
                                                Icon(Icons.Outlined.Delete, contentDescription = "编辑", modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = { rootNavController.navigate(Routes.editPackage(type = "add")) },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        ) {
            Icon(Icons.Outlined.Add, contentDescription = "添加包裹")
        }
    }

    showDeleteDialog?.let { record ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("确认删除") },
            text = { Text("确定要删除 ${record.name} 吗？") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteRecord(record); showDeleteDialog = null }) {
                    Text("删除", color = colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = null }) { Text("取消") } }
        )
    }

    showDateDeleteDialog?.let { date ->
        AlertDialog(
            onDismissRequest = { showDateDeleteDialog = null },
            title = { Text("确认删除") },
            text = { Text("确定要删除 $date 的所有包裹吗？") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteByDate(date); showDateDeleteDialog = null }) {
                    Text("删除", color = colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showDateDeleteDialog = null }) { Text("取消") } }
        )
    }
}
