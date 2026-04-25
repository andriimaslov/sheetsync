package dev.maslov.sheetsync.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.maslov.sheetsync.Routes
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
        startDestination = Routes.RuleList.value
    ) {
        composable("rules") {
            RuleListScreen(
                onOpenSettings = { navController.navigate(Routes.Settings.value) },
                onAddRule = {
                    navController.navigate(Routes.AddRule.value)
                },
                onSearch = { navController.navigate("search") },
                onEditRule = { ruleId -> navController.navigate(Routes.RuleEdit.createRoute(ruleId)) }
            )
        }

        composable(Routes.Settings.value) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.AddRule.value) {
            AddRuleScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.RuleEdit.value,
            arguments =
            listOf(
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
