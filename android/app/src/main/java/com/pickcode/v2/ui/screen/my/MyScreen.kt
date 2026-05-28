package com.pickcode.v2.ui.screen.my

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.pickcode.v2.R
import com.pickcode.v2.navigation.Routes
import com.pickcode.v2.ui.theme.*

@Composable
fun MyScreen(
    rootNavController: NavHostController,
    viewModel: MyViewModel = hiltViewModel()
) {
    val stats by viewModel.stats.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header with gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(GradientStart, GradientEnd)))
                .statusBarsPadding()
                .padding(vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(R.drawable.avatar),
                    contentDescription = "头像",
                    modifier = Modifier.size(72.dp).clip(CircleShape)
                )
                Spacer(Modifier.height(8.dp))
                Text("快递取件助手", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Stats card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .offset(y = (-16).dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${stats.first}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Primary)
                    Text("已收到", fontSize = 12.sp, color = TextSecondary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${stats.second}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Primary)
                    Text("已取件", fontSize = 12.sp, color = TextSecondary)
                }
            }
        }

        // Menu items
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column {
                MenuItem("匹配设置", Icons.Outlined.Tune) { rootNavController.navigate(Routes.MATCH_RULES) }
                HorizontalDivider(color = Divider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                MenuItem("AI 设置", Icons.Outlined.SmartToy) { rootNavController.navigate(Routes.AI_SETTINGS) }
                HorizontalDivider(color = Divider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                MenuItem("软件介绍", Icons.Outlined.Info) { rootNavController.navigate(Routes.ABOUT) }
                HorizontalDivider(color = Divider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                MenuItem("更新记录", Icons.Outlined.History) { rootNavController.navigate(Routes.CHANGELOG) }
                HorizontalDivider(color = Divider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                MenuItem("常见问题", Icons.AutoMirrored.Filled.HelpOutline) { rootNavController.navigate(Routes.FAQ) }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Version
        Text(
            text = "v3.0.0",
            fontSize = 12.sp,
            color = TextTertiary,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun MenuItem(title: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = title, tint = Primary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(12.dp))
        Text(title, modifier = Modifier.weight(1f), fontSize = 15.sp)
        Icon(Icons.AutoMirrored.Filled.NavigateNext, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(18.dp))
    }
}
