package com.pickcode.v2.ui.screen.my

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.pickcode.v2.BuildConfig
import com.pickcode.v2.R
import com.pickcode.v2.navigation.Routes
import com.pickcode.v2.ui.theme.brandGradient
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
        // 品牌渐变头（跟其它页面的 GradientHeader 同一个渐变）
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brandGradient())
                .statusBarsPadding()
                .padding(vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(R.drawable.avatar),
                    contentDescription = "头像",
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .border(2.dp, colorScheme.onPrimary.copy(alpha = 0.7f), CircleShape)
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "取件码",
                    style = MaterialTheme.typography.titleLarge,
                    color = colorScheme.onPrimary
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "短信自动提取 · 数据只存本机",
                    style = MaterialTheme.typography.labelMedium,
                    color = colorScheme.onPrimary.copy(alpha = 0.85f)
                )
            }
        }

        // 统计：已收到 / 待取 / 已取件
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .offset(y = (-20).dp),
            shape = MaterialTheme.shapes.medium,
            color = colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatItem(value = received, label = "已收到", valueColor = colorScheme.onSurface, modifier = Modifier.weight(1f))
                StatDivider(colorScheme)
                StatItem(value = pending, label = "待取", valueColor = colorScheme.primary, modifier = Modifier.weight(1f))
                StatDivider(colorScheme)
                StatItem(value = picked, label = "已取件", valueColor = successColor(), modifier = Modifier.weight(1f))
            }
        }

        // 菜单
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .offset(y = (-8).dp),
            shape = MaterialTheme.shapes.medium,
            color = colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outlineVariant)
        ) {
            Column {
                MenuItem("匹配设置", Icons.Outlined.Tune) { rootNavController.navigate(Routes.MATCH_RULES) }
                MenuDivider(colorScheme)
                MenuItem("AI 设置", Icons.Outlined.SmartToy) { rootNavController.navigate(Routes.AI_SETTINGS) }
                MenuDivider(colorScheme)
                MenuItem("软件介绍", Icons.Outlined.Info) { rootNavController.navigate(Routes.ABOUT) }
                MenuDivider(colorScheme)
                MenuItem("更新记录", Icons.Outlined.History) { rootNavController.navigate(Routes.CHANGELOG) }
                MenuDivider(colorScheme)
                MenuItem("常见问题", Icons.AutoMirrored.Filled.HelpOutline) { rootNavController.navigate(Routes.FAQ) }
            }
        }

        Spacer(Modifier.height(24.dp))

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

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun StatItem(
    value: Int,
    label: String,
    valueColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$value",
            style = MaterialTheme.typography.headlineSmall,
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
        modifier = Modifier.height(28.dp).width(1.dp),
        color = colorScheme.outlineVariant
    )
}

@Composable
private fun MenuDivider(colorScheme: ColorScheme) {
    HorizontalDivider(
        thickness = 1.dp,
        color = colorScheme.outlineVariant,
        modifier = Modifier.padding(start = 52.dp)
    )
}

@Composable
private fun MenuItem(title: String, icon: ImageVector, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = title,
            tint = colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Icon(
            Icons.AutoMirrored.Filled.NavigateNext,
            contentDescription = null,
            tint = colorScheme.outline,
            modifier = Modifier.size(20.dp)
        )
    }
}
