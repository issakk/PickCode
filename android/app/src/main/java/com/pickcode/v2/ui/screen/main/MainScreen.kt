package com.pickcode.v2.ui.screen.main

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pickcode.v2.ui.screen.my.MyScreen
import com.pickcode.v2.ui.screen.pickup.PickupListScreen

enum class BottomTab(val route: String, val label: String, val icon: ImageVector) {
    Pickup("pickup_list", "取件码", Icons.Outlined.Inventory2),
    My("my", "我的", Icons.Outlined.Person)
}

@Composable
fun MainScreen(rootNavController: NavHostController) {
    val tabNavController = rememberNavController()
    val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        // 顶部 inset 交给各页面自己的顶栏处理（这样大标题能铺到状态栏下）
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar(
                tonalElevation = 0.dp,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                BottomTab.entries.forEach { tab ->
                    val selected = currentRoute == tab.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (currentRoute != tab.route) {
                                tabNavController.navigate(tab.route) {
                                    popUpTo(tabNavController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label, style = MaterialTheme.typography.labelMedium) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = tabNavController,
            startDestination = BottomTab.Pickup.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(BottomTab.Pickup.route) { PickupListScreen(rootNavController) }
            composable(BottomTab.My.route) { MyScreen(rootNavController) }
        }
    }
}
