package com.pickcode.v2.ui.screen.my

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.pickcode.v2.ui.components.GradientHeader

@Composable
fun AboutScreen(navController: NavHostController) {
    Column(modifier = Modifier.fillMaxSize()) {
        GradientHeader(title = "软件介绍", onBack = { navController.popBackStack() })

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("取件码 - 快递取件助手", style = MaterialTheme.typography.titleLarge)
                    Text("一款简洁实用的快递取件码记录工具，帮助您快速提取和管理快递取件码。", style = MaterialTheme.typography.bodyMedium)

                    FeatureItem("智能提取", "自动从短信中识别并提取取件码、快递公司和取件地址")
                    FeatureItem("自定义规则", "支持头尾匹配和正则表达式两种匹配模式，灵活适配各种短信格式")
                    FeatureItem("历史记录", "所有取件码按日期分组，支持标记已取、标签分类和备注")
                    FeatureItem("隐私保护", "所有数据仅存储在本地，不上传任何信息到服务器")
                    FeatureItem("简洁界面", "清爽的界面设计，操作简单直观")
                }
            }
        }
    }
}

@Composable
private fun FeatureItem(title: String, desc: String) {
    Column {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(desc, style = MaterialTheme.typography.bodySmall)
    }
}
