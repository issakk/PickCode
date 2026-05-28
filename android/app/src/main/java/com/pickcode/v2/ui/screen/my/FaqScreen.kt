package com.pickcode.v2.ui.screen.my

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.pickcode.v2.ui.components.GradientHeader

@Composable
fun FaqScreen(navController: NavHostController) {
    Column(modifier = Modifier.fillMaxSize()) {
        GradientHeader(title = "常见问题", onBack = { navController.popBackStack() })

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FaqItem(
                "为什么需要短信权限？",
                "本应用仅在您点击\"自动匹配\"按钮时读取短信，用于提取快递取件码。短信内容仅在本地处理，不会上传到任何服务器。应用不会修改或删除您的短信。您可以在系统设置中随时撤销权限。"
            )
            FaqItem(
                "为什么自动匹配没有反应？",
                "可能的原因：\n1. 未添加匹配规则 - 请在\"匹配设置\"中添加规则\n2. 短信权限未开启 - 请在系统设置中允许读取短信\n3. iOS 设备不支持读取短信\n4. 小米设备需要额外开启\"通知类短信\"权限"
            )
            FaqItem(
                "为什么匹配到的内容不完整？",
                "请检查匹配规则是否正确。建议使用\"测试\"功能验证规则。不同快递公司的短信格式不同，可能需要添加多条规则。小米设备请确保已开启\"通知类短信\"权限。"
            )
        }
    }
}

@Composable
private fun FaqItem(question: String, answer: String) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(question, style = MaterialTheme.typography.titleMedium)
            if (expanded) {
                Spacer(Modifier.height(8.dp))
                Text(answer, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
