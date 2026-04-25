package dev.maslov.sheetsync.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.maslov.sheetsync.Screen
import dev.maslov.sheetsync.ui.screens.AddRuleScreen
import dev.maslov.sheetsync.ui.screens.RuleEditScreen
import dev.maslov.sheetsync.ui.screens.RuleListScreen
import dev.maslov.sheetsync.ui.screens.SearchScreen
import dev.maslov.sheetsync.ui.screens.SettingsScreen
import java.util.UUID

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.RuleList.route
    ) {
        composable("rules") {
            RuleListScreen(
                onOpenSettings = { navController.navigate(Screen.Settings.route) },
                onAddRule = {
                    navController.navigate(Screen.AddRule.route)
                },
                onSearch = { navController.navigate("search") },
                onEditRule = { ruleId -> navController.navigate(Screen.RuleEdit.createRoute(ruleId)) }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }


        composable(Screen.AddRule.route) {
            AddRuleScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.RuleEdit.route,
            arguments = listOf(
                navArgument("ruleId") { type = NavType.StringType }
            )
        ) { backStackEntry ->

            val rule = backStackEntry.arguments?.getString("ruleId")!!

            RuleEditScreen(
                ruleId = UUID.fromString(rule),
                onBack = { navController.popBackStack() }
            )
        }

        composable("search") {
            SearchScreen(onBack = { navController.popBackStack() })
        }
    }
}