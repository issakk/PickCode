package com.pickcode.v2.ui.screen.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.ListAlt
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
import com.pickcode.v2.ui.screen.logs.LogsScreen
import com.pickcode.v2.ui.screen.my.MyScreen
import com.pickcode.v2.ui.screen.package_record.PackageRecordScreen
import com.pickcode.v2.ui.screen.pickup.PickupListScreen

enum class BottomTab(val route: String, val label: String, val icon: ImageVector) {
    Pickup("pickup_list", "取件码", Icons.Outlined.Inventory2),
    Packages("package_records", "包裹", Icons.Outlined.ListAlt),
    My("my", "我的", Icons.Outlined.Person),
    Logs("logs", "日志", Icons.Outlined.BugReport)
}

@Composable
fun MainScreen(rootNavController: NavHostController) {
    val tabNavController = rememberNavController()
    val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar(
                tonalElevation = 0.dp
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
                        label = { Text(tab.label, fontSize = 11.sp) }
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
            composable(BottomTab.Packages.route) { PackageRecordScreen(rootNavController) }
            composable(BottomTab.My.route) { MyScreen(rootNavController) }
            composable(BottomTab.Logs.route) { LogsScreen() }
        }
    }
}
