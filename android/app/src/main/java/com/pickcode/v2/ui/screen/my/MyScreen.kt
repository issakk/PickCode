package com.pickcode.v2.ui.screen.my

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.pickcode.v2.BuildConfig
import com.pickcode.v2.navigation.Routes
import com.pickcode.v2.ui.components.LargeTitleHeader
import com.pickcode.v2.ui.theme.successColor

@Composable
fun MyScreen(
    rootNavController: NavHostController,
    viewModel: MyViewModel = hiltViewModel()
) {
    val stats by viewModel.stats.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    val received = stats.first
    val picked = stats.second
    val pending = (received - picked).coerceAtLeast(0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        LargeTitleHeader(title = "我的")

        // 统计条
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(received, "已收到", colorScheme.onSurface, Modifier.weight(1f))
            StatDivider(colorScheme)
            StatItem(pending, "待取", colorScheme.primary, Modifier.weight(1f))
            StatDivider(colorScheme)
            StatItem(picked, "已取件", successColor(), Modifier.weight(1f))
        }
        HorizontalDivider(color = colorScheme.outlineVariant)

        SectionLabel("设置")
        MenuItem("匹配设置", Icons.Outlined.Tune) { rootNavController.navigate(Routes.MATCH_RULES) }
        MenuDivider(colorScheme)
        MenuItem("AI 设置", Icons.Outlined.SmartToy) { rootNavController.navigate(Routes.AI_SETTINGS) }

        SectionLabel("关于")
        MenuItem("软件介绍", Icons.Outlined.Info) { rootNavController.navigate(Routes.ABOUT) }
        MenuDivider(colorScheme)
        MenuItem("更新记录", Icons.Outlined.History) { rootNavController.navigate(Routes.CHANGELOG) }
        MenuDivider(colorScheme)
        MenuItem("常见问题", Icons.AutoMirrored.Filled.HelpOutline) { rootNavController.navigate(Routes.FAQ) }

        Spacer(Modifier.height(32.dp))
        Text(
            text = "v${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.labelMedium,
            color = colorScheme.outline,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "所有取件码只保存在本机，不上传服务器",
            style = MaterialTheme.typography.labelSmall,
            color = colorScheme.outline,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun StatItem(
    value: Int,
    label: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$value",
            style = MaterialTheme.typography.headlineMedium,
            color = valueColor
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatDivider(colorScheme: ColorScheme) {
    HorizontalDivider(
        modifier = Modifier.height(32.dp).width(1.dp),
        color = colorScheme.outlineVariant
    )
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
private fun MenuDivider(colorScheme: ColorScheme) {
    HorizontalDivider(
        thickness = 1.dp,
        color = colorScheme.outlineVariant,
        modifier = Modifier.padding(start = 56.dp)
    )
}

@Composable
private fun MenuItem(title: String, icon: ImageVector, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
    }
}
