package dev.maslov.sheetsync.configuration

import java.util.UUID

sealed class Routes(val value: String) {

    object RuleList : Routes("rules")

    object Settings : Routes("settings")

    object AddRule : Routes("add_rule")

    object RuleEdit : Routes("rule_edit/{ruleId}") {
        fun createRoute(ruleId: UUID) = "rule_edit/$ruleId"
    }

    object Onboarding : Routes("onboarding")
}
