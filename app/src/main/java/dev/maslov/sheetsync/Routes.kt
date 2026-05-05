package dev.maslov.sheetsync

import java.util.UUID

sealed class Routes(val value: String) {
    object Login : Routes("login")

    object RuleList : Routes("rules")

    object Settings : Routes("settings")

    object AddRule : Routes("add_rule")

    object RuleEdit : Routes("rule_edit/{ruleId}") {
        fun createRoute(ruleId: UUID) = "rule_edit/$ruleId"
    }
}
