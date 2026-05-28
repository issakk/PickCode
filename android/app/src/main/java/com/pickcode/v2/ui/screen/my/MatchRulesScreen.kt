package com.pickcode.v2.ui.screen.my

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.pickcode.v2.domain.model.MatchRule
import com.pickcode.v2.navigation.Routes
import com.pickcode.v2.ui.components.GradientHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchRulesScreen(
    navController: NavHostController,
    viewModel: MatchRulesViewModel = hiltViewModel()
) {
    val rules by viewModel.rules.collectAsState()
    val presets by viewModel.presets.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    var showPresetSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<MatchRule?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        GradientHeader(title = "匹配规则", onBack = { navController.popBackStack() })

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(rules) { rule ->
                Card(shape = RoundedCornerShape(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(rule.name, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (rule.matchType == "startEnd") colorScheme.primaryContainer else colorScheme.secondaryContainer
                            ) {
                                Text(
                                    if (rule.matchType == "startEnd") "头尾" else "正则",
                                    fontSize = 11.sp,
                                    color = if (rule.matchType == "startEnd") colorScheme.onPrimaryContainer else colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(rule.createTime, fontSize = 11.sp, color = colorScheme.outline)
                        }
                        Switch(
                            checked = rule.enabled,
                            onCheckedChange = { viewModel.toggleEnabled(rule) }
                        )
                        IconButton(onClick = { navController.navigate(Routes.matchSettings("edit", rule.id)) }) {
                            Icon(Icons.Default.Edit, contentDescription = "编辑", modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = { showDeleteDialog = rule }) {
                            Icon(Icons.Default.Delete, contentDescription = "删除", tint = colorScheme.error, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        // Bottom buttons
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { showPresetSheet = true },
                modifier = Modifier.weight(1f)
            ) { Text("使用预制规则") }
            Button(
                onClick = { navController.navigate(Routes.matchSettings("add")) },
                modifier = Modifier.weight(1f)
            ) { Text("添加规则") }
        }
    }

    // Preset bottom sheet
    if (showPresetSheet) {
        ModalBottomSheet(onDismissRequest = { showPresetSheet = false }) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("选择预制规则", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))
                presets.forEach { preset ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        onClick = { viewModel.importPreset(preset); showPresetSheet = false }
                    ) {
                        Text(preset.name, modifier = Modifier.padding(16.dp))
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    showDeleteDialog?.let { rule ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("确认删除") },
            text = { Text("确定要删除规则 \"${rule.name}\" 吗？") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteRule(rule); showDeleteDialog = null }) {
                    Text("删除", color = colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = null }) { Text("取消") } }
        )
    }
}
