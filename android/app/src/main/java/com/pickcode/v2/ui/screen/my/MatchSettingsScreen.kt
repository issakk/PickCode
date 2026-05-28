package com.pickcode.v2.ui.screen.my

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.pickcode.v2.ui.components.GradientHeader
import com.pickcode.v2.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchSettingsScreen(
    navController: NavHostController,
    viewModel: MatchSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        GradientHeader(title = "匹配设置") {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "返回", tint = Surface)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Rule name
            OutlinedTextField(
                value = uiState.ruleName,
                onValueChange = { viewModel.updateRuleName(it) },
                label = { Text("规则名称") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )

            // SMS content for testing
            OutlinedTextField(
                value = uiState.smsContent,
                onValueChange = { viewModel.updateSmsContent(it) },
                label = { Text("短信内容（用于测试）") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(8.dp)
            )

            // Match type toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.matchType == "startEnd",
                    onClick = { viewModel.updateMatchType("startEnd") },
                    label = { Text("头尾匹配") }
                )
                FilterChip(
                    selected = uiState.matchType == "regex",
                    onClick = { viewModel.updateMatchType("regex") },
                    label = { Text("正则匹配") }
                )
            }

            // Field editors
            listOf("code" to "取件码", "express" to "快递名称", "address" to "地址").forEach { (key, label) ->
                Card(shape = RoundedCornerShape(8.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(label, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))

                        if (uiState.matchType == "startEnd") {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = uiState.getFieldStart(key),
                                    onValueChange = { viewModel.updateFieldStart(key, it) },
                                    label = { Text("开始文本") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                OutlinedTextField(
                                    value = uiState.getFieldEnd(key),
                                    onValueChange = { viewModel.updateFieldEnd(key, it) },
                                    label = { Text("结束文本") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        } else {
                            OutlinedTextField(
                                value = uiState.getFieldPattern(key),
                                onValueChange = { viewModel.updateFieldPattern(key, it) },
                                label = { Text("正则表达式") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                        }

                        // Match result preview
                        val result = uiState.matchResults[key] ?: ""
                        if (uiState.smsContent.isNotBlank()) {
                            Text(
                                text = if (result.isNotEmpty()) "匹配结果: $result" else "未匹配到",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (result.isNotEmpty()) SuccessGreen else ErrorRed,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            // AI generate button
            if (uiState.matchType == "regex") {
                OutlinedButton(
                    onClick = { viewModel.aiGenerate(context) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.aiLoading
                ) {
                    if (uiState.aiLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("AI 生成正则")
                }
            }

            // Save button
            Button(
                onClick = {
                    if (viewModel.save()) {
                        navController.popBackStack()
                    } else {
                        Toast.makeText(context, "请填写规则名称和至少一条匹配规则", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("保存规则", modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}
