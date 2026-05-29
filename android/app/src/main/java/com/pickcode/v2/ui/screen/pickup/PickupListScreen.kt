package com.pickcode.v2.ui.screen.pickup

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.pickcode.v2.domain.model.PackageCode
import com.pickcode.v2.navigation.Routes
import com.pickcode.v2.ui.components.CodeCard
import com.pickcode.v2.ui.components.GradientHeader

@Composable
fun PickupListScreen(
    rootNavController: NavHostController,
    viewModel: PickupListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    var showDeleteDialog by remember { mutableStateOf<PackageCode?>(null) }
    var showDateDeleteDialog by remember { mutableStateOf<String?>(null) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    val smsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.autoMatch(context)
        } else {
            Toast.makeText(context, "需要短信权限才能自动匹配取件码", Toast.LENGTH_SHORT).show()
        }
    }

    fun requestSmsAndMatch() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {
            viewModel.autoMatch(context)
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
                Icon(Icons.Outlined.Refresh, contentDescription = "自动匹配")
            }
        }

        if (uiState.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
        }

        val flatItems = uiState.flatItems
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
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
private fun DateHeaderItem(
    date: String,
    pendingCount: Int,
    onDelete: () -> Unit,
    colorScheme: ColorScheme
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = date,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = colorScheme.onSurfaceVariant,
            letterSpacing = 0.5.sp,
            modifier = Modifier.weight(1f)
        )
        if (pendingCount > 0) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Text(
                    text = "$pendingCount 个待取",
                    fontSize = 11.sp,
                    color = colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(
                Icons.Outlined.DeleteSweep,
                contentDescription = "删除当日",
                tint = colorScheme.error,
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
    Text(
        text = address,
        fontSize = 13.sp,
        color = colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
    )
}
