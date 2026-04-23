package dev.maslov.sheetsync.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.maslov.sheetsync.ui.screens.RuleListScreen
import dev.maslov.sheetsync.ui.screens.SearchScreen
import dev.maslov.sheetsync.ui.screens.SettingsScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "rules"
    ) {
        composable("rules") {
            RuleListScreen(
                onOpenSettings = { navController.navigate("settings") },
                onSearch = { navController.navigate("search") }
            )
        }

        composable("settings") {
            SettingsScreen(onBack = { navController.popBackStack() })
        }

        composable("search") {
            SearchScreen(onBack = { navController.popBackStack() })
        }
    }
}