package dev.maslov.sheetsync

import java.util.UUID

sealed class Screen(val route: String) {
    object RuleList : Screen("rules")
    object Settings : Screen("settings")
    object AddRule : Screen("add_rule")

    object RuleEdit : Screen("rule_edit/{ruleId}") {
        fun createRoute(ruleId: UUID) = "rule_edit/$ruleId"
    }
}