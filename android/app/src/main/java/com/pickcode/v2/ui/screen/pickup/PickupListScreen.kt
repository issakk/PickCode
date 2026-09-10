package com.pickcode.v2.ui.screen.pickup

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AllInbox
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavHostController
import com.pickcode.v2.domain.model.PackageCode
import com.pickcode.v2.navigation.Routes
import com.pickcode.v2.ui.components.CodeCard
import com.pickcode.v2.ui.components.GradientHeader
import kotlinx.coroutines.launch

@Composable
fun PickupListScreen(
    rootNavController: NavHostController,
    viewModel: PickupListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // 从编辑页返回时重新读库，否则列表还是旧数据
    LifecycleResumeEffect(Unit) {
        viewModel.reload()
        onPauseOrDispose { }
    }

    var showDeleteDialog by remember { mutableStateOf<PackageCode?>(null) }
    var showDateDeleteDialog by remember { mutableStateOf<String?>(null) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    val smsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.autoMatch(context) {
                coroutineScope.launch { listState.animateScrollToItem(0) }
            }
        } else {
            Toast.makeText(context, "需要短信权限才能自动匹配取件码", Toast.LENGTH_SHORT).show()
        }
    }

    fun requestSmsAndMatch() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {
            viewModel.autoMatch(context) {
                coroutineScope.launch { listState.animateScrollToItem(0) }
            }
        } else {
            smsPermissionLauncher.launch(Manifest.permission.READ_SMS)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        GradientHeader(title = "取件码") {
            if (uiState.codes.isNotEmpty()) {
                IconButton(onClick = { showDeleteAllDialog = true }) {
                    Icon(Icons.Outlined.DeleteSweep, contentDescription = "全部删除")
                }
            }
            IconButton(onClick = { requestSmsAndMatch() }) {
                Icon(Icons.Outlined.Refresh, contentDescription = "扫描短信匹配")
            }
        }

        if (uiState.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        val flatItems = uiState.flatItems
        if (flatItems.isEmpty() && !uiState.isLoading) {
            EmptyPickupState(onScan = { requestSmsAndMatch() })
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = flatItems,
                    key = { item ->
                        when (item) {
                            is PickupListItem.DateHeader -> "date_${item.date}"
                            is PickupListItem.AddressHeader -> "addr_${item.date}_${item.address}"
                            is PickupListItem.Code -> "code_${item.item.id}"
                        }
                    },
                    contentType = { item -> item::class }
                ) { item ->
                    when (item) {
                        is PickupListItem.DateHeader -> DateHeaderItem(
                            date = item.date,
                            pendingCount = item.pendingCount,
                            onDelete = { showDateDeleteDialog = item.date },
                            colorScheme = colorScheme
                        )

                        is PickupListItem.AddressHeader -> AddressHeaderItem(
                            address = item.address,
                            colorScheme = colorScheme
                        )

                        is PickupListItem.Code -> {
                            val code = item.item
                            CodeCard(
                                item = code,
                                onTogglePicked = remember(code) { { viewModel.togglePicked(code) } },
                                onEdit = remember(code) { { rootNavController.navigate(Routes.editCode(code.id)) } },
                                onDelete = remember(code) { { showDeleteDialog = code } }
                            )
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation dialogs
    showDeleteDialog?.let { item ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("确认删除") },
            text = { Text("确定要删除取件码 ${item.code} 吗？") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteCode(item); showDeleteDialog = null }) {
                    Text("删除", color = colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("取消") }
            }
        )
    }

    showDateDeleteDialog?.let { date ->
        AlertDialog(
            onDismissRequest = { showDateDeleteDialog = null },
            title = { Text("确认删除") },
            text = { Text("确定要删除 $date 的所有取件码吗？") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteByDate(date); showDateDeleteDialog = null }) {
                    Text("删除", color = colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDateDeleteDialog = null }) { Text("取消") }
            }
        )
    }

    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("全部删除") },
            text = { Text("确定要删除所有 ${uiState.codes.size} 个取件码吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteAll(); showDeleteAllDialog = false }) {
                    Text("全部删除", color = colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) { Text("取消") }
            }
        )
    }
}

@Composable
private fun EmptyPickupState(onScan: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Outlined.AllInbox,
            contentDescription = null,
            tint = colorScheme.outline,
            modifier = Modifier.size(56.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text("还没有取件码", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        Text(
            text = "扫描最近 4 天的短信，按规则自动提取取件码",
            style = MaterialTheme.typography.bodySmall,
            color = colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onScan, shape = MaterialTheme.shapes.small) {
            Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("扫描短信")
        }
    }
}

@Composable
private fun DateHeaderItem(
    date: String,
    pendingCount: Int,
    onDelete: () -> Unit,
    colorScheme: ColorScheme
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = date,
            style = MaterialTheme.typography.titleSmall,
            color = colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (pendingCount > 0) {
            Surface(
                shape = MaterialTheme.shapes.extraSmall,
                color = colorScheme.primaryContainer
            ) {
                Text(
                    text = "$pendingCount 个待取",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(
                Icons.Outlined.DeleteSweep,
                contentDescription = "删除当日",
                tint = colorScheme.outline,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun AddressHeaderItem(
    address: String,
    colorScheme: ColorScheme
) {
    Row(
        modifier = Modifier.padding(start = 4.dp, top = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Outlined.LocationOn,
            contentDescription = null,
            tint = colorScheme.outline,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = address,
            style = MaterialTheme.typography.labelMedium,
            color = colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}
