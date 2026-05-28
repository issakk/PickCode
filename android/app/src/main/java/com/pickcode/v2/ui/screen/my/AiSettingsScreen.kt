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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.pickcode.v2.ui.components.GradientHeader
import com.pickcode.v2.ui.theme.*

@Composable
fun AiSettingsScreen(
    navController: NavHostController,
    viewModel: AiSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showKey by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        GradientHeader(title = "AI 设置") {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "返回", tint = Surface)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.endpoint,
                onValueChange = { viewModel.updateEndpoint(it) },
                label = { Text("API 地址") },
                placeholder = { Text("https://api.openai.com") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )

            OutlinedTextField(
                value = uiState.apiKey,
                onValueChange = { viewModel.updateApiKey(it) },
                label = { Text("API Key") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    TextButton(onClick = { showKey = !showKey }) {
                        Text(if (showKey) "隐藏" else "显示")
                    }
                },
                shape = RoundedCornerShape(8.dp)
            )

            OutlinedTextField(
                value = uiState.model,
                onValueChange = { viewModel.updateModel(it) },
                label = { Text("模型名称") },
                placeholder = { Text("gpt-4o-mini") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.testConnection(context) },
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isTesting
                ) {
                    if (uiState.isTesting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text("测试连接")
                    }
                }
                Button(
                    onClick = {
                        if (viewModel.save()) {
                            Toast.makeText(context, "已保存", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "请填写所有字段", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("保存")
                }
            }

            Text(
                "支持任何 OpenAI 兼容 API（DeepSeek、通义千问等）",
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary
            )
        }
    }
}
