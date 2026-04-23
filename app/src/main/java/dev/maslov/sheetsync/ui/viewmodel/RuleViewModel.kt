package dev.maslov.sheetsync.ui.viewmodel

import androidx.lifecycle.ViewModel
import dev.maslov.sheetsync.model.Rule
import dev.maslov.sheetsync.ui.screens.sampleRules
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RuleViewModel : ViewModel() {

    private val _rules = MutableStateFlow(sampleRules)
    val rules: StateFlow<List<Rule>> = _rules

    fun toggleRule(ruleId: String) {
        _rules.value = _rules.value.map { rule ->
            if (rule.id == ruleId) {
                rule.copy(isActive = !rule.isActive)
            } else rule
        }
    }

    fun deleteRule(ruleId: String) {
        _rules.value = _rules.value.filterNot { it.id == ruleId }
    }

    fun addRule(rule: Rule) {
        _rules.value += rule
    }
}