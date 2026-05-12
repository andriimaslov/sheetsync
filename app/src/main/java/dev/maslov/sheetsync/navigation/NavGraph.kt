package dev.maslov.sheetsync.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.maslov.sheetsync.configuration.Routes
import dev.maslov.sheetsync.ui.screens.AddRuleScreen
import dev.maslov.sheetsync.ui.screens.OnboardingScreen
import dev.maslov.sheetsync.ui.screens.RuleEditScreen
import dev.maslov.sheetsync.ui.screens.RuleListScreen
import dev.maslov.sheetsync.ui.screens.SearchScreen
import dev.maslov.sheetsync.ui.screens.SettingsScreen
import dev.maslov.sheetsync.ui.screens.SplashScreen
import dev.maslov.sheetsync.ui.viewmodel.AppListViewModel
import dev.maslov.sheetsync.ui.viewmodel.AuthViewModel
import dev.maslov.sheetsync.ui.viewmodel.ClientCredentialsViewModel
import dev.maslov.sheetsync.ui.viewmodel.OnboardingViewModel
import dev.maslov.sheetsync.ui.viewmodel.RuleViewModel
import dev.maslov.sheetsync.ui.viewmodel.SettingsViewModel
import dev.maslov.sheetsync.ui.viewmodel.SheetsViewModel
import java.util.UUID

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val onboardingViewModel: OnboardingViewModel = hiltViewModel()
    val credentialsViewModel: ClientCredentialsViewModel = hiltViewModel()
    val ruleViewModel: RuleViewModel = hiltViewModel()
    val appListViewModel: AppListViewModel = hiltViewModel()
    val sheetsViewModel: SheetsViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()

    val isFirstLaunch = onboardingViewModel.isFirstLaunch.collectAsState().value
    if (isFirstLaunch == null) {
        SplashScreen()
        return
    }

    val startDestination =
        if (isFirstLaunch) {
            Routes.Onboarding.value
        } else {
            Routes.RuleList.value
        }
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.Onboarding.value) {
            OnboardingScreen(
                onboardingViewModel = onboardingViewModel,
                authViewModel = authViewModel,
                credentialsViewModel = credentialsViewModel,
                onFinish = {
                    onboardingViewModel.finishOnboarding()
                    navController.navigate(Routes.RuleList.value) {
                        popUpTo(Routes.Onboarding.value) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable("rules") {
            RuleListScreen(
                onOpenSettings = { navController.navigate(Routes.Settings.value) },
                onAddRule = {
                    navController.navigate(Routes.AddRule.value)
                },
                onSearch = { navController.navigate("search") },
                onEditRule = { ruleId -> navController.navigate(Routes.RuleEdit.createRoute(ruleId)) },
                ruleViewModel = ruleViewModel,
                appListViewModel = appListViewModel
            )
        }

        composable(Routes.Settings.value) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                settingsViewModel = settingsViewModel,
                authViewModel = authViewModel,
                credentialsViewModel = credentialsViewModel
            )
        }

        composable(Routes.AddRule.value) {
            AddRuleScreen(
                onBack = { navController.popBackStack() },
                ruleViewModel = ruleViewModel,
                appListViewModel = appListViewModel,
                sheetsViewModel = sheetsViewModel,
                authViewModel = authViewModel
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
                onBack = { navController.popBackStack() },
                ruleViewModel = ruleViewModel,
                appListViewModel = appListViewModel,
                sheetsViewModel = sheetsViewModel,
                authViewModel = authViewModel
            )
        }

        composable("search") {
            SearchScreen(onBack = { navController.popBackStack() })
        }
    }
}
