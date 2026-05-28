package com.pickcode.v2.ui.screen.my

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.pickcode.v2.ui.components.GradientHeader
import com.pickcode.v2.ui.theme.*

@Composable
fun ChangelogScreen(navController: NavHostController) {
    Column(modifier = Modifier.fillMaxSize()) {
        GradientHeader(title = "更新记录") {
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
            VersionCard("v3.0.0", "2024-XX-XX", listOf(
                "全新 Compose 原生版本",
                "性能优化，启动更快",
                "UI 界面升级"
            ))
            VersionCard("v1.0.1", "2024-03-21", listOf(
                "新增预制匹配规则",
                "新增软件介绍页面",
                "优化界面显示",
                "修复短信读取问题"
            ))
            VersionCard("v1.0.0", "2024-03-20", listOf(
                "支持自定义匹配规则",
                "支持自动读取短信匹配取件码",
                "支持历史记录查看"
            ))
        }
    }
}

@Composable
private fun VersionCard(version: String, date: String, items: List<String>) {
    Card(shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(version, style = MaterialTheme.typography.titleMedium, color = Primary)
            Text(date, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
            items.forEach { item ->
                Text("• $item", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
