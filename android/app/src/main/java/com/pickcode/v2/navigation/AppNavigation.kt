package com.pickcode.v2.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pickcode.v2.ui.screen.main.MainScreen
import com.pickcode.v2.ui.screen.my.AboutScreen
import com.pickcode.v2.ui.screen.my.AiSettingsScreen
import com.pickcode.v2.ui.screen.my.ChangelogScreen
import com.pickcode.v2.ui.screen.my.FaqScreen
import com.pickcode.v2.ui.screen.my.MatchRulesScreen
import com.pickcode.v2.ui.screen.my.MatchSettingsScreen
import com.pickcode.v2.ui.screen.pickup.EditCodeScreen

object Routes {
    const val MAIN = "main"
    const val EDIT_CODE = "edit_code/{codeId}"
    const val MATCH_RULES = "match_rules"
    const val MATCH_SETTINGS = "match_settings?mode={mode}&id={ruleId}"
    const val AI_SETTINGS = "ai_settings"
    const val FAQ = "faq"
    const val ABOUT = "about"
    const val CHANGELOG = "changelog"

    fun editCode(codeId: Long) = "edit_code/$codeId"
    fun matchSettings(mode: String = "add", ruleId: String = "") =
        "match_settings?mode=$mode&id=$ruleId"
}

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.MAIN) {
        composable(Routes.MAIN) {
            MainScreen(rootNavController = navController)
        }

        composable(
            Routes.EDIT_CODE,
            arguments = listOf(navArgument("codeId") { type = NavType.LongType })
        ) {
            EditCodeScreen(navController = navController)
        }

        composable(Routes.MATCH_RULES) {
            MatchRulesScreen(navController = navController)
        }

        composable(
            Routes.MATCH_SETTINGS,
            arguments = listOf(
                navArgument("mode") { type = NavType.StringType; defaultValue = "add" },
                navArgument("ruleId") { type = NavType.StringType; defaultValue = "" }
            )
        ) {
            MatchSettingsScreen(navController = navController)
        }

        composable(Routes.AI_SETTINGS) {
            AiSettingsScreen(navController = navController)
        }

        composable(Routes.FAQ) {
            FaqScreen(navController = navController)
        }

        composable(Routes.ABOUT) {
            AboutScreen(navController = navController)
        }

        composable(Routes.CHANGELOG) {
            ChangelogScreen(navController = navController)
        }
    }
}
