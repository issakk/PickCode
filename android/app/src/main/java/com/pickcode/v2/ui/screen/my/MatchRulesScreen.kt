package com.pickcode.v2.ui.screen.my

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
    var menuRule by remember { mutableStateOf<MatchRule?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        GradientHeader(title = "匹配规则", onBack = { navController.popBackStack() })

        if (rules.isEmpty()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Outlined.Tune,
                    contentDescription = null,
                    tint = colorScheme.outline,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text("还没有匹配规则", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "规则决定怎么从短信里提取取件码，可以直接用预制规则试试",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(rules, key = { it.id }) { rule ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate(Routes.matchSettings("edit", rule.id)) },
                        shape = MaterialTheme.shapes.medium,
                        color = colorScheme.surface,
                        border = BorderStroke(1.dp, colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp, top = 10.dp, bottom = 10.dp, end = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(rule.name, style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = MaterialTheme.shapes.extraSmall,
                                        color = if (rule.matchType == "startEnd") {
                                            colorScheme.primaryContainer
                                        } else {
                                            colorScheme.secondaryContainer
                                        }
                                    ) {
                                        Text(
                                            text = if (rule.matchType == "startEnd") "头尾匹配" else "正则匹配",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (rule.matchType == "startEnd") {
                                                colorScheme.onPrimaryContainer
                                            } else {
                                                colorScheme.onSecondaryContainer
                                            },
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = rule.createTime.take(10),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colorScheme.outline
                                    )
                                }
                            }
                            Switch(
                                checked = rule.enabled,
                                onCheckedChange = { viewModel.toggleEnabled(rule) }
                            )
                            Box {
                                IconButton(onClick = { menuRule = rule }, modifier = Modifier.size(40.dp)) {
                                    Icon(
                                        Icons.Default.MoreVert,
                                        contentDescription = "更多操作",
                                        tint = colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                if (menuRule?.id == rule.id) {
                                    DropdownMenu(expanded = true, onDismissRequest = { menuRule = null }) {
                                        DropdownMenuItem(
                                            text = { Text("编辑") },
                                            leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                                            onClick = {
                                                menuRule = null
                                                navController.navigate(Routes.matchSettings("edit", rule.id))
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("删除", color = colorScheme.error) },
                                            leadingIcon = {
                                                Icon(Icons.Outlined.Delete, contentDescription = null, tint = colorScheme.error)
                                            },
                                            onClick = {
                                                menuRule = null
                                                showDeleteDialog = rule
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { showPresetSheet = true },
                modifier = Modifier.weight(1f).height(48.dp),
                shape = MaterialTheme.shapes.small
            ) { Text("使用预制规则") }
            Button(
                onClick = { navController.navigate(Routes.matchSettings("add")) },
                modifier = Modifier.weight(1f).height(48.dp),
                shape = MaterialTheme.shapes.small
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("添加规则")
            }
        }
    }

    // 预制规则
    if (showPresetSheet) {
        ModalBottomSheet(onDismissRequest = { showPresetSheet = false }) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text("选择预制规则", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "导入后可以再改成自己的规则",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                presets.forEach { preset ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { viewModel.importPreset(preset); showPresetSheet = false },
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(preset.name, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = listOfNotNull(
                                    preset.rules.code.start.takeIf { it.isNotEmpty() }?.let { "取件码：$it … ${preset.rules.code.end}" },
                                    preset.rules.express.start.takeIf { it.isNotEmpty() }?.let { "快递：" + it + " … " + preset.rules.express.end }
                                ).joinToString("　"),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Spacer(Modifier.navigationBarsPadding())
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
